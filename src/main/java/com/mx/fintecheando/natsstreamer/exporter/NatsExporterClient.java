/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.0. You may not use this file
 * except in compliance with the Zeebe Community License 1.0.
 */
package com.mx.fintecheando.natsstreamer.exporter;

import com.mx.fintecheando.natsstreamer.exporter.infrastructure.NatsMessageProducer;
import io.camunda.zeebe.protocol.record.Record;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class NatsExporterClient {
    private final Logger logger;
    private final NatsExporterConfiguration configuration;
    private NatsExporterMetrics metrics;
    private AtomicLong sentToNats = new AtomicLong(0);
    private boolean initialized;

    // json content for now
    private NatsMessageProducer producer;
    public static final String NATS_TOPIC = "zeebe-export";

    public NatsExporterClient(final NatsExporterConfiguration configuration, final Logger logger) {
        this.configuration = configuration;
        this.logger = logger;
        Map<String, Object> natsProperties = new HashMap<>();

        String clientId = buildNatsClientId(logger);
        natsProperties.put("natsUrl", "nats://messaging-server:4222");
        natsProperties.put("clientId", clientId);
        natsProperties.put("clusterId", clientId);
        this.producer = new NatsMessageProducer(natsProperties);
        try {
            this.producer.start();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        logger.info("configured Nats producer with client id {}", clientId);
    }

    public static String buildNatsClientId(Logger logger) {
        return UUID.randomUUID().toString();
    }

    public void close() {
        try{
            producer.close();
        } 
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void index(final Record<?> record) {
        if (metrics == null && !initialized) {
            try {
                metrics = new NatsExporterMetrics(record.getPartitionId());
            } catch (Exception e) {
                logger.warn("## failed to initialize metrics, continuing without it");
            }
            initialized = true;
        }

        if (configuration.shouldIndexRecord(record)) {
            logger.trace("sending record to Nats: {}", record.toJson());
            sentToNats.incrementAndGet();
            metrics.recordBulkSize(1);
            producer.send(NATS_TOPIC, idFor(record), record.toJson());
        } else {
            logger.trace("skipping record: {}", record.toString());
        }
    }

    /**
     * @return true if all bulk records where flushed successfully
     */
    public boolean flush() {
        if (sentToNats.get() > 0) {
            try {
                producer.flush();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            logger.info("flushed {} exported records to Nats", sentToNats.get());
            sentToNats.set(0);
        }
        return true;
    }

    public boolean shouldFlush() {
        return sentToNats.get() >= configuration.bulk.size;
    }

    protected String idFor(final Record<?> record) {
        return record.getPartitionId() + "-" + record.getPosition();
    }
}
