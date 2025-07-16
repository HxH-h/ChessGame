package com.game.Controller;

import com.game.Constant.Code;
import com.game.Constant.Message;
import com.game.Controller.ControllerPojo.PlayerVO;
import com.game.Controller.Response.Result;
import com.game.CusAnnotation.RequestLimit;
import com.game.CusException.PlayerException;
import com.game.Dao.Pojo.GameHistory;
import com.game.Service.Impl.FriendServiceImpl;
import com.game.Service.PlayerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@Tag(name = "玩家操作")
@RequestMapping("/api/player")
@RequestLimit(interval = 10000,blockTime = 2)
public class PlayerController {

    
    @Autowired
    FriendServiceImpl friendServiceImpl;

   

    @GetMapping("/getfriend")
    @Operation(summary = "获取好友列表")
    public Result<List> getFriendList(){
        List friendList = friendServiceImpl.getFriendList();
        return new Result<>(Code.GETSUCCESS, Message.GETSUCCESS, friendList);
    }

    @GetMapping("/getmessage/{user}")
    @Operation(summary = "获取消息列表")
    public Result<List> getMessageList(@PathVariable String user) throws PlayerException {
        List messageList = friendServiceImpl.getMessage(user);
        return new Result<>(Code.GETSUCCESS, Message.GETSUCCESS, messageList);
    }
}
