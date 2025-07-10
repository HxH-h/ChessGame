package com.game.Service;


public interface GameService {

    //在线玩家
    void addNewGamer(String uuid);
    void removeGamer(String uuid);

    //匹配池
    void addMatching(String uuid);
    void removeMatching(String uuid);


    //正在进行的游戏
    void addNewGame(String playerA, String playerB,Integer difficulty);


    //游戏操作
    void startGame(Gamer playerA, Gamer playerB);
    void setChess(Integer x , Integer y , String uuid);

    //游戏断线信息保存
    void saveDisConnectGame(String uuid);
    //断线重连
    void reconnectGame(String uuid);
    // 主动认输
    void giveUp(String uuid);

}
