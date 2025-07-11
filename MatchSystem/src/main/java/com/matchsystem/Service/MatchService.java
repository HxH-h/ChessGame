package com.matchsystem.Service;

import org.springframework.stereotype.Service;

@Service
public class MatchService {
    public final static MatchPool matchpool = new MatchPool();

    public void addPlayer(Player player) {
        matchpool.addPlayer(player);
    }

    public void removePlayer(String uuid) {
        matchpool.removePlayer(uuid);
    }
}
