package com.game.CusException;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BlockException extends Exception{
    private Integer status;
    private String message;

}
