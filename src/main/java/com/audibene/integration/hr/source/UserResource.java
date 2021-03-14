package com.audibene.integration.hr.source;

import com.audibene.integration.hr.source.struct.User;
import com.audibene.integration.hr.source.struct.UserRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class UserResource {

    private final static String GO_REST_API_BASE_URL = "https://gorest.co.in/public-api/";
    private final static String USER_RESOURCE = "users";
    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    public List<User> getUsers() {
        UserRequest userRequest = getUsers(1);
        return IntStream.range(2, userRequest.getMeta().getPagination().getPages() + 1)
                .mapToObj(this::getUsers)
                .map(UserRequest::getData)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    public UserRequest getUsers(int page) {
        return get(URI.create(GO_REST_API_BASE_URL + USER_RESOURCE + "?page=" + page));
    }

    private UserRequest get(URI uri) {
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
