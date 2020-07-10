### Start kafka
https://kafka.apache.org/quickstart
1. cd /Users/esuarez/dev/kafka_2.11-2.1.0
2. bin/zookeeper-server-start.sh config/zookeeper.properties
3. bin/kafka-server-start.sh config/server.properties

#### Delete topic
bin/kafka-topics.sh --zookeeper localhost:2181 --delete --topic <topic-name>

#### List topics
bin/kafka-topics.sh --list --zookeeper localhost:2181

### REST endpoints
1. http://localhost:8084/v1/qry/orders
2. http://localhost:8084/v1/qry/orders/customers/1
3. http://localhost:8084/v1/qry/orders/validations/status