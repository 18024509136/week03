package com.geek.nettyserverdemo;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {

    private ThreadGroup threadGroup;

    private final AtomicInteger threadNumber = new AtomicInteger(1);

    private final String namePrefix;

    private final boolean daemon;

    public NamedThreadFactory(String namePrefix) {
        this(namePrefix, false);
    }

    public NamedThreadFactory(String namePrefix, boolean daemon) {
        this.namePrefix = namePrefix;
        this.daemon = daemon;

        SecurityManager securityManager = System.getSecurityManager();
        threadGroup = securityManager == null ? Thread.currentThread().getThreadGroup() : securityManager.getThreadGroup();
    }

    public Thread newThread(Runnable r) {
        return new Thread(this.threadGroup, r, this.namePrefix + "-" + threadNumber.getAndIncrement(), 0);
    }
}
