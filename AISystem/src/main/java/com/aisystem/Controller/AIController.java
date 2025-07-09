package com.aisystem.Controller;

import com.aisystem.Service.AIchess;
import com.aisystem.Service.Chess;
import com.aisystem.Service.ChessGameUtils;
import com.aisystem.Service.EasyAI;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

// TODO 后期优化 使用websocket一次只传一个棋子，而不是传一个棋盘

@RestController
@RequestMapping("/ai")
public class AIController {

    @Autowired
    RestTemplate restTemplate;

    @PostMapping ("/chess/")
    public void ask(@RequestBody String data){

        String chess = ChessGameUtils.Unzip( JSONObject.parseObject(data).getString("chess"));
        String uuid = JSONObject.parseObject(data).getString("player");
        Integer difficulty = JSONObject.parseObject(data).getInteger("difficulty");

        //使用匿名的方式开辟一个新线程
        new Thread(() -> {
            Chess bestMove;
            if (difficulty == 1){
                EasyAI AI = new EasyAI();
                AI.init(ChessGameUtils.ChessGameArray(chess),uuid);
                bestMove = AI.run();
            }else {
                AIchess AI = new AIchess();
                AI.init(ChessGameUtils.ChessGameArray(chess),uuid);
                bestMove = AI.getBestMove();
            }

            String url = "http://127.0.0.1:8080/game/setchess/"+uuid+"/"+bestMove.getX()+"/"+bestMove.getY();
            restTemplate.getForObject(url,String.class);
        }).start();

    }
}
