package org.apache.fineract.paymenthub.exporter.nats;

import io.nats.client.Connection;
import io.nats.client.Nats;
import io.nats.client.Options;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;

@Component
@ConditionalOnExpression("${exporter.nats.enabled}")
public class NatsConfig {

    @Value("${exporter.nats.server}")
    String natsServer;

    @Bean
    public Connection natsConnection() throws IOException, InterruptedException {
        Options o = new Options.Builder().server(natsServer).maxReconnects(-1).build();
        Connection nc = Nats.connect(o);

        return nc;
    }
}