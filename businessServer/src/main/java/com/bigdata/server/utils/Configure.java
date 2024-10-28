package com.bigdata.server.utils;

import com.mongodb.MongoClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import redis.clients.jedis.Jedis;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import java.util.HashMap;
import java.util.Map;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

@Configuration
public class Configure {

    private String jedisHost;

    private String mongoHost;
    private int mongoPort;

    private String esClusterName;
    private String esHost;
    private int esPort;

    private String kafkaBootstrapServers;

    public Configure() throws IOException {

        Properties properties = new Properties();
        Resource resource = new ClassPathResource("application.properties");
        properties.load(new FileInputStream(resource.getFile()));

        this.jedisHost = properties.getProperty("jedis.host");
        this.mongoHost = properties.getProperty("mongo.host");
        this.mongoPort = Integer.parseInt(properties.getProperty("mongo.port"));
        this.esClusterName = properties.getProperty("es.cluster.name");
        this.esHost = properties.getProperty("es.host");
        this.esPort = Integer.parseInt(properties.getProperty("es.port"));

        this.kafkaBootstrapServers = properties.getProperty("kafka.bootstrap-servers");

    }


    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "hadoop100:9092");
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }


    @Bean("jedis")
    public Jedis getJedis(){
        Jedis jedis = new Jedis(this.jedisHost);
        return jedis;
    }

    @Bean("mongoClient")
    public MongoClient getMongoClient(){
        MongoClient mongoClient = new MongoClient(this.mongoHost,this.mongoPort);
        return mongoClient;
    }

    @Bean("esClient")
    public TransportClient getTransportClient() throws UnknownHostException {
        Settings settings = Settings.builder().put("cluster.name",this.esClusterName).build();
        TransportClient esClient = new PreBuiltTransportClient(settings);
        esClient.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(this.esHost),this.esPort));
        return esClient;
    }
}
