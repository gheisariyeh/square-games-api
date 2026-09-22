package com.afsaneh.square_games_api.dto;

import java.util.List;
import java.util.UUID;

public class GameCreationParams {
    private String gameType;
    private Integer playerCount;
    private Integer boardSize;
    private List<UUID> opponentIds;

    public String getGameType() {
        return gameType;
    }

    public Integer getPlayerCount() {
        return playerCount;
    }

    public Integer getBoardSize() {
        return boardSize;
    }

    public List<UUID> getOpponentIds() {
        return opponentIds;
    }

    public void setGameType(String gameType) {
        this.gameType = gameType;
    }

    public void setPlayerCount(Integer playerCount) {
        this.playerCount = playerCount;
    }

    public void setBoardSize(Integer boardSize) {
        this.boardSize = boardSize;
    }
    public void setOpponentIds(List<UUID> opponentIds) {
        this.opponentIds = opponentIds;
    }
}
