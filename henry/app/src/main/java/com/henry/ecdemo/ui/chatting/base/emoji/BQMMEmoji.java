package com.henry.ecdemo.ui.chatting.base.emoji;


public class BQMMEmoji {

    private int code;
    private int res;

    public BQMMEmoji(int code, int res) {
        this.code = code;
        this.res = res;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getRes() {
        return res;
    }

    public void setRes(int res) {
        this.res = res;
    }
}
