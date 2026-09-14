package com.afsaneh.square_games_api.dto;

public class GameCreationParams {
    String gameType;
    Integer playerCount;
    Integer boardSize;

    public String getGameType() {
        return gameType;
    }

    public Integer getPlayerCount() {
        return playerCount;
    }

    public Integer getBoardSize() {
        return boardSize;
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
}
