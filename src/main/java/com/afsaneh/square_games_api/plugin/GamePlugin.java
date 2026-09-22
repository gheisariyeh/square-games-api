package com.afsaneh.square_games_api.plugin;

import fr.le_campus_numerique.square_games.engine.Game;
import fr.le_campus_numerique.square_games.engine.TokenPosition;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public interface GamePlugin {
    String getName(Locale locale);

    String getId();

    Game createGame(Integer playerCount, Integer boardSize, UUID userId, List<UUID> opponentIds);

    Game restoreGame(
            UUID gameId,
            int boardSize,
            List<UUID> players,
            Collection<TokenPosition<UUID>> boardTokens,
            Collection<TokenPosition<UUID>> removedTokens
    );
}
