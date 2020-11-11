package com.qzy.test;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

public class Thread5 {
    private final  static CyclicBarrier cyclicBarrier = new CyclicBarrier(1);
    public static void main(String[] args){
        final ThreadName threadName = new ThreadName();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                threadName.setName(Thread.currentThread().getName());
                System.out.println("子线程执行结束，执行结果为： "+threadName.getName());
                try {
                    cyclicBarrier.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        System.out.println("主线程执行结束，执行结果为： "+threadName.getName());
    }
}
