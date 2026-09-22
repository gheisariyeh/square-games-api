# Square Games API

Spring Boot REST API for creating and playing board games. The application supports several game plugins, persistent game storage, player identification through the `X-UserId` HTTP header, validation against a separate Users API, and OpenAPI/Swagger documentation.

This project is part of the **Square Games** training project developed at *Le Campus Numérique in the Alps*.

## Features

- Create board games with real application users
- Support multiple game types through a plugin architecture
- Persist games and tokens with Spring Data JPA
- H2 persistence by default, with a MySQL profile also available
- Require `X-UserId` on game requests
- Validate users through the separate `square-users-api` service using Spring `RestClient`
- Filter the game list by participating user
- Enforce player turns and return `403 Forbidden` when the requesting user is not the current player
- Document and manually test the API with Swagger UI / OpenAPI
- Expose a game catalog and heartbeat endpoint

## Tech stack

- Java 21
- Spring Boot 4.2.0-SNAPSHOT
- Spring Web MVC
- Spring Data JPA
- Spring JDBC
- H2 Database
- MySQL Driver
- Spring `RestClient`
- springdoc-openapi / Swagger UI
- Maven
- Square Games Engine `1.0-SNAPSHOT` provided by the Campus

## Architecture

The application follows a layered architecture and isolates external-service communication behind a client abstraction.

```text
HTTP Client
    |
    v
Controller
    |
    v
Service
   / \
  v   v
DAO   UserClient
 |       |
 v       v
JPA   RestClient
 |       |
 v       v
DB    Users API :8081
```

Main packages:

```text
com.afsaneh.square_games_api
├── client       # Communication with square-users-api
├── controller   # REST endpoints
├── dao          # Persistence abstraction and implementations
├── dto          # Request/response objects
├── entity       # JPA persistence entities
├── heartbeat    # Heartbeat service
├── plugin       # Game plugins
├── repository   # Spring Data JPA repositories
└── service      # Application and game logic
```

## Prerequisites

- JDK 21
- Maven, or the included Maven Wrapper
- Access to the Campus private Square Games engine artifact:

```text
fr.le-campus-numerique.square-games:engine:1.0-SNAPSHOT
```

Maven must already be configured according to the Campus instructions so that this private dependency can be resolved. Do not commit access tokens or credentials to the repository.

For user-aware game operations, the companion **`square-users-api`** should be running on:

```text
http://localhost:8081
```

Its URL is configured through:

```properties
users.api.url=http://localhost:8081
```

## Run the application

The default Spring profile is `h2`, so no external database is required for the standard local setup.

### Windows

```powershell
.\mvnw.cmd spring-boot:run
```

### macOS / Linux

```bash
./mvnw spring-boot:run
```

The Games API uses Spring Boot's default port:

```text
http://localhost:8080
```

## Database profiles

### H2 — default

The active profile is configured in `application.properties`:

```properties
spring.profiles.active=h2
```

The file-based database is stored locally at:

```text
./data/square_games
```

The `data/` directory and H2 database files are ignored by Git.

H2 Console:

```text
http://localhost:8080/h2-console
```

Connection settings:

```text
JDBC URL: jdbc:h2:file:./data/square_games
User:     sa
Password: <empty>
```

### MySQL — optional profile

A MySQL configuration is also provided in `application-mysql.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3310/square_games
spring.datasource.username=root
spring.datasource.password=root
spring.jpa.hibernate.ddl-auto=update
```

Activate the `mysql` profile instead of `h2` when using the configured local MySQL instance.

## Users API integration

Game requests use a custom HTTP header:

```http
X-UserId: <user-uuid>
```

The Games API validates this identifier by calling:

```http
GET http://localhost:8081/users/{id}/valid
```

The integration is implemented through:

```text
GameServiceImpl
    ↓
UserClient
    ↓
UserApiClient
    ↓
Spring RestClient
    ↓
square-users-api
```

If a supplied user does not exist, the Games API returns `401 Unauthorized`.

> **Security note:** `X-UserId` is intentionally used for the training iteration and is not secure authentication. A client can forge this header. A token-based mechanism such as JWT is the intended next step.

## Game endpoints

All `/games` endpoints require a valid `X-UserId` header.

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/games` | Create a game for the requesting user and specified opponents |
| `GET` | `/games` | List only games in which the requesting user participates |
| `GET` | `/games/{gameId}` | Retrieve a game by ID |
| `GET` | `/games/{gameId}/moves` | List possible moves for the current game state |
| `POST` | `/games/{gameId}/moves` | Play a move |

### Create a game

```http
POST /games
X-UserId: <user-1-uuid>
Content-Type: application/json
```

Example body:

```json
{
  "gameType": "tictactoe",
  "playerCount": 2,
  "boardSize": 3,
  "opponentIds": [
    "<user-2-uuid>"
  ]
}
```

The requesting user and all opponent IDs are validated against the Users API before the game is created.

The response is the generated game UUID.

### List the requesting user's games

```http
GET /games
X-UserId: <user-uuid>
```

Only games whose `playerIds` contain the requesting user are returned.

### Get possible moves

```http
GET /games/{gameId}/moves
X-UserId: <user-uuid>
```

### Play a move

```http
POST /games/{gameId}/moves
X-UserId: <current-player-uuid>
Content-Type: application/json
```

Example when placing a token that is not yet on the board:

```json
{
  "from": null,
  "to": {
    "x": 0,
    "y": 0
  }
}
```

If `X-UserId` does not match the game's `currentPlayerId`, the API returns:

```text
403 Forbidden
```

with the message:

```text
It is not this player's turn
```

## Additional endpoints

### Game catalog

```http
GET /game
```

Returns the available game types exposed by the registered game plugins.

### Heartbeat

```http
GET /heartbeat
```

Returns a generated heartbeat value.

## Swagger / OpenAPI

Once the application is running:

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

Swagger UI can also be used for manual endpoint testing by entering the required `X-UserId`, path parameters, and request bodies and then selecting **Execute**.

## Example end-to-end test scenario

For a complete multiplayer integration test:

```text
Users API :8081
     ↓
Create User 1
     ↓
Create User 2
     ↓
Store user1Id / user2Id
     ↓
Validate users

Games API :8080
     ↓
Create a game with User 1 + User 2
     ↓
Store gameId
     ↓
Verify both player IDs are attached
     ↓
Player 1 move → 200
     ↓
Player 1 tries again → 403
     ↓
Player 2 move → 200
     ↓
Invalid X-UserId → 401
```

This scenario can be automated in Postman or Bruno using collection variables and test scripts.

## HTTP error behavior

The local configuration includes:

```properties
spring.web.error.include-message=always
```

This makes application error messages visible in local API responses, which is useful during development and Postman/Swagger testing.

## Related service

This application works with the separate `square-users-api`, which is responsible for user persistence and user ID validation.

Typical local setup:

```text
square-games-api  → http://localhost:8080
square-users-api  → http://localhost:8081
```

## Project status

Implemented training milestones include:

- Spring Boot REST API
- Plugin-based game catalog
- DAO/JDBC/JPA persistence work
- H2 and MySQL profiles
- User-aware multiplayer behavior
- Inter-service REST communication
- Turn authorization
- Swagger/OpenAPI documentation
- Postman-compatible integration testing workflow
