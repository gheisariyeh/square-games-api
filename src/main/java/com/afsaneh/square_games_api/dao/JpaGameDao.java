package com.afsaneh.square_games_api.dao;

import com.afsaneh.square_games_api.entity.GameEntity;
import com.afsaneh.square_games_api.entity.GameTokenEntity;
import com.afsaneh.square_games_api.plugin.GamePlugin;
import com.afsaneh.square_games_api.repository.GameEntityRepository;
import fr.le_campus_numerique.square_games.engine.CellPosition;
import fr.le_campus_numerique.square_games.engine.Game;
import fr.le_campus_numerique.square_games.engine.Token;
import fr.le_campus_numerique.square_games.engine.TokenPosition;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Primary
@Repository
public class JpaGameDao implements GameDao {

    private final GameEntityRepository gameEntityRepository;
    private final List<GamePlugin> plugins;

    public JpaGameDao(GameEntityRepository gameEntityRepository, List<GamePlugin> plugins) {
        this.gameEntityRepository = gameEntityRepository;
        this.plugins = plugins;
    }

    private GameEntity toEntity(Game game) {

        GameEntity entity = new GameEntity();

        // Convertit les informations générales du jeu
        entity.id = game.getId().toString();
        entity.factoryId = game.getFactoryId();
        entity.boardSize = game.getBoardSize();

        // Convertit les identifiants des joueurs en une seule chaîne
        entity.playerIds = game.getPlayerIds().stream()
                .map(UUID::toString)
                .collect(Collectors.joining(","));

        entity.tokens = new ArrayList<>();

        // Convertit les tokens présents sur le plateau
        for (Map.Entry<CellPosition, Token> entry : game.getBoard().entrySet()) {

            CellPosition position = entry.getKey();
            Token token = entry.getValue();

            GameTokenEntity tokenEntity = new GameTokenEntity();

            tokenEntity.ownerId = token.getOwnerId()
                    .map(UUID::toString)
                    .orElse(null);

            tokenEntity.name = token.getName();
            tokenEntity.removed = false;
            tokenEntity.x = position.x();
            tokenEntity.y = position.y();

            entity.tokens.add(tokenEntity);
        }

        // Convertit les tokens retirés du jeu
        for (Token token : game.getRemovedTokens()) {

            GameTokenEntity tokenEntity = new GameTokenEntity();

            tokenEntity.ownerId = token.getOwnerId()
                    .map(UUID::toString)
                    .orElse(null);

            tokenEntity.name = token.getName();
            tokenEntity.removed = true;
            tokenEntity.x = null;
            tokenEntity.y = null;

            entity.tokens.add(tokenEntity);
        }

        return entity;
    }

    private Game toGame(GameEntity gameEntity) {

        // Récupère les informations générales du jeu depuis l'entité
        UUID gameId = UUID.fromString(gameEntity.id);
        String factoryId = gameEntity.factoryId;
        int boardSize = gameEntity.boardSize;

        // Convertit la chaîne des identifiants des joueurs en liste de UUID
        List<UUID> players = Arrays.stream(gameEntity.playerIds.split(","))
                .map(UUID::fromString)
                .toList();

        // Convertit les tokens présents sur le plateau
        List<TokenPosition<UUID>> boardTokens = gameEntity.tokens.stream()
                .filter(token -> !token.removed)
                .map(token -> new TokenPosition<>(
                        token.ownerId != null
                                ? UUID.fromString(token.ownerId)
                                : null,
                        token.name,
                        token.x,
                        token.y
                ))
                .toList();

        // Reconstruit les TokenPosition correspondant aux tokens retirés
        List<TokenPosition<UUID>> removedTokens = gameEntity.tokens.stream()
                .filter(token -> token.removed)
                .map(token -> new TokenPosition<>(
                        token.ownerId != null
                                ? UUID.fromString(token.ownerId)
                                : null,
                        token.name,
                        0,
                        0
                ))
                .toList();

        // Recherche le plugin correspondant au type de jeu
        GamePlugin plugin = plugins.stream()
                .filter(p -> p.getId().equals(factoryId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No plugin found for factory " + factoryId
                ));

        // Reconstruit le jeu à partir des données persistées
        return plugin.restoreGame(
                gameId,
                boardSize,
                players,
                boardTokens,
                removedTokens
        );
    }

    @Override
    public Stream<Game> findAll() {
        return gameEntityRepository.findAll()
                .stream()
                .map(this::toGame);
    }

    @Override
    public Optional<Game> findById(UUID gameId) {
        return gameEntityRepository.findById(gameId.toString())
                .map(this::toGame);
    }

    @Override
    public Game upsert(Game game) {
        GameEntity entity = toEntity(game);
        gameEntityRepository.save(entity);
        return game;
    }

    @Override
    public void delete(UUID gameId) {
        gameEntityRepository.deleteById(gameId.toString());
    }
}
