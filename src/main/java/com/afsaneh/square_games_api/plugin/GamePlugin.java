package com.afsaneh.square_games_api.plugin;

import fr.le_campus_numerique.square_games.engine.Game;

import java.util.Locale;

public interface GamePlugin {
    String getName(Locale locale);

    String getId();

    Game createGame(Integer playerCount, Integer boardSize);
}
