package com.afsaneh.square_games_api.dto;


import fr.le_campus_numerique.square_games.engine.CellPosition;

public class MoveInfo {

    private CellPosition from;
    private CellPosition to;

    public MoveInfo(CellPosition from, CellPosition to) {
        this.from = from;
        this.to = to;
    }

    public CellPosition getFrom() {
        return from;
    }

    public CellPosition getTo() {
        return to;
    }
}
