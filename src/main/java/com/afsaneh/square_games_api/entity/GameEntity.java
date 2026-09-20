package com.afsaneh.square_games_api.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "games_jpa")
public class GameEntity {
    @Id
    public String id;
    public String factoryId;
    public int boardSize;
    public String playerIds;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "game_id")
    public List<GameTokenEntity> tokens;
}
