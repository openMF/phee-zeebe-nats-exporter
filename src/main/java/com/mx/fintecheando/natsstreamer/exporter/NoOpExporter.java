package com.mx.fintecheando.natsstreamer.exporter;

import io.camunda.zeebe.exporter.api.Exporter;
import io.camunda.zeebe.exporter.api.context.Context;
import io.camunda.zeebe.exporter.api.context.Controller;
import io.camunda.zeebe.protocol.record.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoOpExporter implements Exporter {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void configure(Context context) throws Exception {
        logger.info("## no-op exporter configured");
    }

    @Override
    public void open(Controller controller) {
        logger.info("## no-op exporter opened");
    }

    @Override
    public void close() {
        logger.info("## no-op exporter closed");
    }

    @Override
    public void export(Record<?> record) {
        logger.info("## no-op exporter record");
    }
}
