package com.game.Service;

public class Chess {
    Integer x;
    Integer y;

    public Chess(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "Chess{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
