#!/usr/bin/env python3
"""
DataFlow Dataset Generator
Generates realistic Zomato-style restaurant and review datasets with configurable
size and controlled data quality anomaly injection for ETL testing and benchmarking.
"""

import csv
import random
import argparse
import sys
from pathlib import Path

CITIES = [
    ("Bangalore", ["Koramangala", "Indiranagar", "Whitefield", "Jayanagar", "HSR Layout", "MG Road", "Bannerghatta Road", "Bellandur"]),
    ("New Delhi", ["Connaught Place", "Hauz Khas", "Saket", "Karol Bagh", "Chandni Chowk", "Vasant Kunj", "Rajouri Garden"]),
    ("Mumbai", ["Bandra West", "Andheri West", "Colaba", "Juhu", "Lower Parel", "Powai", "Dadar", "Worli"]),
    ("Hyderabad", ["Banjara Hills", "Jubilee Hills", "Gachibowli", "Hitech City", "Madhapur", "Kondapur"]),
    ("Pune", ["Koregaon Park", "Viman Nagar", "Kothrud", "Baner", "Hinjewadi", "FC Road"]),
    ("Chennai", ["T. Nagar", "Adyar", "Nungambakkam", "Velachery", "Anna Nagar", "Alwarpet"]),
    ("Kolkata", ["Park Street", "Salt Lake", "Ballygunge", "New Town", "Southern Avenue"])
]

NAME_PREFIXES = [
    "The Royal", "Spice", "Urban", "Flavors of", "Barbeque", "Cafe", "Bistro", "Golden", "Little",
    "Saffron", "Tandoori", "Copper", "Biryani", "Mainland", "Smoky", "Green", "Sweet", "Grand",
    "Social", "Chai", "Daily", "Coastal", "Dhaba", "Trattoria", "Wok", "Dragon", "Punjabi", "Madras"
]

NAME_SUFFIXES = [
    "Kitchen", "Bites", "Diner", "Lounge", "Grill", "House", "Corner", "Boutique", "Treats",
    "Express", "Palace", "Plaza", "Hub", "Junction", "Table", "Room", "Pot", "Stories",
    "Central", "Garden", "Cuisine", "Eats", "Co.", "Point", "Delight"
]

CUISINE_POOL = [
    "North Indian", "South Indian", "Chinese", "Continental", "Italian", "Mughlai",
    "Biryani", "Fast Food", "Street Food", "Beverages", "Desserts", "Cafe",
    "Asian", "Thai", "Mexican", "Bakery", "Seafood", "Momos", "Pizza", "Burger"
]

REVIEW_TEMPLATES = [
    # Positive
    ("The butter chicken and garlic naan were absolutely spectacular! Prompt service and cozy ambiance.", "Excellent", 4.8),
    ("Loved the handcrafted pasta and wood-fired pizza. Great coffee and polite staff. Will visit again!", "Very Good", 4.5),
    ("Best biryani in town! Rich aroma, tender meat, and fast table service. Highly recommended for families.", "Excellent", 4.7),
    ("Amazing South Indian thali. Filter coffee was authentically brewed. Value for money meal.", "Very Good", 4.3),
    ("Crispy dosas and fresh chutneys. Fast turnaround time and very hygienic preparation.", "Good", 4.1),
    ("Great cafe vibe, excellent Wi-Fi, and top-notch cheesecake. Perfect spot for remote working.", "Very Good", 4.4),
    
    # Neutral / Mixed
    ("Decent food quality but the delivery took over 50 minutes. Samosas were slightly cold.", "Average", 3.2),
    ("The ambience is stunning, but food portions are small relative to the high price tag.", "Average", 3.4),
    ("Standard Chinese dishes. Manchurian was good, but the fried rice was oily and under-salted.", "Average", 3.0),
    ("Good location and spacious seating, but the waiters were inattentive during peak dinner rush.", "Average", 3.1),
    
    # Negative
    ("Terrible experience. Waited 45 minutes for a table despite reservation. Food was cold and bland.", "Poor", 1.8),
    ("Dreadful delivery packaging! Soup spilled completely inside the bag. Customer support refused refund.", "Poor", 1.5),
    ("Extremely overpriced for mediocre quality. Paneer tikka was rubbery and lacked seasoning.", "Poor", 2.1),
    ("Unprofessional staff and loud acoustics. Found a hair in the salad. Never ordering again.", "Poor", 1.2),
    ("Stale food served. Got mild food poisoning the next day. Health inspectors need to visit this place.", "Poor", 1.4)
]

def generate_record(record_id: int, inject_anomalies: bool = False):
    city_info = random.choice(CITIES)
    city = city_info[0]
    locality = random.choice(city_info[1])
    
    name = f"{random.choice(NAME_PREFIXES)} {random.choice(NAME_SUFFIXES)}"
    address = f"Shop {random.randint(1, 150)}, 2nd Floor, {locality}, {city}"
    
    # Cuisines (1 to 3 random cuisines)
    num_cuisines = random.randint(1, 3)
    cuisines = ", ".join(random.sample(CUISINE_POOL, num_cuisines))
    
    # Base review and rating
    review_info = random.choice(REVIEW_TEMPLATES)
    review_text = review_info[0]
    rating_text = review_info[1]
    rating = round(review_info[2] + random.uniform(-0.2, 0.2), 1)
    rating = max(1.0, min(5.0, rating))
    
    # Price
    price_tier = random.choices([1, 2, 3, 4], weights=[0.3, 0.4, 0.2, 0.1])[0]
    cost_base = {1: random.randint(150, 400), 2: random.randint(450, 900), 3: random.randint(950, 1800), 4: random.randint(1900, 4500)}
    average_cost = cost_base[price_tier]
    
    has_online_delivery = "Yes" if random.random() > 0.25 else "No"
    has_table_booking = "Yes" if price_tier >= 3 and random.random() > 0.4 else "No"
    votes = random.randint(10, 3500)
    currency = "INR"
    
    restaurant_id_val = str(record_id)
    rating_val = str(rating)
    cost_val = str(average_cost)
    
    # Controlled anomaly injection (roughly 5% of records if enabled)
    if inject_anomalies and random.random() < 0.05:
        anomaly_type = random.choice([
            "malformed_rating_new",
            "malformed_rating_dash",
            "malformed_rating_out_of_bounds",
            "negative_cost",
            "missing_name",
            "missing_city",
            "inconsistent_boolean",
            "cost_with_comma"
        ])
        
        if anomaly_type == "malformed_rating_new":
            rating_val = "NEW"
            rating_text = "Not rated"
        elif anomaly_type == "malformed_rating_dash":
            rating_val = "-"
            rating_text = "Not rated"
        elif anomaly_type == "malformed_rating_out_of_bounds":
            rating_val = "-1.5" if random.random() < 0.5 else "6.8"
        elif anomaly_type == "negative_cost":
            cost_val = "-450"
        elif anomaly_type == "missing_name":
            name = ""
        elif anomaly_type == "missing_city":
            city = ""
        elif anomaly_type == "inconsistent_boolean":
            has_online_delivery = random.choice(["TRUE", "1", "y", "FALSE", "0"])
        elif anomaly_type == "cost_with_comma":
            cost_val = f'"{average_cost:,.0f}"'

    return {
        "restaurant_id": restaurant_id_val,
        "name": name,
        "city": city,
        "address": address,
        "locality": locality,
        "cuisines": cuisines,
        "average_cost_for_two": cost_val,
        "currency": currency,
        "has_online_delivery": has_online_delivery,
        "has_table_booking": has_table_booking,
        "price_range": str(price_tier),
        "aggregate_rating": rating_val,
        "rating_text": rating_text,
        "votes": str(votes),
        "review_text": review_text
    }

def generate_csv(output_path: Path, num_rows: int, inject_anomalies: bool = True):
    fieldnames = [
        "restaurant_id",
        "name",
        "city",
        "address",
        "locality",
        "cuisines",
        "average_cost_for_two",
        "currency",
        "has_online_delivery",
        "has_table_booking",
        "price_range",
        "aggregate_rating",
        "rating_text",
        "votes",
        "review_text"
    ]
    
    output_path.parent.mkdir(parents=True, exist_ok=True)
    
    print(f"Generating {num_rows} records into {output_path} (anomalies={inject_anomalies})...")
    
    random.seed(42)  # Deterministic generation for reproducible tests
    
    with open(output_path, mode="w", newline="", encoding="utf-8") as f:
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        writer.writeheader()
        
        # Inject duplicate record scenario: record 1005 is a duplicate of 1001
        for i in range(1, num_rows + 1):
            if inject_anomalies and i == 1005 and num_rows >= 1005:
                # Deliberate duplicate of record 1001
                rec = generate_record(1001, inject_anomalies=False)
                rec["review_text"] = "Duplicate review entry for same restaurant ID."
            else:
                rec = generate_record(1000 + i, inject_anomalies=inject_anomalies)
            writer.writerow(rec)
            
            if i % 100000 == 0:
                print(f"  Processed {i}/{num_rows} records...")
                
    file_size_mb = output_path.stat().st_size / (1024 * 1024)
    print(f"Dataset generated successfully! File size: {file_size_mb:.2f} MB")

def main():
    parser = argparse.ArgumentParser(description="Generate synthetic Zomato-style restaurant dataset")
    parser.add_argument("--rows", type=int, default=1500, help="Number of rows to generate (default: 1500)")
    parser.add_argument("--output", type=str, default="data/samples/zomato_restaurants.csv", help="Output CSV path")
    parser.add_argument("--no-anomalies", action="store_true", help="Disable injection of data quality anomalies")
    args = parser.parse_args()
    
    output_file = Path(args.output)
    generate_csv(output_file, args.rows, inject_anomalies=not args.no_anomalies)

if __name__ == "__main__":
    main()
