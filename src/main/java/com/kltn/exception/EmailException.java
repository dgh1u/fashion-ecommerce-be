package com.kltn.exception;

import lombok.Data;

@Data
public class EmailException extends RuntimeException{
    public EmailException(String mes){
        super(mes);
    }
}
