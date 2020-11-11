package com.qzy.test;

import java.util.concurrent.*;

public class Thread7 {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        ThreadName threadName = new ThreadName();
        FutureTask<String> futureTask = new FutureTask<String>(new SubThread7(threadName));
        new Thread(futureTask).start();
        System.out.println("主线程执行结束，执行结果为： "+futureTask.get());
    }
}

class  SubThread7 implements Callable<String> {
    private ThreadName threadName;
    public SubThread7(ThreadName threadName) {
        this.threadName = threadName;
    }
    @Override
    public String call() throws Exception {
        threadName.setName(Thread.currentThread().getName());
        System.out.println("子线程执行结束，执行结果为： "+threadName.getName());
        return threadName.getName();
    }
}


