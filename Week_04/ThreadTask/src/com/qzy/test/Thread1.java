package com.qzy.test;

public class Thread1 {

    public static void main(String[] args) throws InterruptedException {
        ThreadName threadName = new ThreadName();
        Thread thread = new Thread(new SubThread1(threadName));
        thread.start();
        thread.join();
        System.out.println("主线程执行结束，执行结果为： "+threadName.getName());
    }
}

class  SubThread1 implements  Runnable{
    private ThreadName threadName;
    public SubThread1(ThreadName threadName) {
        this.threadName = threadName;
    }
    @Override
    public void run() {
        threadName.setName(Thread.currentThread().getName());
        System.out.println("子线程执行结束，执行结果为： "+threadName.getName());
    }
}
