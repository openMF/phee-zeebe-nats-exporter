# Zeebe Exporter for NATS

An easy to use exporter which will export Zeebe records to a configured Nats topic.

## Usage

The exporter is set up to stream records from Zeebe to Nats as they are processed by the exporter
stream processor. While this is done asynchronously. As such, your consumers should use `read_committed`
isolation level.

The following variables should be set in the Zeebe Broker Instances.

      - ZEEBE_LOG_LEVEL=debug
      - ZEEBE_BROKER_NETWORK_HOST=0.0.0.0
      - ZEEBE_BROKER_EXECUTION_METRICS_EXPORTER_ENABLED=true
      - ZEEBE_BROKER_EXPORTERS_NATS_JARPATH=/usr/local/zeebe/exporters/exporter-1.0.0-SNAPSHOT.jar
      - ZEEBE_BROKER_EXPORTERS_NATS_CLASSNAME=com.mx.fintecheando.natsstreamer.exporter.NatsExporter
      - ZEEBE_BROKER_EXPORTERS_NATS_ARGS_URL=nats://messaging-server:4222
