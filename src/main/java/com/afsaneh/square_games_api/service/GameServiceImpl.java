package com.afsaneh.square_games_api.service;

import com.afsaneh.square_games_api.client.UserClient;
import com.afsaneh.square_games_api.dao.GameDao;
import com.afsaneh.square_games_api.dto.MoveInfo;
import com.afsaneh.square_games_api.plugin.GamePlugin;
import fr.le_campus_numerique.square_games.engine.CellPosition;
import fr.le_campus_numerique.square_games.engine.Game;
import fr.le_campus_numerique.square_games.engine.InvalidPositionException;
import fr.le_campus_numerique.square_games.engine.Token;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class GameServiceImpl implements GameService {

    private final List<GamePlugin> plugins;
    private final GameDao gameDao;
    private final UserClient userClient;

    public GameServiceImpl(List<GamePlugin> plugins, GameDao gameDao, UserClient userClient) {
        this.plugins = plugins;
        this.gameDao = gameDao;
        this.userClient = userClient;
    }

    private void validateUser(UUID userId) {
        if (!userClient.isValidUser(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Unknown user"
            );
        }
    }

    private Game findGame(UUID gameId) {
        return gameDao.findById(gameId)
                .orElseThrow();
    }

    @Override
    public Game createGame(String gameType, Integer playerCount, Integer boardSize, UUID userId, List<UUID> opponentIds) {
        validateUser(userId);


        if (opponentIds != null) {
            for (UUID opponentId : opponentIds) {
                validateUser(opponentId);
            }
        }

        for (GamePlugin plugin : plugins) {
            if (plugin.getId().equals(gameType)) {
                Game game = plugin.createGame(playerCount, boardSize, userId, opponentIds);

                gameDao.upsert(game);
                return game;
            }
        }
        throw new IllegalArgumentException("Unknown game type");
    }

    @Override
    public Game getGame(UUID gameId, UUID userId) {
        validateUser(userId);
        return findGame(gameId);
    }

    @Override
    public List<MoveInfo> getPossibleMoves(UUID gameId, UUID userId) {
        validateUser(userId);

        Game game = findGame(gameId);

        List<MoveInfo> possibleMoves = new ArrayList<>();

        for (Token token : game.getRemainingTokens()) {

            for (CellPosition to : token.getAllowedMoves()) {
                possibleMoves.add(new MoveInfo(null, to));
            }
        }

        for (Map.Entry<CellPosition, Token> entry : game.getBoard().entrySet()) {
            CellPosition from = entry.getKey();
            Token token = entry.getValue();

            for (CellPosition to : token.getAllowedMoves()) {
                possibleMoves.add(new MoveInfo(from, to));
            }
        }
        return possibleMoves;
    }

    @Override
    public void playMove(UUID gameId, UUID userId, CellPosition from, CellPosition to) {
        validateUser(userId);

        Game game = findGame(gameId);
        if (!userId.equals(game.getCurrentPlayerId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "It is not this player's turn");
        }

        // Le token n'est pas encore placé sur le plateau.
        if (from == null) {

            for (Token token : game.getRemainingTokens()) {

                if (token.getAllowedMoves().contains(to)) {
                    try {
                        token.moveTo(to);
                        // Sauvegarde le nouvel état du jeu dans la base de données
                        gameDao.upsert(game);
                        return;
                    } catch (InvalidPositionException e) {
                        throw new IllegalArgumentException("Move not allowed", e);
                    }
                }
            }

            throw new IllegalArgumentException("Move not allowed");
        }

        // Le token est déjà placé sur le plateau.
        Token token = game.getBoard().get(from);

        if (token == null) {
            throw new IllegalArgumentException("No token found at source position");
        }

        if (!token.getAllowedMoves().contains(to)) {
            throw new IllegalArgumentException("Move not allowed");
        }

        try {
            token.moveTo(to);

            // Sauvegarde le nouvel état du jeu dans la base de données
            gameDao.upsert(game);

        } catch (InvalidPositionException e) {
            throw new IllegalArgumentException("Move not allowed", e);
        }
    }

    @Override
    public List<Game> getAllGames(UUID userId) {
        validateUser(userId);
        return gameDao.findAll().filter(game -> game.getPlayerIds().contains(userId)).toList();
    }
}
