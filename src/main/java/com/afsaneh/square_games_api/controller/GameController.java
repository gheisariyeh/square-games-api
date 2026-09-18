package com.afsaneh.square_games_api.controller;

import com.afsaneh.square_games_api.dto.GameCreationParams;
import com.afsaneh.square_games_api.dto.MoveInfo;
import com.afsaneh.square_games_api.service.GameService;
import com.afsaneh.square_games_api.dto.MoveParams;
import fr.le_campus_numerique.square_games.engine.Game;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping
    public String createGame(@RequestBody GameCreationParams params) {
        Game game = gameService.createGame(params.getGameType(), params.getPlayerCount(), params.getBoardSize());
        return game.getId().toString();
    }

    @GetMapping("/{gameId}")
    public Game getGame(@PathVariable UUID gameId) {
        return gameService.getGame(gameId);
    }

    @GetMapping("/{gameId}/moves")
    public List<MoveInfo> getPossibleMoves(@PathVariable UUID gameId) {
        return gameService.getPossibleMoves(gameId);
    }

    @PostMapping("/{gameId}/moves")
    public Game playMove(@PathVariable UUID gameId, @RequestBody MoveParams params) {
        gameService.playMove(gameId, params.getFrom(), params.getTo());
        return gameService.getGame(gameId);
    }

    @GetMapping
    public List<Game> getAllGames() {
        return gameService.getAllGames();
    }
}