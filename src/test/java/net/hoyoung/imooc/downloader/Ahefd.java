package net.hoyoung.imooc.downloader;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2015/11/18.
 */
public class Ahefd {
    public static void main(String[] args) {
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(3, 5, 0,
                TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(1000),
                new ThreadPoolExecutor.DiscardOldestPolicy());


        for (int i = 0; i < 10; i++) {
            MyRunnable my = new MyRunnable(i+1+"");
            threadPool.execute(my);
        }
        System.out.println("a");
        threadPool.shutdown();
        System.out.println("b");

    }
    static class MyRunnable implements Runnable{
        private String name;

        public MyRunnable(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public void run() {
            System.out.println("name:"+name);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
