package com.mx.fintecheando.natsstreamer.exporter.model;

public interface MessageProducer {

    void send(String topicName, String message);

}