package com.afsaneh.square_games_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

public class GameCreationParams {

    @Schema(
            description = "Type of board game",
            example = "tictactoe"
    )
    private String gameType;

    @Schema(
            description = "Number of players",
            example = "2"
    )
    private Integer playerCount;

    @Schema(
            description = "Size of the game board",
            example = "3"
    )
    private Integer boardSize;

    @Schema(
            description = "Identifiers of the opponent players"
    )
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
