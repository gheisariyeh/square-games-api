package com.afsaneh.square_games_api.dao;

import com.afsaneh.square_games_api.plugin.GamePlugin;
import fr.le_campus_numerique.square_games.engine.CellPosition;
import fr.le_campus_numerique.square_games.engine.Game;
import fr.le_campus_numerique.square_games.engine.Token;
import fr.le_campus_numerique.square_games.engine.TokenPosition;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Primary
@Repository
public class JdbcGameDao implements GameDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final List<GamePlugin> plugins;

    public JdbcGameDao(NamedParameterJdbcTemplate jdbcTemplate, List<GamePlugin> plugins) {
        this.jdbcTemplate = jdbcTemplate;
        this.plugins = plugins;
    }

    private record GameData(String factoryId, int boardSize) {
    }

    @Override
    public Stream<Game> findAll() {

        String sql = """
            SELECT id
            FROM games
            """;

        // Récupère les identifiants de tous les jeux
        List<UUID> gameIds = jdbcTemplate.query(sql, (rs, rowNum) -> UUID.fromString(rs.getString("id")));

        // Reconstruit chaque jeu à partir de son identifiant
        return gameIds.stream()
                .map(this::findById)
                .flatMap(Optional::stream);
    }

    @Override
    public Optional<Game> findById(UUID gameId) {

        MapSqlParameterSource params =
                new MapSqlParameterSource("gameId", gameId.toString());

        // 1. Lire les informations principales du jeu
        String gameSql = """
            SELECT factory_id, board_size
            FROM games
            WHERE id = :gameId
            """;

        List<GameData> games = jdbcTemplate.query(gameSql, params,
                (rs, rowNum) -> new GameData(
                        rs.getString("factory_id"),
                        rs.getInt("board_size")
                )
        );

        // Si le jeu n'existe pas
        if (games.isEmpty()) {
            return Optional.empty();
        }

        GameData gameData = games.getFirst();

        // 2. Lire les joueurs dans le bon ordre
        String playersSql = """
            SELECT player_id
            FROM game_players
            WHERE game_id = :gameId
            ORDER BY player_order
            """;

        List<UUID> players = jdbcTemplate.query(playersSql, params,
                (rs, rowNum) ->
                        UUID.fromString(rs.getString("player_id"))
        );

        // 3. Lire les tokens présents sur le plateau
        String boardTokensSql = """
            SELECT owner_id, token_name, x, y
            FROM game_tokens
            WHERE game_id = :gameId
              AND token_state = 'BOARD'
            """;

        List<TokenPosition<UUID>> boardTokens =
                jdbcTemplate.query(boardTokensSql, params,
                        (rs, rowNum) -> {
                            String owner =
                                    rs.getString("owner_id");

                            UUID ownerId =
                                    owner != null
                                            ? UUID.fromString(owner)
                                            : null;

                            return new TokenPosition<>(
                                    ownerId,
                                    rs.getString("token_name"),
                                    rs.getInt("x"),
                                    rs.getInt("y")
                            );
                        }
                );

        // 4. Lire les tokens supprimés
        String removedTokensSql = """
            SELECT owner_id, token_name
            FROM game_tokens
            WHERE game_id = :gameId
              AND token_state = 'REMOVED'
            """;

        List<TokenPosition<UUID>> removedTokens = jdbcTemplate.query(removedTokensSql, params, (rs, rowNum) -> {
            String owner = rs.getString("owner_id");
            UUID ownerId = owner != null ? UUID.fromString(owner) : null;
            return new TokenPosition<>(ownerId, rs.getString("token_name"), 0, 0);
        }
        );

        // 5. Trouver le plugin correspondant au factory_id
        GamePlugin plugin = plugins.stream().filter(p -> p.getId().equals(gameData.factoryId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No plugin found for factory " + gameData.factoryId()));

        // 6. Reconstruire le Game
        Game game = plugin.restoreGame(gameId, gameData.boardSize(), players, boardTokens, removedTokens);

        return Optional.of(game);
    }

    @Transactional
    @Override
    public Game upsert(Game game) {

        String sql = """
            INSERT INTO games (id, factory_id, board_size)
            VALUES (:id, :factoryId, :boardSize)
            ON DUPLICATE KEY UPDATE
                factory_id = :factoryId,
                board_size = :boardSize
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", game.getId().toString())
                .addValue("factoryId", game.getFactoryId())
                .addValue("boardSize", game.getBoardSize());

        jdbcTemplate.update(sql, params);

        String deletePlayersSql = """
        DELETE FROM game_players
        WHERE game_id = :gameId
        """;

        jdbcTemplate.update(deletePlayersSql, new MapSqlParameterSource("gameId", game.getId().toString()));

        String insertPlayerSql = """
        INSERT INTO game_players (
            game_id,
            player_id,
            player_order
        )
        VALUES (
            :gameId,
            :playerId,
            :playerOrder
        )
        """;

        int order = 0;

        for (UUID playerId : game.getPlayerIds()) {

            MapSqlParameterSource playerParams = new MapSqlParameterSource()
                            .addValue("gameId", game.getId().toString())
                            .addValue("playerId", playerId.toString())
                            .addValue("playerOrder", order);

            jdbcTemplate.update(insertPlayerSql, playerParams);

            order++;
        }

        String deleteTokensSql = """
        DELETE FROM game_tokens
        WHERE game_id = :gameId
        """;

        jdbcTemplate.update(deleteTokensSql, new MapSqlParameterSource("gameId", game.getId().toString()));

        String insertTokenSql = """
        INSERT INTO game_tokens (
            game_id,
            owner_id,
            token_name,
            x,
            y,
            token_state
        )
        VALUES (
            :gameId,
            :ownerId,
            :tokenName,
            :x,
            :y,
            :tokenState
        )
        """;
        for (Map.Entry<CellPosition, Token> entry : game.getBoard().entrySet()) {

            CellPosition position = entry.getKey();
            Token token = entry.getValue();

            String ownerId = token.getOwnerId()
                    .map(UUID::toString)
                    .orElse(null);

            MapSqlParameterSource tokenParams = new MapSqlParameterSource()
                            .addValue("gameId", game.getId().toString())
                            .addValue("ownerId", ownerId)
                            .addValue("tokenName", token.getName())
                            .addValue("x", position.x())
                            .addValue("y", position.y())
                            .addValue("tokenState", "BOARD");

            jdbcTemplate.update(insertTokenSql, tokenParams);
        }

        for (Token token : game.getRemovedTokens()) {

            String ownerId = token.getOwnerId()
                    .map(UUID::toString)
                    .orElse(null);

            MapSqlParameterSource tokenParams = new MapSqlParameterSource()
                            .addValue("gameId", game.getId().toString())
                            .addValue("ownerId", ownerId)
                            .addValue("tokenName", token.getName())
                            .addValue("x", null)
                            .addValue("y", null)
                            .addValue("tokenState", "REMOVED");

            jdbcTemplate.update(insertTokenSql, tokenParams);
        }
        return game;
    }

    @Override
    public void delete(UUID gameId) {
        String sql = """
            DELETE FROM games
            WHERE id = :id
            """;

        MapSqlParameterSource params = new MapSqlParameterSource("id", gameId.toString());

        jdbcTemplate.update(sql, params);

    }
}
