package com.aisystem.Service;

public class ChessGameUtils {
    public static String ChessGameserialization(Integer[][] chess){
        StringBuilder s = new StringBuilder();
        for (int i=0;i<19;i++){
            for (int j=0;j<19;j++){
                s.append(chess[i][j]);
            }
            s.append(',');
        }
        return s.toString();
    }
    public static Integer[][] ChessGameArray(String schess){
        Integer [][] chess=new Integer[19][19];
        int j=0,k=0;
        String temp="";
        String tempnum="";
        boolean flag=false;
        for (int i=0;i<schess.length();i++){
            temp=schess.substring(i ,i+1);
            if(temp.equals("/") || temp.equals(",")){
                j++;
                k=0;
                continue;
            }
            if (temp.equals("(")){
                flag = true;
                continue;
            }
            if (temp.equals(")")){
                chess[j][k++]= Integer.parseInt(tempnum)%2==0?2:1;
                tempnum = "";
                flag = false;
                continue;
            }
            if (flag){
                tempnum += temp;
            }else {
                Integer c = Integer.parseInt(temp);
                if (c == 0){
                    chess[j][k++]= 0;
                }else {
                    chess[j][k++]= c%2==0?2:1;
                }
            }

        }
        return chess;
    }
    public static String Unzip(String s){
        StringBuilder replace = new StringBuilder();

        // 对于一行全0的情况，不循环拼接0，而是直接拼接19个0
        String temp = "0000000000000000000";

        // 判断原字符串该行是否全0
        boolean empty = true;
        for (int i = 0; i < s.length(); i++){
            // 遇到字母，则拼接对应个数的0
            if (s.charAt(i) >= 'a' && s.charAt(i) <= 'z'){
                for (int j = 0; j < s.charAt(i) - 'a'; j++){
                    replace.append('0');
                }
                continue;
                // 遇到逗号，则判断该行是否全0
            } else if (s.charAt(i) == ',') {
                if (empty){
                    replace.append(temp);
                }
                //开启新的一行重置
                empty = true;
            }else {
                //改行不全为0
                empty = false;
            }
            replace.append(s.charAt(i));
        }
        return replace.toString();
    }
}
