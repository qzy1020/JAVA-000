package com.qzy.test;

import org.apache.commons.lang.StringUtils;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Thread10 {
    private final static Lock lock = new ReentrantLock();
    private final static Condition writeCondition = lock.newCondition();
    private final static Condition readCondition = lock.newCondition();

    public static void main(String[] args) {
        final ThreadName threadName = new ThreadName();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    lock.lock();
                    while (StringUtils.isNotEmpty(threadName.getName())){
                        try {
                            writeCondition.await();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    threadName.setName(Thread.currentThread().getName());
                    System.out.println("子线程执行结束，执行结果为： "+threadName.getName());
                    writeCondition.signal();
                }finally {
                    lock.unlock();
                }
            }
        });
        thread.start();
        try{
            lock.lock();
            while (StringUtils.isEmpty(threadName.getName())){
                writeCondition.await();
            }
            System.out.println("主线程执行结束，执行结果为： "+threadName.getName());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
}
