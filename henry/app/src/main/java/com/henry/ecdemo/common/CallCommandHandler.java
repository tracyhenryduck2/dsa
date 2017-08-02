package com.henry.ecdemo.common;

import android.os.Handler;

public abstract interface CallCommandHandler {
    public abstract void postCommand(Runnable paramRunnable);
    public abstract void destroy();
    public abstract Handler getCommandHandler();
}

