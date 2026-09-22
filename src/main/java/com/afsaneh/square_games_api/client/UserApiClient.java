package com.afsaneh.square_games_api.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
public class UserApiClient implements UserClient {
    private final RestClient restClient;

    public UserApiClient(@Value("${users.api.url}") String usersApiUrl) {

        this.restClient = RestClient.builder().baseUrl(usersApiUrl).build();
    }

    @Override
    public boolean isValidUser(UUID userId) {

        Boolean valid = restClient.get()
                .uri("/users/{id}/valid", userId)
                .retrieve()
                .body(Boolean.class);

        return Boolean.TRUE.equals(valid);
    }
}
