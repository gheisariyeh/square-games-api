package com.afsaneh.square_games_api.service;

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
    private final Map<UUID, Game> games = new HashMap<>();

    public GameServiceImpl(List<GamePlugin> plugins) {
        this.plugins = plugins;
    }

    @Override
    public Game createGame(String gameType, Integer playerCount, Integer boardSize) {

        for (GamePlugin plugin : plugins) {
            if (plugin.getId().equals(gameType)) {
                Game game = plugin.createGame(playerCount, boardSize);
                games.put(game.getId(), game);
                return game;
            }
        }
        throw new IllegalArgumentException("Unknown game type");
    }

    @Override
    public Game getGame(UUID gameId) {
        return games.get(gameId);
    }

    @Override
    public List<MoveInfo> getPossibleMoves(UUID gameId) {

        Game game = getGame(gameId);

        if (game == null) {
            throw new IllegalArgumentException("Game not found");
        }

        List<MoveInfo> possibleMoves = new ArrayList<>();

        for (Token token : game.getRemainingTokens()) {

            for (CellPosition to : token.getAllowedMoves()) {

                possibleMoves.add(
                        new MoveInfo(null, to)
                );
            }
        }

        for (Map.Entry<CellPosition, Token> entry
                : game.getBoard().entrySet()) {

            CellPosition from = entry.getKey();
            Token token = entry.getValue();

            for (CellPosition to : token.getAllowedMoves()) {

                possibleMoves.add(
                        new MoveInfo(from, to)
                );
            }
        }

        return possibleMoves;
    }

    @Override
    public void playMove(UUID gameId, CellPosition from, CellPosition to) {
        Game game = getGame(gameId);
        if (game == null) {
            throw new IllegalArgumentException("Game not found");
        }

        // Token is not on the Board yet
        if (from == null) {
            for (Token token : game.getRemainingTokens()) {
                if (token.getAllowedMoves().contains(to)) {
                    try {
                        token.moveTo(to);
                        return;
                    } catch (InvalidPositionException e) {
                        throw new IllegalArgumentException("Move not allowed", e);
                    }
                }
            }
            throw new IllegalArgumentException("Move not allowed");
        }

        // Token is already on the Board
        Token token = game.getBoard().get(from);

        if (token == null) {
            throw new IllegalArgumentException("No token found at source position");
        }

        if (!token.getAllowedMoves().contains(to)) {
            throw new IllegalArgumentException("Move not allowed");
        }

        try {
            token.moveTo(to);
        } catch (InvalidPositionException e) {
            throw new IllegalArgumentException("Move not allowed", e);
        }
    }
}
