package com.afsaneh.square_games_api.dto;

public class GameInfo {

    private String id;
    private String name;

    public GameInfo(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}