package com.game.Controller;


import com.alibaba.fastjson.JSONObject;
import com.game.Constant.Code;
import com.game.Constant.Message;
import com.game.CusException.LoginException;
import com.game.Service.Task.DisConDetTask;
import com.game.Service.Impl.ChessGameServiceImpl;
import com.game.Utils.JWTUtils;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;



@Component
@ServerEndpoint("/websocket/{token}")
public class WebSocket {

    private static final Logger log = LoggerFactory.getLogger(WebSocket.class);
    public static ConcurrentHashMap<String,WebSocket> player=new ConcurrentHashMap<>();

    public static ChessGameServiceImpl chessGameServiceImpl;
    @Autowired
    public void setChessGameService(ChessGameServiceImpl chessGameServiceImpl) {
        WebSocket.chessGameServiceImpl = chessGameServiceImpl;
    }


    Session session;
    String uuid;
    @OnOpen
    public void OnOpen(Session session, @PathParam("token") String token) throws IOException, LoginException {
        if (token == null){
            log.info("token为空");
            session.close();
        }
        try {
            String uuid = JWTUtils.parseJWT(token, "uuid");
            this.uuid = uuid;
            this.session = session;
            // 判断是否重复登录
            if (player.containsKey(uuid)){
                // 踢出当前玩家
                player.get(uuid).sendMessage("{\"event\": \"exist\"}");
                player.get(uuid).session.close();
            }
            chessGameServiceImpl.addNewGamer(uuid);
            player.put(uuid,this);

            //更新websocket 连接时间
            DisConDetTask.playerLastTime.put(uuid,System.currentTimeMillis());

            // 判断该玩家是否是断线重连
            chessGameServiceImpl.reconnectGame(uuid);
        }catch (Exception e){
            log.info(e.getMessage());
            session.close();
            throw new LoginException(Code.NEEDLOGIN, Message.NEEDLOGIN);
        }
    }

    @OnClose
    public void OnClose(){
        if (this.uuid != null && player.containsKey(this.uuid)){
            player.remove(this.uuid);
            chessGameServiceImpl.saveDisConnectGame(this.uuid);
            chessGameServiceImpl.removeGamer(this.uuid);
        }
    }
    @OnError
    public void OnError(Throwable e){
        e.printStackTrace();
        this.OnClose();
        log.info("意外关闭");
    }


    @OnMessage
    public void OnMessage(String message, Session session){
        JSONObject data = JSONObject.parseObject(message);
        String event = data.getString("event");
        if ("down".equals(event)){
            chessGameServiceImpl.setChess(
                    data.getJSONObject("position").getInteger("x"),
                    data.getJSONObject("position").getInteger("y"),
                    this.uuid
            );
        }else if ("start-matching".equals(event)){
            chessGameServiceImpl.addMatching(this.uuid);
        } else if ("stop-matching".equals(event)) {
            chessGameServiceImpl.removeMatching(this.uuid);
        } else if ("startAI".equals(event)) {
            // 获取难度
            Integer level = data.getInteger("level");
            chessGameServiceImpl.addNewGame(this.uuid , null , level);
        } else if ("giveUp".equals(event)) {
            chessGameServiceImpl.giveUp(this.uuid);
        } else if ("ping".equals(event)){
            this.heartCheck();
        }
        // 更新websocket连接时间
        DisConDetTask.playerLastTime.put(this.uuid,System.currentTimeMillis());
    }

    public void sendMessage(String message){
        synchronized (this.session){
            try {
                this.session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void heartCheck(){
        sendMessage("pang");
    }
}
