CREATE TABLE games (
   id CHAR(36) PRIMARY KEY,
   factory_id VARCHAR(100) NOT NULL,
   board_size INT NOT NULL
);

CREATE TABLE game_players (
  game_id CHAR(36) NOT NULL,
  player_id CHAR(36) NOT NULL,
  player_order INT NOT NULL,

  PRIMARY KEY (game_id, player_id),
  UNIQUE (game_id, player_order),

  FOREIGN KEY (game_id)
      REFERENCES games(id)
      ON DELETE CASCADE
);

CREATE TABLE game_tokens (
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 game_id CHAR(36) NOT NULL,
 owner_id CHAR(36),
 token_name VARCHAR(100) NOT NULL,
 x INT,
 y INT,
 token_state VARCHAR(20) NOT NULL,

 FOREIGN KEY (game_id)
     REFERENCES games(id)
     ON DELETE CASCADE,

 CHECK (token_state IN ('BOARD', 'REMOVED'))
);