package com.bigdata.kafkastream;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.processor.TopologyBuilder;

import java.util.Properties;

public class Application {

    public static void main(String[] args) {
//        if (args.length < 4) {
//            System.err.println("Usage: kafkaStream <brokers> <zookeepers> <from> <to>\n" +
//                    "  <brokers> is a list of one or more Kafka brokers\n" +
//                    "  <zookeepers> is a list of one or more Zookeeper nodes\n" +
//                    "  <from> is a topic to consume from\n" +
//                    "  <to> is a topic to product to\n\n");
//            System.exit(1);
//        }
//        String brokers = args[0];
//        String zookeepers = args[1];
//        String from = args[2];
//        String to = args[3];

        String from = "abc";
        String to = "recommender";
        String brokers = "hadoop100:9092";
        String zookeepers = "hadoopp100:2181";

        Properties properties = new Properties();
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "logProcessor");
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        properties.put(StreamsConfig.ZOOKEEPER_CONNECT_CONFIG, zookeepers);

        StreamsConfig config = new StreamsConfig(properties);

        TopologyBuilder builder = new TopologyBuilder();
        builder.addSource("source", from)
                .addProcessor("process", () -> new LogProcessor(), "source")
                .addSink("sink", to, "process");

        KafkaStreams kafkaStreams = new KafkaStreams(builder, config);

        kafkaStreams.start();
    }
}
