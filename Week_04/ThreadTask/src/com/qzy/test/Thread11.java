package com.qzy.test;

import java.util.concurrent.Semaphore;

public class Thread11 {
    private final static Semaphore semaphore = new Semaphore(1);
    public static void main(String[] args) throws InterruptedException {
        final ThreadName threadName = new ThreadName();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    threadName.setName(Thread.currentThread().getName());
                    System.out.println("子线程执行结束，执行结果为： "+threadName.getName());
                }finally {
                    semaphore.release();
                }
            }
        });
        thread.start();
        semaphore.acquire();
        System.out.println("主线程执行结束，执行结果为： "+threadName.getName());
    }
}
