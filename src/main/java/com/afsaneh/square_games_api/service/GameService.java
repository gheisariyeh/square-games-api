package com.afsaneh.square_games_api.service;

import com.afsaneh.square_games_api.dto.MoveInfo;
import fr.le_campus_numerique.square_games.engine.CellPosition;
import fr.le_campus_numerique.square_games.engine.Game;

import java.util.List;
import java.util.UUID;

public interface GameService {
    Game createGame(String gameType, Integer playerCount, Integer boardSize);

    Game getGame(UUID gameId);

    List<MoveInfo> getPossibleMoves(UUID gameId);

    void playMove(UUID gameId, CellPosition from, CellPosition to);

    List<Game> getAllGames();
}
