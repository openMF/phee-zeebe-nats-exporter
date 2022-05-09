package org.apache.fineract.paymenthub.exporter.nats;

import org.apache.fineract.paymenthub.exporter.config.ElasticSearchConfiguration;
import org.apache.fineract.paymenthub.exporter.config.ExporterMetrics;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import io.zeebe.protocol.record.Record;
import io.nats.client.Connection;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

public class NatsExporterClient {
    private final Logger logger;
    private final ElasticSearchConfiguration configuration;
    private ExporterMetrics metrics;
    private AtomicLong sentToNats = new AtomicLong(0);
    private boolean initialized;
    private Connection natsConnection;

    @Value("${exporter.nats.topic}")
    private String zeebeExportTopic;

    public NatsExporterClient(final ElasticSearchConfiguration configuration, final Logger logger,
        Connection natsConnection) {
        this.configuration = configuration;
        this.logger = logger;
        this.natsConnection = natsConnection;
    }

    public void index(final Record<?> record) {
        if (metrics == null && !initialized) {
            try {
                metrics = new ExporterMetrics(record.getPartitionId());
            } catch (Exception e) {
                logger.warn("## failed to initialize metrics, continuing without it");
            }
            initialized = true;
        }

        if (configuration.shouldIndexRecord(record)) {
            logger.trace("sending record to kafka: {}", record.toJson());
            sentToNats.incrementAndGet();
            metrics.recordBulkSize(1);
            natsConnection.request(zeebeExportTopic, record.toJson().getBytes(StandardCharsets.UTF_8));
        } else {
            logger.trace("skipping record: {}", record.toString());
        }
    }

    public boolean flush() {
        if (sentToNats.get() > 0) {
            try {
                natsConnection.flush(Duration.ofSeconds(1)); // Flush the message queue
                logger.info("flushed {} exported records to Nats", sentToNats.get());
                sentToNats.set(0);
            } catch (TimeoutException e) {
                logger.error("flushed error {}", e.getMessage());
            } catch (InterruptedException e) {
                logger.error("flushed error {}", e.getMessage());
            }
        }
        return true;
    }

    public void close() {
        try {
            natsConnection.close();
        } catch (InterruptedException e) {
            logger.error("Close connection {}", e.getMessage());
        }
    }

    public boolean shouldFlush() {
        return sentToNats.get() >= configuration.bulk.size;
    }

    protected String idFor(final Record<?> record) {
        return record.getPartitionId() + "-" + record.getPosition();
    }
}
