package com.henry.ecdemo.exception;


public class ECDemoException extends Exception {

    public ECDemoException(){

    }

    public ECDemoException(String message){
        super(message);
    }

    public ECDemoException(Throwable throwable){
        super(throwable);
    }


}
