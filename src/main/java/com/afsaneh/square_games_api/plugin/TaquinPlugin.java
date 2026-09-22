package com.afsaneh.square_games_api.plugin;

import fr.le_campus_numerique.square_games.engine.Game;
import fr.le_campus_numerique.square_games.engine.InconsistentGameDefinitionException;
import fr.le_campus_numerique.square_games.engine.TokenPosition;
import fr.le_campus_numerique.square_games.engine.taquin.TaquinGameFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class TaquinPlugin implements GamePlugin {

    @Value("${game.taquin.default-player-count}")
    private Integer defaultPlayerCount;

    @Value("${game.taquin.default-board-size}")
    private Integer defaultBoardSize;

    private final TaquinGameFactory factory =  new TaquinGameFactory();

    private final MessageSource messageSource;

    public TaquinPlugin(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public String getName(Locale locale) {
        return messageSource.getMessage(
                "game.taquin.name",
                null,
                locale
        );
    }

    @Override
    public String getId() {
        return factory.getGameFactoryId();
    }

    @Override
    public Game createGame(Integer playerCount, Integer boardSize, UUID userId, List<UUID> opponentIds) {
        int finalPlayerCount = playerCount != null ? playerCount : defaultPlayerCount;

        int finalBoardSize = boardSize != null ? boardSize : defaultBoardSize;

        Set<UUID> playerIds = new LinkedHashSet<>();
        playerIds.add(userId);

        if (opponentIds != null) {
            playerIds.addAll(opponentIds);
        }

        if (playerIds.size() != finalPlayerCount) {
            throw new IllegalArgumentException(
                    "Expected " + finalPlayerCount + " players, but got "
                            + playerIds.size()
            );
        }

        return factory.createGame(finalBoardSize, playerIds);
    }

    @Override
    public Game restoreGame(UUID gameId, int boardSize, List<UUID> players, Collection<TokenPosition<UUID>> boardTokens, Collection<TokenPosition<UUID>> removedTokens) {
        try {
            return factory.createGameWithIds(gameId, boardSize, players, boardTokens, removedTokens);
        } catch (InconsistentGameDefinitionException e) {
            throw new IllegalStateException("Unable to restore game " + gameId, e);
        }
    }
}
