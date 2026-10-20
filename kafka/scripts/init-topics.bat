@echo off
echo ===================================================
echo Initializing DataFlow Kafka Topics...
echo ===================================================

docker exec -it dataflow-kafka kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic dataflow.restaurant.events --partitions 3 --replication-factor 1
docker exec -it dataflow-kafka kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic dataflow.restaurant.reviews --partitions 3 --replication-factor 1
docker exec -it dataflow-kafka kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --topic dataflow.restaurant.dlq --partitions 1 --replication-factor 1

echo.
echo Active Topics:
docker exec -it dataflow-kafka kafka-topics --bootstrap-server localhost:9092 --list
echo ===================================================
