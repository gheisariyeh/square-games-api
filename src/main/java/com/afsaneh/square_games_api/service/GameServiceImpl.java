package com.afsaneh.square_games_api.service;

import com.afsaneh.square_games_api.dao.GameDao;
import com.afsaneh.square_games_api.dto.MoveInfo;
import com.afsaneh.square_games_api.plugin.GamePlugin;
import fr.le_campus_numerique.square_games.engine.CellPosition;
import fr.le_campus_numerique.square_games.engine.Game;
import fr.le_campus_numerique.square_games.engine.InvalidPositionException;
import fr.le_campus_numerique.square_games.engine.Token;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GameServiceImpl implements GameService {

    private final List<GamePlugin> plugins;
//    private final Map<UUID, Game> games = new HashMap<>();
    private final GameDao gameDao;

    public  GameServiceImpl(List<GamePlugin> plugins, GameDao gameDao) {
        this.plugins = plugins;
        this.gameDao = gameDao;
    }

    @Override
    public Game createGame(String gameType, Integer playerCount, Integer boardSize) {

        for (GamePlugin plugin : plugins) {
            if (plugin.getId().equals(gameType)) {
                Game game = plugin.createGame(playerCount, boardSize);
                //games.put(game.getId(), game);
                gameDao.upsert(game);
                return game;
            }
        }
        throw new IllegalArgumentException("Unknown game type");
    }

    @Override
    public Game getGame(UUID gameId) {
        return gameDao.findById(gameId).orElseThrow();
    }

    @Override
    public List<MoveInfo> getPossibleMoves(UUID gameId) {

        Game game = getGame(gameId);

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
    public void playMove(UUID gameId, CellPosition from, CellPosition to) {

        Game game = getGame(gameId);

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
    public List<Game> getAllGames() {
        return gameDao.findAll().toList();
    }
}
