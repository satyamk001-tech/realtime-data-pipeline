#!/bin/bash
set -e

echo "==================================================="
echo "Initializing DataFlow Kafka Topics..."
echo "==================================================="

docker exec -i dataflow-kafka kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic dataflow.restaurant.events --partitions 3 --replication-factor 1
docker exec -i dataflow-kafka kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic dataflow.restaurant.reviews --partitions 3 --replication-factor 1
docker exec -i dataflow-kafka kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic dataflow.restaurant.dlq --partitions 1 --replication-factor 1

echo ""
echo "Active Topics:"
docker exec -i dataflow-kafka kafka-topics --bootstrap-server localhost:9092 --list
echo "==================================================="
