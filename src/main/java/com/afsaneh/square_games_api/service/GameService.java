package com.afsaneh.square_games_api.service;

import com.afsaneh.square_games_api.dto.MoveInfo;
import fr.le_campus_numerique.square_games.engine.CellPosition;
import fr.le_campus_numerique.square_games.engine.Game;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;
import java.util.UUID;

public interface GameService {
    Game createGame(String gameType, Integer playerCount, Integer boardSize, UUID userId, List<UUID> opponentIds);

    Game getGame(@RequestHeader("X-UserId") UUID userId, UUID gameId);

    List<MoveInfo> getPossibleMoves(UUID gameId, UUID userId);

    void playMove(UUID gameId, UUID userId, CellPosition from, CellPosition to);

    List<Game> getAllGames(UUID userId);
}
