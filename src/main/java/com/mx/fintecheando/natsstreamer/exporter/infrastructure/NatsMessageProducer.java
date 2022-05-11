/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mx.fintecheando.natsstreamer.exporter.infrastructure;

import io.nats.client.Connection;
import io.nats.client.Nats;
//import io.nats.streaming.StreamingConnectionFactory;
import com.mx.fintecheando.natsstreamer.exporter.model.MessageProducer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
       
public class NatsMessageProducer implements MessageProducer {

    private final String natsUrl;
    //private final String clusterId;
    //private final String clientId;

    private Connection connection;

    public NatsMessageProducer(String natsUrl) {
        this.natsUrl = natsUrl;
        //this.clusterId = clusterId;
        //this.clientId = clientId;
    }
    
    public NatsMessageProducer(Map<String, Object> natsProperties) {
        this.natsUrl = natsProperties.get("natsUrl").toString();
        //this.clusterId = natsProperties.get("clusterId").toString();
        //this.clientId = natsProperties.get("clientId").toString();
    }

    public void start() throws Exception {        
        connection = Nats.connect(natsUrl);
    }

    @Override
    public void send(String topicName, String message) {
        try {
            connection.publish(topicName, message.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void close() throws Exception {
        this.connection.close();
    }
    
    public void flush() throws Exception {        
        this.connection.flush(Duration.ofSeconds(1)); 
    }

}