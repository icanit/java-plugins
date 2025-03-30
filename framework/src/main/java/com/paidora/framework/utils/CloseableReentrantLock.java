package com.paidora.framework.utils;

import java.util.concurrent.locks.ReentrantLock;

public class CloseableReentrantLock extends ReentrantLock implements AutoCloseable {
    public CloseableReentrantLock open() {
        this.lock();
        return this;
    }

    @Override
    public void close() {
        this.unlock();
    }
}