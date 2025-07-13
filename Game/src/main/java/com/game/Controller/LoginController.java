package com.game.Controller;

import com.alibaba.fastjson.JSONObject;
import com.game.Constant.Code;
import com.game.Constant.Message;
import com.game.Controller.ControllerPojo.LoginDTO;
import com.game.Controller.Response.Result;
import com.game.CusAnnotation.RequestLimit;
import com.game.CusException.LoginException;
import com.game.Service.PlayerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@Tag(name = "登录操作")
public class LoginController {

    @Autowired
    PlayerService playerServiceImpl;


    @PostMapping("/api/login")
    @Operation(summary = "登录接口")
    @RequestLimit(count = 5)
    public Result<HashMap> login(@RequestBody LoginDTO loginDTO) throws LoginException {
        Map jwt = playerServiceImpl.login(loginDTO);
        return new Result(Code.LOGIN_SUCCESS, Message.LOGIN_SUCCESS, jwt);
    }

    @GetMapping("/api/getAccessToken")
    @Operation(summary = "获取短期Token")
    public Result getAccessToken(HttpServletRequest request) throws LoginException {
        String jwt = request.getHeader("Authorization");
        try {
            String accessToken = playerServiceImpl.getAccessToken(jwt);
            return new Result<>(Code.GETACCESS_SUCCESS , Message.GETACCESS_SUCCESS , accessToken);
        }catch (Exception e){
            throw new LoginException(Code.NEEDLOGIN, Message.NEEDLOGIN);
        }
    }


}
