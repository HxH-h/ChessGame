package com.game.Controller;

import com.game.Constant.RedisConstant;
import com.game.Service.Chess;
import com.game.Service.ChessGame;
import com.game.Service.GameService;
import com.game.Service.Impl.ChessGameServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/game")
public class GameController {

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    GameService chessGameServiceImpl;

    @GetMapping("/setchess/{uuid}/{x}/{y}")
    public void AISetChess(@PathVariable String uuid,@PathVariable Integer x,@PathVariable Integer y){
        //x为数组行，y为数组列
        Long gameId = (Long) redisTemplate.opsForHash().get(RedisConstant.player + uuid, "game");
        ChessGame chessGame = ChessGameServiceImpl.games.get(gameId);
        chessGame.queue.offer(new Chess(y,x));
    }

    @PostMapping("/match")
    public void MatchSuccess(@RequestBody List<String> players){

        chessGameServiceImpl.addNewGame(players.get(0),players.get(1),null);
    }
}
