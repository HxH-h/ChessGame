package com.game.Controller;


import com.game.Constant.Code;
import com.game.Constant.Message;
import com.game.Controller.ControllerPojo.RegisterDTO;
import com.game.Controller.Response.Result;
import com.game.CusAnnotation.RequestLimit;
import com.game.CusException.PlayerException;
import com.game.CusException.RegisterException;
import com.game.Service.PlayerService;
import com.game.Utils.IpUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RestController
@Tag(name = "注册操作")
@RequestMapping("/api/register")
public class RegisterController {

    @Autowired
    PlayerService playerServiceImpl;



    @PostMapping("/register")
    @Operation(summary = "注册接口")
    public Result register(@RequestBody RegisterDTO registerDTO) throws RegisterException {
        playerServiceImpl.register(registerDTO);
        return new Result(Code.REGISTER_SUCCESS, Message.REGISTER_SUCCESS);
    }


    @GetMapping("/genCaptcha")
    @Operation(summary = "获取图形验证码")
    public String genCaptcha(HttpServletRequest request, HttpServletResponse response){
        //获取ip地址和浏览器信息 作为key
        String ip = IpUtils.getIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        String key = ip + userAgent;

        String img = playerServiceImpl.getCaptcha(key);
        return img;
    }


    @GetMapping("/genCode/{email}/{captcha}")
    @Operation(summary = "生成验证码")
    public Result genCode(@PathVariable String email,@PathVariable String captcha) throws PlayerException {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attr.getRequest();
        String ip = IpUtils.getIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        String key = ip + userAgent;
        if (playerServiceImpl.verifyCaptcha(key, captcha)){
            playerServiceImpl.getCode(email);
            return new Result<>(Code.GETCODE_SUCCESS, Message.GETCODE_SUCCESS);
        }else {
            return new Result<>(Code.CODE_ERROR, Message.CODE_ERROR);
        }


    }
}
