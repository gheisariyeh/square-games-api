package com.afsaneh.square_games_api.plugin;

import fr.le_campus_numerique.square_games.engine.Game;
import fr.le_campus_numerique.square_games.engine.connectfour.ConnectFourGameFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class ConnectFourPlugin implements GamePlugin {

    @Value("${game.connect4.default-player-count}")
    private Integer defaultPlayerCount;

    @Value("${game.connect4.default-board-size}")
    private Integer defaultBoardSize;

    private final ConnectFourGameFactory factory = new ConnectFourGameFactory();

    private final MessageSource messageSource;

    public ConnectFourPlugin(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public String getName(Locale locale) {
        return messageSource.getMessage(
                "game.connect4.name",
                null,
                locale
        );
    }

    @Override
    public String getId() {
        return factory.getGameFactoryId();
    }

    @Override
    public Game createGame(Integer playerCount, Integer boardSize) {
        int finalPlayerCount = playerCount != null ? playerCount : defaultPlayerCount;

        int finalBoardSize = boardSize != null ? boardSize : defaultBoardSize;

        return factory.createGame(finalPlayerCount, finalBoardSize);    }
}
