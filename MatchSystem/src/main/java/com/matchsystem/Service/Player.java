package com.matchsystem.Service;

import java.util.Objects;

public class Player {
    String uuid;
    Integer score;

    public Player() {
    }

    public Player(String uuid, Integer score) {
        this.uuid = uuid;
        this.score = score;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return Objects.equals(uuid, player.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uuid);
    }

    @Override
    public String toString() {
        return "Player{" +
                "uuid='" + uuid + '\'' +
                ", score=" + score +
                '}';
    }
}
