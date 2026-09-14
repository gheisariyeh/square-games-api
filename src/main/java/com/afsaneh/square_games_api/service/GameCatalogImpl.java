package com.afsaneh.square_games_api.service;

import com.afsaneh.square_games_api.dto.GameInfo;
import com.afsaneh.square_games_api.plugin.GamePlugin;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

@Service
public class GameCatalogImpl implements GameCatalog {

    private final List<GamePlugin> plugins;

    public GameCatalogImpl(List<GamePlugin> plugins) {
        this.plugins = plugins;
    }

    @Override
    public Collection<GameInfo> getGames(Locale locale) {
        List<GameInfo> games = new ArrayList<>();
        for (GamePlugin plugin : plugins) {
            GameInfo gameInfo = new GameInfo(plugin.getId(), plugin.getName(locale));
            games.add(gameInfo);
        }
        return games;
    }
}