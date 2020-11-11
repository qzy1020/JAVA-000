package com.qzy.test;

import org.apache.commons.lang.StringUtils;

public class Thread3 {
    private final static Object object = new Object();
    public static void main(String[] args) throws InterruptedException {
        final ThreadName threadName = new ThreadName();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                threadName.setName(Thread.currentThread().getName());
                System.out.println("子线程执行结束，执行结果为： "+threadName.getName());
                synchronized (object){
                    object.notify();
                }
            }
        });
        thread.start();
        synchronized (object){
            object.wait();
        }
        System.out.println("主线程执行结束，执行结果为： "+threadName.getName());
    }
}
