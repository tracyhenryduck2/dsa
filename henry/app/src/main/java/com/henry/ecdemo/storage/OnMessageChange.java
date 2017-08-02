package com.henry.ecdemo.storage;

public interface OnMessageChange {
    /**
     * 数据库改变
     */
    public void onChanged(String sessionId);
}
