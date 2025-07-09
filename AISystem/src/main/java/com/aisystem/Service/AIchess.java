package com.aisystem.Service;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AIchess {
    Integer[][] board = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 2, 2, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
    };

    Integer basevalue = 20;

    //棋形评分
    enum Pattern {
        FIVE, LIVEFOUR, DEADFOUR, LIVETHREE, DEADTHREE, LIVETWO, DEADTWO
    }

    final Integer[] values = {100000, 20000, 10000, 2000, 1000, 500, 300};
    final int[][] dir_offset = {
            {0, 1},
            {1, 0},
            {1, 1},
            {-1, 1}
    };

    // 最大深度
    Integer MAX_DEPTH = 4;
    Chess bestchess = null;
    // 搜索半径
    final int RADIUS = 2;
    // 棋子颜色
    int mine = 1;

    int rawTimes = 0;
    int actualNumberOfTimes = 0;

    String uuid = null;


    public void init(Integer[][] board , String uuid){
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                this.board[i][j] = board[i][j];
            }
        }
        this.uuid = uuid;
    }


    int calvalue(int x, int y) {
        int tempx = 10 - x;
        int tempy = 10 - y;
        if (tempx < 0) {
            tempx = -tempx;
        }
        if (tempy < 0) {
            tempy = -tempy;
        }
        return basevalue - tempx - tempy;
    }

    HashSet<Chess> genNeighbors(int x, int y) {
        HashSet<Chess> neighbors = new HashSet<>();
        int sx = x - RADIUS;
        int sy = y - RADIUS;
        int ex = x + RADIUS;
        int ey = y + RADIUS;
        if (sx < 0) {
            sx = 0;
        }
        if (sy < 0) {
            sy = 0;
        }
        if (ex >= board.length) {
            ex = board.length - 1;
        }
        if (ey >= board.length) {
            ey = board.length - 1;
        }
        for (int i = sx; i <= ex; i++) {
            for (int j = sy; j <= ey; j++) {
                if (board[i][j] == 0) {
                    neighbors.add(new Chess(calvalue(i, j), i, j));
                }
            }
        }
        return neighbors;
    }

    List<Chess> genmoves() {
        HashSet<Chess> set = new HashSet<>();
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                if (board[i][j] != 0) {
                    HashSet<Chess> neighbors = genNeighbors(i, j);
                    set.addAll(neighbors);
                }
            }
        }
        List<Chess> moves = new ArrayList<>(set);

        return moves;
    }

    String getLine(Chess chess, int turn, int px, int py) {
        StringBuffer stringBuffer = new StringBuffer();
        int tx = chess.x - px * 4, ty = chess.y - py * 4;
        for (int i = 0; i < 9; i++) {
            if (tx < 0 || tx >= 19 || ty < 0 || ty >= 19) {
                stringBuffer.append(3);
            } else {
                if (board[tx][ty] == turn + 1) {
                    stringBuffer.append('X');
                } else if (board[tx][ty] == 0) {
                    stringBuffer.append('0');
                } else {
                    stringBuffer.append('P');
                }
            }
            tx = tx + px;
            ty = ty + py;
        }
        return stringBuffer.toString();
    }

    int analyzeLine(String line) {
        int value = 0;
        StringBuilder sb = new StringBuilder(line);
        sb.setCharAt(4, 'X');
        line = sb.toString();
        if (line.contains("XXXXX")) {
            value = values[Pattern.FIVE.ordinal()];

        } else if (line.contains("0XXXX0")) {
            value = values[Pattern.LIVEFOUR.ordinal()];

        } else if (line.contains("XXXX0") || line.contains("0XXXX")) {
            value = values[Pattern.DEADFOUR.ordinal()];

        } else if (line.contains("XXX0X") || line.contains("X0XXX")) {
            value = values[Pattern.DEADFOUR.ordinal()];

        } else if (line.contains("XX0XX")) {
            value = values[Pattern.DEADFOUR.ordinal()];

        } else if (line.contains("0XXX0")) {
            value = values[Pattern.LIVETHREE.ordinal()];

        } else if (line.contains("0XX0X0") || line.contains("0X0XX0")) {
            value = values[Pattern.LIVETHREE.ordinal()];

        } else if (line.contains("XXX00") || line.contains("00XXX")) {
            value = values[Pattern.DEADTHREE.ordinal()];

        } else if (line.contains("XX0X0") || line.contains("0X0XX")) {
            value = values[Pattern.DEADTHREE.ordinal()];

        } else if (line.contains("XX00X") || line.contains("X00XX")) {
            value = values[Pattern.DEADTHREE.ordinal()];

        } else if (line.contains("X0X0X")) {
            value = values[Pattern.DEADTHREE.ordinal()];

        } else if (line.contains("00XX00")) {
            value = values[Pattern.LIVETWO.ordinal()];

        } else if (line.contains("0X0X0")) {
            value = values[Pattern.LIVETWO.ordinal()];

        } else if (line.contains("X00X0") || line.contains("0X00X")) {
            value = values[Pattern.LIVETWO.ordinal()];

        } else if (line.contains("XX000") || line.contains("000XX")) {
            value = values[Pattern.DEADTWO.ordinal()];

        } else if (line.contains("X0X00") || line.contains("00X0X")) {
            value = values[Pattern.DEADTWO.ordinal()];

        } else if (line.contains("X000X")) {
            value = values[Pattern.DEADTWO.ordinal()];
        }
        return value;
    }

    //turn 0 和 1 代表黑棋和白棋
    int evaluate(Chess chess, int turn) {
        int value = chess.value;
        for (int i = 0; i < dir_offset.length; i++) {
            value += analyzeLine(getLine(chess, turn, dir_offset[i][0], dir_offset[i][1]));
            value += analyzeLine(getLine(chess, (turn + 1) % 2, dir_offset[i][0], dir_offset[i][1]));
        }
        return turn == mine ? value : -value;
    }


    int search(int depth, int role, int score, int beta) {
        if (depth == MAX_DEPTH || score >= values[Pattern.FIVE.ordinal()] || score <= -values[Pattern.FIVE.ordinal()]) {
            return score;
        }

        int tempscore = 0;
        int alpha = -0x7fffffff;

        List<Chess> moves = genmoves();
        for (Chess move : moves) {
            move.value = evaluate(move, role);
        }

        moves.sort((o1, o2) -> o2.value - o1.value);
        if (role != mine){
            Collections.reverse(moves);

        }

        this.rawTimes += moves.size();

        for (Chess move : moves) {
            board[move.x][move.y] = role + 1;
            score += move.value;

            //递归
            tempscore = search(depth + 1, (role + 1) % 2, score, alpha);

            this.actualNumberOfTimes++;

            //回溯
            board[move.x][move.y] = 0;
            score -= move.value;

            if (role == mine) {
                if (tempscore > alpha) {
                    alpha = tempscore;
                    bestchess = move;
                    if (alpha >= beta) {
                        break;
                    }
                }
            } else {
                if (alpha == -0x7fffffff){
                    alpha = -alpha;
                }
                if (tempscore < alpha) {
                    alpha = tempscore;
                    bestchess = move;
                    if (alpha <= beta) {
                        break;
                    }
                }
            }


        }
        return alpha;
    }

    public Chess getBestMove() {
        search(0, mine, 0, -0x7fffffff);
        return bestchess;

    }
}
