package com.qzy.test;

import java.util.concurrent.CountDownLatch;

public class Thread4 {
    private final  static CountDownLatch countDownLatch = new CountDownLatch(1);
    public static void main(String[] args) throws InterruptedException {
        final ThreadName threadName = new ThreadName();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                threadName.setName(Thread.currentThread().getName());
                System.out.println("子线程执行结束，执行结果为： "+threadName.getName());
                countDownLatch.countDown();
            }
        });
        thread.start();
        countDownLatch.await();
        System.out.println("主线程执行结束，执行结果为： "+threadName.getName());
    }
}
