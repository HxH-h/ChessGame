package com.game.Service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.game.Constant.GamerStatus;
import com.game.Constant.RedisConstant;
import com.game.Controller.Response.Result;
import com.game.Dao.Mapper.PlayerMapper;
import com.game.Dao.Pojo.Player;
import com.game.Service.Chess;
import com.game.Service.ChessGame;
import com.game.Service.GameService;
import com.game.Service.Gamer;
import com.game.Utils.ChessGameUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


@Component
@Slf4j
public class ChessGameServiceImpl implements GameService {
    // 存储游戏房间
    public static ConcurrentHashMap<Long, ChessGame> games = new ConcurrentHashMap<>();

    // 操作redis
    public static RedisTemplate redisTemplate;

    @Autowired
    public void setRedisTemplate(RedisTemplate redisTemplate) {
        ChessGameServiceImpl.redisTemplate = redisTemplate;
    }

    // 发送HTTP请求
    private static RestTemplate restTemplate;

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        ChessGameServiceImpl.restTemplate = restTemplate;
    }

    @Autowired
    public PlayerMapper playerMapper;

    // 事务管理器
    @Autowired
    public DataSourceTransactionManager transactionManager;

    @Override
    public void addNewGamer(String uuid) {
        Player player = playerMapper.selectByUUID(uuid);
        HashMap<String, String> map = new HashMap<>();
        map.put("uuid", player.getUuid());
        map.put("username", player.getUsername());
        map.put("photo", player.getPhoto());
        map.put("status", GamerStatus.ONLINE);
        map.put("score", String.valueOf(player.getScore()));
        map.put("game", null);
        redisTemplate.opsForHash().putAll(RedisConstant.player + uuid, map);
    }

    @Override
    public void removeGamer(String uuid) {
        try {
            redisTemplate.delete(RedisConstant.player + uuid);
        } catch (Exception e) {
            e.printStackTrace();
            log.info("redis 玩家离线 删除玩家失败");
        }

    }


    @Override
    public void addMatching(String uuid) {
        // 查看是否为匹配状态 只有online允许匹配
        String status = (String) redisTemplate.opsForHash()
                .get(RedisConstant.player + uuid, "status");

        if (!status.equals(GamerStatus.ONLINE)) {
            return;
        }

        HashMap<String, String> player = new HashMap<>();
        player.put("uuid", uuid);
        // 获取分数
        String score = (String) redisTemplate.opsForHash()
                .get(RedisConstant.player + uuid, "score");
        player.put("score", score);

        // 发送信息
        String url = "http://127.0.0.1:9001/match/addPlayer";
        Result r = restTemplate.postForObject(url, player, Result.class);

        //判断是否添加成功
        if (r.getCode() == 200) {
            redisTemplate.opsForHash()
                    .put(RedisConstant.player + uuid, "status", GamerStatus.MATCHING);
        }
    }

    @Override
    public void removeMatching(String uuid) {
        String url = "http://127.0.0.1:9001/match/removePlayer/" + uuid;
        Result ret = restTemplate.getForObject(url, Result.class);
        if (ret.getCode() == 200) {
            redisTemplate.opsForHash()
                    .put(RedisConstant.player + uuid, "status", GamerStatus.ONLINE);
        }
    }

    // 该方法可能同时被多个线程调用
    // 加锁同步
    @Override
    public synchronized void addNewGame(String playerA, String playerB,Integer difficulty) {
        String Aname = (String) redisTemplate.opsForHash().get(RedisConstant.player + playerA, "username");
        Gamer gamerA = new Gamer(playerA, Aname);

        // 判断是人机还是玩家
        Gamer gamerB = null;
        if (playerB != null) {
            String Bname = (String) redisTemplate.opsForHash().get(RedisConstant.player + playerB, "username");
            gamerB = new Gamer(playerB, Bname);
        }

        // 生成房间号
        Long num = redisTemplate.opsForValue().increment(RedisConstant.gameId, 1L);

        Long timestamp = System.currentTimeMillis();
        Long gameId = timestamp << 32 | num;
        // 按位与 确保为正整数
        Long mask = 0x7FFFFFFFFFFFFFFFL;
        gameId = gameId & mask;

        // 更新玩家状态
        redisTemplate.opsForHash().put(RedisConstant.player + playerA, "status", GamerStatus.PLAYING);
        redisTemplate.opsForHash().put(RedisConstant.player + playerA, "game", gameId);
        if (playerB != null) {
            redisTemplate.opsForHash().put(RedisConstant.player + playerB, "status", GamerStatus.PLAYING);
            redisTemplate.opsForHash().put(RedisConstant.player + playerB, "game", gameId);
        }

        startGame(gamerA, gamerB);

        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        ChessGame chessGame = new ChessGame(gamerA, gamerB, gameId, difficulty,playerMapper,transactionManager);

        games.put(gameId, chessGame);
        // 开始游戏

        chessGame.start();

    }


    public static void endGame(Long gameId, Gamer playerA, Gamer playerB) {
        //游戏结束，删除该游戏
        games.remove(gameId);

        // 更新玩家状态
        String Aid = playerA.getUuid();
        redisTemplate.opsForHash().put(RedisConstant.player + Aid, "status", GamerStatus.ONLINE);
        redisTemplate.opsForHash().put(RedisConstant.player + Aid, "game", null);
        // B可能是人机 要判断是否为空
        if (playerB != null) {
            String Bid = playerB.getUuid();
            redisTemplate.opsForHash().put(RedisConstant.player + Bid, "status", GamerStatus.ONLINE);
            redisTemplate.opsForHash().put(RedisConstant.player + Bid, "game", null);
        }


    }

    @Override
    public void startGame(Gamer playerA, Gamer playerB) {
        JSONObject respa = new JSONObject();
        if (playerB == null) {
            respa.put("opponent_name", "AI");
        } else {
            respa.put("opponent_name", playerB.getUsername());
            // 获取头像
            String photo = (String) redisTemplate.opsForHash()
                    .get(RedisConstant.player + playerB.getUuid(), "photo");
            respa.put("opponent_photo", photo);
            // 获取分数
            String score = (String) redisTemplate.opsForHash()
                    .get(RedisConstant.player + playerB.getUuid(), "score");
            respa.put("opponent_score", score);
        }

        respa.put("turn", 0);

        playerA.SendMessage("match_success", respa.toJSONString());


        if (playerB == null) {
            return;
        }

        JSONObject respb = new JSONObject();
        respb.put("oppont_name", playerA.getUsername());
        respb.put("turn", 1);

        playerB.SendMessage("match_success", respb.toJSONString());
    }


    @Override
    public void setChess(Integer x, Integer y, String uuid) {
        Long gameId = (Long) redisTemplate.opsForHash().get(RedisConstant.player + uuid, "game");
        ChessGame chessGame = games.get(gameId);
        if (chessGame.isTurn(uuid) && chessGame.isEmpty(x, y)) {
            chessGame.queue.offer(new Chess(x, y));
        }
    }

    // 保存断线信息
    @Override
    public void saveDisConnectGame(String uuid) {
        // 获取状态

        String gamerStatus = (String) redisTemplate.opsForHash().get(RedisConstant.player + uuid, "status");
        // 状态判空
        if (gamerStatus == null) {
            return;
        }
        // 若为playing 则保存断线前的信息
        if (gamerStatus.equals(GamerStatus.PLAYING)) {
            Long gameId = (Long) redisTemplate.opsForHash()
                    .get(RedisConstant.player + uuid, "game");
            // 保存断线前的信息，时长 1 分钟
            redisTemplate.opsForValue()
                    .set(RedisConstant.disconnection + uuid, gameId, 1, TimeUnit.MINUTES);
        }
    }

    // 断线重连
    @Override
    public void reconnectGame(String uuid) {

        // 判断是否需要重连
        Long gameId = (Long) redisTemplate.opsForValue().get(RedisConstant.disconnection + uuid);
        // 获取断线之前的信息，更新信息
        if (gameId != null) {
            // 判断该游戏是否已经结束 ， 结束则发送消息返回匹配页面
            if (games.containsKey(gameId)) {
                // 更新信息
                redisTemplate.opsForHash().put(RedisConstant.player + uuid, "game", gameId);
                redisTemplate.opsForHash().put(RedisConstant.player + uuid, "status", GamerStatus.PLAYING);
                // 同步局面和回合
                games.get(gameId).Reconnect(uuid);
            }
            // 重连后删除断线信息
            redisTemplate.delete(RedisConstant.disconnection + uuid);

        }

    }

    @Override
    public void giveUp(String uuid) {
        // 查看该玩家所在对局
        Long gameId = (Long) redisTemplate.opsForHash()
                .get(RedisConstant.player + uuid, "game");

        // 判断对局是否还在
        if (gameId != null && games.containsKey(gameId)) {
            ChessGame chessGame = games.get(gameId);
            // 广播认输消息
            int whoLose = uuid.equals(chessGame.gamerB.getUuid()) ? 0 : 1;
            chessGame.Broadcast("giveUp", whoLose);
            // 保存对局信息
            chessGame.saveGame(false, uuid);
            // 结束对局
            endGame(gameId, chessGame.gamerB, chessGame.gamerW);
            // 中断线程
            chessGame.interrupt();
        }
    }
}
