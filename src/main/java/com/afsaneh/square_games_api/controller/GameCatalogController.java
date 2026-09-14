package com.afsaneh.square_games_api.controller;

import com.afsaneh.square_games_api.dto.GameInfo;
import com.afsaneh.square_games_api.service.GameCatalog;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Locale;

@RestController
public class GameCatalogController {

    private final GameCatalog gameCatalog;

    public GameCatalogController(GameCatalog gameCatalog) {
        this.gameCatalog = gameCatalog;
    }

    @GetMapping("/game")
    public Collection<GameInfo> getGames(Locale locale) {
        return gameCatalog.getGames(locale);
    }
}
