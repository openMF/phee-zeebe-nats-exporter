/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mx.fintecheando.natsstreamer.exporter.infrastructure;

import io.nats.client.Connection;
import io.nats.client.Nats;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import io.nats.client.Message;
import io.nats.client.impl.Headers;
import io.nats.client.impl.NatsMessage;
       
public class NatsMessageProducer {

    private final String natsUrl;

    private Connection connection;

    public NatsMessageProducer(String natsUrl) {
        this.natsUrl = natsUrl;
    }
    
    public NatsMessageProducer(Map<String, Object> natsProperties) {
        this.natsUrl = natsProperties.get("natsUrl").toString();
    }

    public void start() throws Exception {        
        connection = Nats.connect(natsUrl);
    }
    
    public void send(String topicName, String idRecord, String message) {
        try {
            Headers headers = new Headers();
            headers.add("idRecord", idRecord);
            Message msg = NatsMessage.builder()
                            .data(message.getBytes(StandardCharsets.UTF_8))
                            .subject(topicName)
                            .headers(headers)
                            .build();
            connection.publish(msg);
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