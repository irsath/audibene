package com.audibene.integration.hr;

import com.audibene.integration.hr.sink.UserEventProducer;
import com.audibene.integration.hr.source.UserResource;
import com.audibene.integration.hr.source.struct.User;
import org.apache.pulsar.client.api.PulsarClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.TimerTask;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class UserApiPullTask extends TimerTask {
    private final static Logger logger = LoggerFactory.getLogger(UserApiPullTask.class);

    private final UserResource userResource;
    private final UserEventProducer userEventProducer;
    private int pageToFecth;
    private int maxUsersPerPage;

    public UserApiPullTask(UserResource userResource, UserEventProducer userEventProducer) {
        this.userResource = userResource;
        this.userEventProducer = userEventProducer;
        this.pageToFecth = 0;
    }

    @Override
    public void run() {
        logger.info("Fetching new users...");

        List<User> users = fetchUsers();
        for (User u: users) {
            try {
                userEventProducer.send(u);
            } catch (PulsarClientException e) {
                logger.error("Unable to publish event for user.", e);
            }
        }
        logger.info("Found {} users", users.size());
    }

    private List<User> fetchUsers(){
        List<User> users;
        if (pageToFecth == 0) {
            // First run
            logger.info("First run detected. Fetching all users. This might take a while...");
            maxUsersPerPage = userResource.usersPerPage();
            pageToFecth = userResource.pagesCount() + 1;
            users = IntStream.range(1, pageToFecth)
                    .mapToObj(userResource::getUsers)
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
        } else {
            logger.info("Fetching users from page {}", pageToFecth);
            users = userResource.getUsers(pageToFecth);
            if(users.size() == maxUsersPerPage){
                pageToFecth++;
            }
        }
        return users;
    }
}
