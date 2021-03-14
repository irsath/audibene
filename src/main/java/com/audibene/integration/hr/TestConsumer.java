package com.audibene.integration.hr;

import com.audibene.integration.hr.source.struct.User;
import org.apache.pulsar.client.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class TestConsumer implements Closeable, Runnable {
    private final static Logger logger = LoggerFactory.getLogger(TestConsumer.class);

    private static final String SERVICE_URL = ApplicationProperties.get("pulsar.url");
    private final PulsarClient client;
    private final Consumer<User> consumer;

    public TestConsumer() throws PulsarClientException {
        client = PulsarClient.builder()
                .serviceUrl(SERVICE_URL)
                .authentication(AuthenticationFactory.token(ApplicationProperties.get("pulsar.token")))
                .build();

        consumer = client.newConsumer(Schema.JSON(User.class))
                .subscriptionName("user-event-subscriber")
                .topic(ApplicationProperties.get("pulsar.user.topic"))
                .subscribe();
    }

    @Override
    public void close() throws IOException {
        consumer.close();
        client.close();
    }

    @Override
    public void run() {
        while (true) {
            try {
                Message msg = consumer.receive(1, TimeUnit.SECONDS);

                if (msg != null) {
                    logger.info("Received pulsar event : {}", new String(msg.getData()));
                    consumer.acknowledge(msg);
                }
            } catch (PulsarClientException e) {
                logger.error("Pulsar event consumption error", e);
            }
        }
    }
}
