package com.game.Constant;

// 消息属性  消息子子类别  消息子类别 消息类别
//    X         X          X        X
//   成功
//   失败
//   中性

public class Code {

    // 用户消息类别
        //成功
    public static final Integer GETSUCCESS = 200;
    public static final Integer LOGIN_SUCCESS = 1001;
    public static final Integer REGISTER_SUCCESS = 1011;
    public static final Integer GETCODE_SUCCESS = 1021;
    public static final Integer GETINFO_SUCCESS = 1031;
    public static final Integer GETHISTORY_SUCCESS = 1041;
    public static final Integer UPLOAD_SUCCESS = 1051;
    public static final Integer GETNUM_SUCCESS = 1061;
    public static final Integer GETACCESS_SUCCESS = 1071;
    public static final Integer SIGNIN_SUCCESS = GETSUCCESS;

        //失败
    public static final Integer LOGIN_FAILURE = 2001;
    public static final Integer CODE_ERROR = 2011;
    public static final Integer FORMAT_ERROR = 2021;
    public static final Integer GETCODE_FAILURE = 2031;
    public static final Integer PAGENUMBER_ILLEGAL = 2041;
    public static final Integer UPLOAD_FAILURE = 2051;
        // 中性
    public static final Integer NEEDLOGIN = 3001;
    public static final Integer PLAYER_EXIST = 3011;
    public static final Integer TOKEN_WRONG = 3021;
    public static final Integer TOKEN_RIGHT = 3031;
    public static final Integer EMAIL_EMPTY = 3041;
    public static final Integer NEED_REFRESH = 3051;
    public static final Integer DATE_ILLEGAL = 3061;
    public static final Integer CNT_ILLEGAL = 3071;
    public static final Integer ALREADY_SIGNIN = 3081;

    // 服务器消息类别
    public static final Integer REQUEST_BLOCK = 4001;



}
