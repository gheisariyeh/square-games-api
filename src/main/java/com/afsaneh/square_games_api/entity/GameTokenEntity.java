package com.afsaneh.square_games_api.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "game_tokens_jpa")
public class GameTokenEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String ownerId;
    public String name;
    public boolean removed;
    public Integer x;
    public Integer y;
}
