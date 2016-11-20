package net.hoyoung.imooc.downloader.download;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.hoyoung.imooc.downloader.model.DownloadInfo;
import net.hoyoung.imooc.downloader.model.DownloadInfo.DownloadStatus;
import net.hoyoung.imooc.downloader.utils.CommonStringUtils;

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
		threadPool = new ThreadPoolExecutor(3, 3, 100,
				TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(1000),
				new ThreadPoolExecutor.DiscardOldestPolicy());

		for (DownloadInfo downloadInfo : tasks) {
			BatchDownloadFile down = new BatchDownloadFile(downloadInfo);
			threadPool.execute(down);
		}
		while (threadPool.getActiveCount() > 0) {// 打印进度
			printProgress();
            sleep(1000);
		}
		threadPool.shutdown();
	}
	
	public DownloadScheduler(List<DownloadInfo> tasks) {
		super();
		this.tasks = tasks;
        this.initTaskSize = tasks.size();
	}

	private int progressStringLength = 0;
    /**
     * 打印进度
     */
	private void printProgress(){
		StringBuffer sb = new StringBuffer();
		
		Iterator<DownloadInfo> ite = tasks.iterator();
		for (int i = 0; i < progressStringLength; i++) {
        	System.out.print("\b");
		}
		int cnt = 0;
		
        while(ite.hasNext()){
        	DownloadInfo downloadInfo = ite.next();
        	if(downloadInfo.getDownloadStatus()==DownloadStatus.UNDOWNLOAD){
        		continue;
			}else if(downloadInfo.getDownloadStatus()==DownloadStatus.DOWNLOADED){
				ite.remove();
			}
        	if(cnt++<3){
        		sb.append(downloadInfo.getFileName()+" ");// 名称
    			sb.append((downloadInfo.getLength()/1024/1024)+"M ");
    			sb.append(downloadInfo.getProgress() + "% ");// 进度值
    			sb.append(" | ");
        	}
		}
        sb.append((initTaskSize-tasks.size())+"/"+initTaskSize);
        System.out.print(sb.toString());
        int tmp = CommonStringUtils.length(sb.toString());
        // 先擦出长出来的部分
        for (int i = 0; i < progressStringLength-tmp; i++) {
			System.out.print(" ");
		}
        // 再退回去
        for (int i = 0; i < progressStringLength-tmp; i++) {
			System.out.print("\b");
		}
        progressStringLength = tmp;
	}
	public static void main(String[] args) {
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
