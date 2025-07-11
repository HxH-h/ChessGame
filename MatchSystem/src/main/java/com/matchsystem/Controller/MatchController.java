package com.matchsystem.Controller;

import com.matchsystem.Service.MatchService;
import com.matchsystem.Service.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/match")
public class MatchController {

    @Autowired
    MatchService matchService;

    @PostMapping("/addPlayer")
    public Result addPlayer(@RequestBody Player player){
        matchService.addPlayer(player);
        return new Result(200,"success");
    }
    @GetMapping("/removePlayer/{uuid}")
    public Result removePlayer(@PathVariable String uuid){
        matchService.removePlayer(uuid);
        return new Result(200,"success");
    }

}
