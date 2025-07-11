package com.matchsystem.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class MatchPool extends Thread {
    private static RestTemplate restTemplate;

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        MatchPool.restTemplate = restTemplate;
    }

    public static List<Player> matchpool = new ArrayList<>();
    ReentrantLock lock = new ReentrantLock();

    private String url = "http://localhost:8080/game/match";


    public void addPlayer(Player player) {
        lock.lock();
        try {
            matchpool.add(player);
        } finally {
            lock.unlock();
        }
    }

    public void removePlayer(String uuid) {
        lock.lock();
        try {
            matchpool.remove(new Player(uuid, null));
        } finally {
            lock.unlock();
        }
    }

    // TODO 设计更高级的匹配方式
    public void match() {
        Iterator<Player> iterator = matchpool.iterator();
        Player playerA = iterator.next();
        iterator.remove();
        Player playerB = iterator.next();
        iterator.remove();
        sendRet(playerA, playerB);

    }

    public void sendRet(Player a, Player b) {
        List<String> players = new ArrayList<>();
        players.add(a.getUuid());
        players.add(b.getUuid());
        restTemplate.postForObject(url, players, String.class);
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(100);
                lock.lock();
                try {
                    if (matchpool.size() >= 2) {
                        match();
                    }
                } finally {
                    lock.unlock();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
    }
}
