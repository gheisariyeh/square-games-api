package com.afsaneh.square_games_api.client;

import java.util.UUID;

public interface UserClient {
    boolean isValidUser(UUID userId);
}
