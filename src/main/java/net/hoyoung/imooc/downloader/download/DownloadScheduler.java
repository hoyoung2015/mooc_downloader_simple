package net.hoyoung.imooc.downloader.download;

import net.hoyoung.imooc.downloader.model.DownloadInfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 下载调度器
 * 
 * @author hoyoung
 *
 */
public class DownloadScheduler {
	private List<DownloadInfo> tasks;

	public List<DownloadInfo> getTasks() {
		return tasks;
	}

	public DownloadScheduler() {
		super();
	}

    int initTaskSize = 0;
	public void setTasks(List<DownloadInfo> tasks) {
		this.tasks = tasks;
        this.initTaskSize = tasks.size();
	}
    private ThreadPoolExecutor threadPool;
    private void sleep(long mills){
        try {
            Thread.sleep(mills);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
	public void start() {
		// 构造一个线程池
		threadPool = new ThreadPoolExecutor(3, 5, 1000,
				TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(1000),
				new ThreadPoolExecutor.DiscardOldestPolicy());

		for (DownloadInfo downloadInfo : tasks) {
			BatchDownloadFile down = new BatchDownloadFile(downloadInfo);
			threadPool.execute(down);
            sleep(100);
		}
		while (threadPool.getActiveCount() > 0) {// 打印进度
			printProgress();
            sleep(1000);
		}
		printProgress();
		threadPool.shutdown();
	}
	
	public DownloadScheduler(List<DownloadInfo> tasks) {
		super();
		this.tasks = tasks;
        this.initTaskSize = tasks.size();
	}

    /**
     * 打印进度
     */
	private void printProgress(){
		StringBuffer sb = new StringBuffer();
		Iterator<DownloadInfo> ite = tasks.iterator();
        while(ite.hasNext()){
			DownloadInfo downloadInfo = ite.next();
            if(!downloadInfo.isDownloading()){
                continue;
            }
            sb.setLength(0);//清空
			sb.append("|");
			int now = downloadInfo.getProgress()/5;
			for (int i = 1; i <= 20; i++) {
				if(i<=now){
					sb.append(">");
				}else{
					sb.append("=");
				}
			}
			sb.append("| ");
			sb.append(downloadInfo.getProgress() + "% ");// 进度值
			sb.append((downloadInfo.getLength()/1024/1024)+"M ");
			sb.append(downloadInfo.getFileName());// 名称
			
			if(downloadInfo.getProgress()==100){
				ite.remove();
			}
            System.out.println(sb.toString());
		}
        System.out.println("完成量："+(initTaskSize-tasks.size())+" / "+initTaskSize);
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		List<DownloadInfo> tasks = new ArrayList<DownloadInfo>();

		DownloadInfo c = new DownloadInfo();
		c.setUrl("http://v1.mukewang.com/5922b1fb-b9c5-410f-85b0-db4da860bd93/L.mp4");
		c.setFileName("1-1 概述.mp4");
		c.setFilePath(c.getFileName());
		c.setSplitter(5);
		tasks.add(c);

		DownloadInfo a = new DownloadInfo();
		a.setUrl("http://v1.mukewang.com/259f8cb1-8596-4278-9916-a7d634b1674b/H.mp4");
		a.setFileName("1-2 触发器的概念和第一个触发器.mp4");
		a.setFilePath(a.getFileName());
		a.setSplitter(5);

		DownloadInfo b = new DownloadInfo();
		b.setUrl("http://v1.mukewang.com/445cdc85-bffa-48dd-b41d-59012c5980ec/H.mp4");
		b.setFileName("1-3 触发器的应用场景.mp4");
		b.setFilePath(b.getFileName());
		b.setSplitter(5);

		// tasks.add(a);
		// tasks.add(b);

		DownloadScheduler scheduler = new DownloadScheduler();
		scheduler.setTasks(tasks);
		scheduler.start();
		System.out.println("over");
	}

}
