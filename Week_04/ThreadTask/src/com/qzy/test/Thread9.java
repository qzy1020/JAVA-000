package com.qzy.test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.BrokenBarrierException;

public class Thread9 {
    private final static BlockingQueue<String> queue = new ArrayBlockingQueue<>(1);
    public static void main(String[] args) throws InterruptedException {
        final ThreadName threadName = new ThreadName();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                threadName.setName(Thread.currentThread().getName());
                System.out.println("子线程执行结束，执行结果为： "+threadName.getName());
                queue.offer(threadName.getName());
            }
        });
        thread.start();
        System.out.println("主线程执行结束，执行结果为： "+queue.take());
    }
}
