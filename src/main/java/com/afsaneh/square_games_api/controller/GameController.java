package com.afsaneh.square_games_api.controller;

import com.afsaneh.square_games_api.dto.GameCreationParams;
import com.afsaneh.square_games_api.dto.MoveInfo;
import com.afsaneh.square_games_api.service.GameService;
import com.afsaneh.square_games_api.dto.MoveParams;
import fr.le_campus_numerique.square_games.engine.Game;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(
        name = "Games",
        description = "Operations related to board games"
)
@RestController
@RequestMapping("/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @Operation(
            summary = "Create a new game",
            description = "Creates a new board game for the requesting user and the specified opponents"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Game created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid game parameters"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unknown user"
            )
    })
    @PostMapping
    public String createGame(
            @Parameter(
            description = "Identifier of the user creating the game",
            required = true
    )@RequestHeader("X-UserId") UUID userId, @RequestBody GameCreationParams params) {

        Game game = gameService.createGame(
                params.getGameType(),
                params.getPlayerCount(),
                params.getBoardSize(),
                userId,
                params.getOpponentIds()
        );
        return game.getId().toString();
    }

    @Operation(
            summary = "Get a game",
            description = "Returns a game identified by its ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Game returned successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unknown user"
            )
    })
    @GetMapping("/{gameId}")
    public Game getGame(
            @Parameter(
                    description = "Identifier of the game",
                    required = true
    ) @PathVariable UUID gameId,
            @Parameter(
                    description = "Identifier of the user sending the request",
                    required = true
            )
            @RequestHeader("X-UserId") UUID userId) {
        return gameService.getGame(gameId, userId);
    }

    @Operation(
            summary = "Get possible moves",
            description = "Returns all possible moves for the current state of a game"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Possible moves returned successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unknown user"
            )
    })
    @GetMapping("/{gameId}/moves")
    public List<MoveInfo> getPossibleMoves(
            @Parameter(
                    description = "Identifier of the user sending the request",
                    required = true
    )@RequestHeader("X-UserId") UUID userId,
            @Parameter(
                    description = "Identifier of the game",
                    required = true
            )
            @PathVariable UUID gameId) {
        return gameService.getPossibleMoves(gameId, userId);
    }

    @Operation(
            summary = "Play a move",
            description = "Plays a move in a game for the user identified by X-UserId"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Move played successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid move"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unknown user"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "It is not this player's turn"
            )
    })
    @PostMapping("/{gameId}/moves")
    public Game playMove(
            @Parameter(
                    description = "Identifier of the user playing the move",
                    required = true
    )@RequestHeader("X-UserId") UUID userId,
            @Parameter(
                    description = "Identifier of the game",
                    required = true
            )
            @PathVariable UUID gameId, @RequestBody MoveParams params) {
        gameService.playMove(gameId, userId, params.getFrom(), params.getTo());
        return gameService.getGame(gameId, userId);
    }

    @Operation(
            summary = "Get user's games",
            description = "Returns all games in which the user participates"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Games returned successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unknown user"
            )
    })
    @GetMapping
    public List<Game> getAllGames(
            @Parameter(
                    description = "Identifier of the user sending the request",
                    required = true
    )@RequestHeader("X-UserId") UUID userId) {
        return gameService.getAllGames(userId);
    }
}