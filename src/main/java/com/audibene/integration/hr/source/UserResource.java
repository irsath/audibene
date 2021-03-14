package com.audibene.integration.hr.source;

import com.audibene.integration.hr.ApplicationProperties;
import com.audibene.integration.hr.source.struct.User;
import com.audibene.integration.hr.source.struct.UserRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class UserResource {
    private final static Logger logger = LoggerFactory.getLogger(UserResource.class);

    private final String GO_REST_API_BASE_URL = ApplicationProperties.get("go.rest.api.base.url");
    private final String USER_RESOURCE = ApplicationProperties.get("go.rest.api.resource.user");
    private final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    public int pagesCount(){
        UserRequest userRequest = get(URI.create(GO_REST_API_BASE_URL + USER_RESOURCE + "?page=" + 1));
        logger.debug("Found {} of pages for user resource", userRequest.getMeta().getPagination().getPages());
        return userRequest.getMeta().getPagination().getPages();
    }

    public int usersPerPage(){
        UserRequest userRequest = get(URI.create(GO_REST_API_BASE_URL + USER_RESOURCE + "?page=" + 1));
        logger.debug("Found {} of pages for user resource", userRequest.getMeta().getPagination().getPages());
        return userRequest.getMeta().getPagination().getLimit();
    }

    public List<User> getUsers(int page) {
        logger.debug("Fetching users from page {}", page);
        return get(URI.create(GO_REST_API_BASE_URL + USER_RESOURCE + "?page=" + page)).getData();
    }

    private UserRequest get(URI uri) {
        logger.debug("Request url {}", uri.toString());
        HttpRequest request = HttpRequest.newBuilder(uri)
                .header("Accept", "application/json")
                .build();
        try {
            HttpResponse<UserRequest> userGetReq =
                    HttpClient.newHttpClient()
                            .send(request, getUserRequestBodyHandler());

            if (userGetReq.statusCode() != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("Status != 200");
            }
            return userGetReq.body();
        } catch (IOException | InterruptedException e) {
            logger.error("Error while fetching {}", uri);
            throw new RuntimeException("Error while fetching " + uri);
        }
    }

    private HttpResponse.BodyHandler<UserRequest> getUserRequestBodyHandler() {
        return responseInfo -> {
            HttpResponse.BodySubscriber<String> upstream = HttpResponse.BodySubscribers.ofString(StandardCharsets.UTF_8);

            return HttpResponse.BodySubscribers.mapping(
                    upstream,
                    (String body) -> {
                        try {
                            return OBJECT_MAPPER.readValue(body, UserRequest.class);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        };
    }
}
