package com.afsaneh.square_games_api.service;

import com.afsaneh.square_games_api.dto.GameInfo;

import java.util.Collection;
import java.util.Locale;

public interface GameCatalog {
    Collection<GameInfo> getGames(Locale locale);

}
