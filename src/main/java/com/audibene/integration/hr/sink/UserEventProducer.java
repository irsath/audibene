package com.audibene.integration.hr.sink;

import com.audibene.integration.hr.ApplicationProperties;
import com.audibene.integration.hr.source.struct.User;
import org.apache.pulsar.client.api.*;

import java.io.Closeable;
import java.io.IOException;

public class UserEventProducer implements Closeable {
    private static final String SERVICE_URL = ApplicationProperties.get("pulsar.url");
    private final PulsarClient client;
    private final Producer<User> producer;

    public UserEventProducer() throws PulsarClientException {
        client = PulsarClient.builder()
                .serviceUrl(SERVICE_URL)
                .authentication(AuthenticationFactory.token(ApplicationProperties.get("pulsar.token")))
                .build();

        producer = client.newProducer(Schema.JSON(User.class))
                .topic(ApplicationProperties.get("pulsar.user.topic"))
                .create();
    }

    public void send(User data) throws PulsarClientException {
        producer.send(data);
    }

    @Override
    public void close() throws IOException {
        producer.close();
        client.close();
    }
}
