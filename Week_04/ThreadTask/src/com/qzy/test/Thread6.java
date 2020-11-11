package com.qzy.test;

import java.util.concurrent.*;

public class Thread6 {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        ThreadName threadName = new ThreadName();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<String> future = executorService.submit(new SubThread6(threadName));
        System.out.println("主线程执行结束，执行结果为： "+future.get());
        executorService.shutdown();
    }
}

class  SubThread6 implements Callable<String> {
    private ThreadName threadName;
    public SubThread6(ThreadName threadName) {
        this.threadName = threadName;
    }
    @Override
    public String call() throws Exception {
        threadName.setName(Thread.currentThread().getName());
        System.out.println("子线程执行结束，执行结果为： "+threadName.getName());
        return threadName.getName();
    }
}
