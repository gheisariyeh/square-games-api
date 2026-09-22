package com.afsaneh.square_games_api.dto;

import fr.le_campus_numerique.square_games.engine.CellPosition;
import io.swagger.v3.oas.annotations.media.Schema;

public class MoveParams {
    @Schema(
            description = "Source position of the token. Null when placing a token that is not yet on the board."
    )
    private CellPosition from;

    @Schema(
            description = "Destination position of the move"
    )
    private CellPosition to;

    public MoveParams() {
    }

    public CellPosition getFrom() {
        return from;
    }

    public void setFrom(CellPosition from) {
        this.from = from;
    }

    public CellPosition getTo() {
        return to;
    }

    public void setTo(CellPosition to) {
        this.to = to;
    }
}
