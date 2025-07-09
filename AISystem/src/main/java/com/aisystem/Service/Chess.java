package com.aisystem.Service;

import java.util.Objects;

public class Chess {
    int value;
    int x;
    int y;

    public Chess(int value, int x, int y) {
        this.value = value;
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Chess chess = (Chess) o;
        return x == chess.x &&
                y == chess.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }


    @Override
    public String toString() {
        return "Chess{" +
                "value=" + value +
                ", x=" + x +
                ", y=" + y +
                '}';
    }
}
