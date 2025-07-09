package com.game.Service;

import com.alibaba.fastjson.JSONObject;
import com.game.Constant.ChessGameConstant;
import com.game.Dao.Mapper.PlayerMapper;
import com.game.Dao.Pojo.Player;
import com.game.Service.Impl.ChessGameServiceImpl;
import com.game.Utils.ChessGameUtils;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;



public class ChessGame extends Thread {


    public Integer[][] chess = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
    };


    PlayerMapper playerMapper;
    DataSourceTransactionManager transactionManager;

    Long gameId;
    public Gamer gamerB = null;
    public Gamer gamerW = null;

    // 人机表示难度
    private Integer difficulty = 0;

    private Integer turn = 0;

    private Chess position = null;
    public ArrayBlockingQueue<Chess> queue = new ArrayBlockingQueue<>(1);

    public ChessGame(Gamer gamerB, Gamer gamerW, Long gameId, Integer difficulty ,PlayerMapper playerMapper , DataSourceTransactionManager transactionManager) {
        this.gamerB = gamerB;
        this.gamerW = gamerW;
        this.gameId = gameId;
        this.playerMapper = playerMapper;
        this.transactionManager = transactionManager;
        if (difficulty != null) {
            this.difficulty = difficulty;
        }
        Broadcast("InitChess", chess);
    }

    public void Broadcast(String event, Object obj) {

        gamerB.SendMessage(event, obj);

        if (gamerW != null) {
            gamerW.SendMessage(event, obj);
        }

        if (gamerW == null && event.equals("start_turn") && turn % 2 == 1) {
            RestTemplate restTemplate = new RestTemplate();
            // 棋盘转字符串并压缩
            String chess = ChessGameUtils.ChessGameserialization(this.chess);
            chess = ChessGameUtils.Zip(chess);

            JSONObject request = new JSONObject();
            request.put("chess", chess);
            request.put("player", gamerB.getUuid());
            request.put("difficulty", difficulty);

            restTemplate.postForObject("http://127.0.0.1:9000/ai/chess/", request, String.class);
        }


    }

    public boolean isTurn(String uuid) {
        if (uuid.equals(gamerB.uuid)) {
            return turn % 2 == 0;
        } else {
            return turn % 2 == 1;
        }
    }

    // 判断该位置是否未落子
    public boolean isEmpty(int x, int y) {
        return chess[y][x] == 0;
    }


    public boolean nextStep() throws InterruptedException {
        Chess c = queue.poll(ChessGameConstant.interval, TimeUnit.MILLISECONDS);

        if (c != null) {
            this.position = c;
            this.chess[c.y][c.x] = turn + 1;
            return true;
        } else {
            return false;
        }

    }

    private boolean Judgement(int diffX, int diffY) {
        int role = turn % 2;
        int cnt = 0;
        int i = 0;
        int x, y;
        for (i = 0; i < 5; i++) {
            x = position.y + diffX * i;
            y = position.x + diffY * i;
            if (x >= 0 && y >= 0 && x < 19 && y < 19 && chess[x][y] != 0 && (chess[x][y] - 1) % 2 == role) {
                cnt++;
            } else {
                break;
            }
        }
        for (i = 1; i < 5; i++) {
            x = position.y - diffX * i;
            y = position.x - diffY * i;
            if (x >= 0 && y >= 0 && x < 19 && y < 19 && chess[x][y] != 0 && (chess[x][y] - 1) % 2 == role) {
                cnt++;
            } else {
                break;
            }
        }
        return cnt >= 5;
    }

    public boolean GameOver() {
        if (Judgement(1, 0)) {
            return true;
        } else if (Judgement(0, 1)) {
            return true;
        } else if (Judgement(1, 1)) {
            return true;
        } else if (Judgement(1, -1)) {
            return true;
        }
        return false;
    }

    // 保存对局记录
    public void saveGame(boolean flag , String uuid) {

        // 记录结束时间
        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String s = dateTime.format(formatter).toString();
        // 棋盘转字符串并压缩
        String hischess = ChessGameUtils.ChessGameserialization(chess);
        hischess = ChessGameUtils.Zip(hischess);

        // 判断是人机还是多人
        String uuidW = null;
        if (gamerW == null) {
            uuidW = "-1";
        } else {
            uuidW = gamerW.getUuid();
        }
        // 设置事务
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus transaction = transactionManager.getTransaction(def);
        // 保存认输的记录
        // 并修改分数
        if (uuid != null){
            int whoWin = uuid.equals(gamerB.getUuid()) ? 1 : 0;
            try {
                int loserscore = getloserScore(uuid);

                playerMapper.saveHistory(gamerB.getUuid(), uuidW, s, whoWin, turn, hischess,
                        ChessGameConstant.winnerScore, loserscore);
                // 扣分
                playerMapper.updateScore(uuid,-loserscore);
                // 加分
                playerMapper.updateScore(uuid.equals(gamerB.getUuid())?uuidW:gamerB.getUuid()
                        ,ChessGameConstant.winnerScore);

                transactionManager.commit(transaction);
            }catch (Exception e){
                e.printStackTrace();
                transactionManager.rollback(transaction);
            }
            // 保存完返回
            return;
        }

        //true 五子，当前回合的一方获胜
        //false 超时 对方获胜
        try {
            if (flag) {
                int loserscore = ChessGameConstant.loserScore;
                if (turn % 2 == 0){
                    loserscore = getloserScore(uuidW);
                    playerMapper.updateScore(gamerB.getUuid(),ChessGameConstant.winnerScore);
                    playerMapper.updateScore(uuidW,-loserscore);
                }else{
                    loserscore = getloserScore(gamerB.getUuid());
                    playerMapper.updateScore(uuidW,ChessGameConstant.winnerScore);
                    playerMapper.updateScore(gamerB.getUuid(),-loserscore);
                }
                playerMapper.saveHistory(gamerB.getUuid(), uuidW, s, turn % 2, turn, hischess,
                        ChessGameConstant.winnerScore , loserscore);
            } else {
                int loserscore = ChessGameConstant.loserScore;
                if (turn % 2 == 0){
                    loserscore = getloserScore(gamerB.getUuid());
                    playerMapper.updateScore(gamerB.getUuid(),-loserscore);
                    playerMapper.updateScore(uuidW,ChessGameConstant.winnerScore);
                }else{
                    loserscore = getloserScore(uuidW);
                    playerMapper.updateScore(uuidW,-loserscore);
                    playerMapper.updateScore(gamerB.getUuid(),ChessGameConstant.winnerScore);
               }
                playerMapper.saveHistory(gamerB.getUuid(), uuidW, s, (turn + 1) % 2, turn, hischess,
                        ChessGameConstant.winnerScore , loserscore);
            }
            transactionManager.commit(transaction);
        }catch (Exception e){
            e.printStackTrace();
            transactionManager.rollback(transaction);
        }
    }
    private int getloserScore(String uuid){
        // 查询败方分数
        Player loser = playerMapper.selectByUUID(uuid);
        // 机器人输直接返回
        if (loser == null){
            return 0;
        }
        // 小于阈值才会扣分
        int loserscore = ChessGameConstant.loserScore;
        if (loser.getScore() - ChessGameConstant.loserScore < ChessGameConstant.SCORE_LIMIT){
            loserscore = 0;
        }
        return loserscore;
    }

    public void Reconnect(String uuid) {
        // 玩家所属回合
        Integer playerturn = null;
        Gamer gamer = null;
        if (uuid.equals(gamerB.getUuid())) {
            gamer = gamerB;
            playerturn = 0;
        } else {
            gamer = gamerW;
            playerturn = 1;
        }
        HashMap<String, String> map = new HashMap<>();
        // 当前回合
        map.put("curturn", String.valueOf(turn % 2));
        map.put("playerturn", String.valueOf(playerturn));
        // 字符串化棋盘并压缩
        String chess = ChessGameUtils.ChessGameserialization(this.chess);
        chess = ChessGameUtils.Zip(chess);
        map.put("chess", chess);
        gamer.SendMessage("reconnect", map);
    }

    @Override
    public void run() {
        this.Broadcast("start_turn", 0);
        this.Broadcast("countDown", System.currentTimeMillis() + (long) ChessGameConstant.interval);
        // 判断是超时还是结束
        boolean flag = true;
        try {
            while (true) {
                if (nextStep()) {
                    this.Broadcast("synchronization", this.chess);
                    if (GameOver()) {
                        break;
                    }
                    position = null;
                    turn++;
                    this.Broadcast("start_turn", turn % 2);
                    this.Broadcast("countDown", System.currentTimeMillis() + (long) ChessGameConstant.interval);
                } else {
                    flag = false;
                    break;
                }
            }
        } catch (InterruptedException e) {
            // 产生中断说明 有人认输
            // 由websocket线程处理结果 该线程直接结束

            return;
        }

        if (flag) {
            this.Broadcast("end", turn % 2);
        } else {
            this.Broadcast("timeout", turn % 2);
        }

        ChessGameServiceImpl.endGame(this.gameId, this.gamerB, this.gamerW);
        this.saveGame(flag , null);

    }

}
