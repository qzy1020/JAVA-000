package com.qzy.test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Thread8 {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        final ThreadName threadName = new ThreadName();
        String result = CompletableFuture.supplyAsync(() -> {
            threadName.setName(Thread.currentThread().getName());
            System.out.println("子线程执行结束，执行结果为： "+threadName.getName());
            return threadName.getName();
        }).get();
        System.out.println("主线程执行结束，执行结果为： "+result);
    }
}
