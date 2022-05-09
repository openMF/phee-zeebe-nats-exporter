package org.apache.fineract.paymenthub.exporter.nats;


import java.time.Duration;

import org.apache.fineract.paymenthub.exporter.config.ElasticSearchConfiguration;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;

import io.nats.client.Connection;

import io.zeebe.exporter.api.Exporter;
import io.zeebe.exporter.api.context.Context;
import io.zeebe.exporter.api.context.Controller;
import io.zeebe.protocol.record.Record;

@ConditionalOnExpression("${exporter.nats.enabled}")
public class NatsExporter implements Exporter {
    private Logger logger;

    private Controller controller;
    private ElasticSearchConfiguration configuration;
    private Connection natsConnection;
    private NatsExporterClient client;

    private long lastPosition = -1;

    @Value("${exporter.nats.topic}")
    private String natsTopic;

    @Override
    public void configure(final Context context) {
        try {
            logger = context.getLogger();
            configuration = context.getConfiguration().instantiate(ElasticSearchConfiguration.class);
            logger.debug("DPC Kafka exporter configured with {}", configuration);

            // context.setFilter(new KafkaRecordFilter(configuration));
        } catch (Exception e) {
            logger.error("Failed to configure KafkaExporter", e);
        }
    }

    @Override
    public void open(final Controller controller) {
        logger.info("DPC Kafka exporter opening");
        this.controller = controller;
        client = new NatsExporterClient(configuration, logger, natsConnection);

        // scheduleDelayedFlush();
        logger.info("DPC Kafka exporter opened");
    }

    @Override
    public void close() {
        flush();
    }

    @Override
    public void export(final Record<?> record) {
        client.index(record);
        lastPosition = record.getPosition();

        if (client.shouldFlush()) {
            flush();
        }
    }

    /*
    private void flushAndReschedule() {
        try {
            flush();
        } catch (final Exception e) {
            logger.error("Unexpected exception occurred on periodically flushing bulk, will retry later.", e);
        }
        scheduleDelayedFlush();
    }

    private void scheduleDelayedFlush() {
        controller.scheduleTask(Duration.ofSeconds(configuration.bulk.delay), this::flushAndReschedule);
    }
    */
    
    private void flush() {
        if (client.flush()) {
            controller.updateLastExportedRecordPosition(lastPosition);
        } else {
            logger.warn("Failed to flush bulk completely");
        }
    }
}
