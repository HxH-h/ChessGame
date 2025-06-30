package com.game.Controller;

import com.game.Constant.Code;
import com.game.Constant.Message;
import com.game.Controller.Response.Result;
import com.game.CusException.BlockException;
import com.game.CusException.LoginException;
import com.game.CusException.PlayerException;
import com.game.CusException.RegisterException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;


@ControllerAdvice
@Slf4j
public class ExceptionController {

    @ExceptionHandler(LoginException.class)
    @ResponseBody
    public Result loginExpHandler(LoginException e) {
        log.info(e.getMessage());
        return new Result(e.getStatus() ,e.getMessage());
    }

    @ExceptionHandler(RegisterException.class)
    @ResponseBody
    public Result registerExpHandler(RegisterException e) {
        log.info(e.getMessage());
        return new Result(e.getStatus() ,e.getMessage());
    }

    @ExceptionHandler(PlayerException.class)
    @ResponseBody
    public Result playerExpHandler(PlayerException e) {
        log.info(e.getMessage());
        return new Result(e.getStatus() , e.getMessage());
    }

    @ExceptionHandler(BlockException.class)
    @ResponseBody
    public Result BlockExpHandler(BlockException e){
        return new Result(e.getStatus(), e.getMessage());
    }

}
