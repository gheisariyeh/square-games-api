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
    public String createGame(@RequestHeader("X-UserId") UUID userId, @RequestBody GameCreationParams params) {

        Game game = gameService.createGame(
                params.getGameType(),
                params.getPlayerCount(),
                params.getBoardSize(),
                userId,
                params.getOpponentIds()
        );
        return game.getId().toString();
    }

    @GetMapping("/{gameId}")
    public Game getGame( @PathVariable UUID gameId, @RequestHeader("X-UserId") UUID userId) {
        return gameService.getGame(gameId, userId);
    }

    @GetMapping("/{gameId}/moves")
    public List<MoveInfo> getPossibleMoves( @RequestHeader("X-UserId") UUID userId, @PathVariable UUID gameId) {
        return gameService.getPossibleMoves(gameId, userId);
    }

    @PostMapping("/{gameId}/moves")
    public Game playMove( @RequestHeader("X-UserId") UUID userId, @PathVariable UUID gameId, @RequestBody MoveParams params) {
        gameService.playMove(gameId, userId, params.getFrom(), params.getTo());
        return gameService.getGame(gameId, userId);
    }

    @GetMapping
    public List<Game> getAllGames( @RequestHeader("X-UserId") UUID userId) {
        return gameService.getAllGames(userId);
    }
}