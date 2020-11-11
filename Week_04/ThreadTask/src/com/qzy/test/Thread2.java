package com.qzy.test;

import org.apache.commons.lang.StringUtils;

public class Thread2 {

    public static void main(String[] args) throws InterruptedException {
        ThreadName threadName = new ThreadName();
        Thread thread = new Thread(new SubThread2(threadName));
        thread.start();
        while (StringUtils.isEmpty(threadName.getName())){
            Thread.sleep(1000);
        }
        System.out.println("主线程执行结束，执行结果为： "+threadName.getName());
    }
}
class  SubThread2 implements  Runnable{
    private ThreadName threadName;
    public SubThread2(ThreadName threadName) {
        this.threadName = threadName;
    }
    @Override
    public void run() {
        threadName.setName(Thread.currentThread().getName());
        System.out.println("子线程执行结束，执行结果为： "+threadName.getName());
    }
}


