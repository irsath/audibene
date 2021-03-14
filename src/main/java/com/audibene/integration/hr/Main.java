package com.audibene.integration.hr;

import com.audibene.integration.hr.sink.UserEventProducer;
import com.audibene.integration.hr.source.UserResource;
import org.apache.pulsar.client.api.PulsarClientException;

import java.time.Duration;
import java.util.Timer;

public class Main {
    public static void main(String[] args) throws PulsarClientException {
        Timer t = new Timer();
        UserApiPullTask mTask = new UserApiPullTask(new UserResource(), new UserEventProducer());
        t.scheduleAtFixedRate(mTask, 0, Duration.ofMinutes(5).toMillis());

        Thread consumerThread = new Thread(new TestConsumer());
        consumerThread.start();
    }

}
