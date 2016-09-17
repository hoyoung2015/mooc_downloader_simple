package net.hoyoung.imooc.downloader.model;

public class DownloadTask {
	private String url;
	private String fileName;
	private String savePath;
	private int threadNum;
	private int progress;//下载进度
	
	public int getProgress() {
		return progress;
	}
	public void setProgress(int progress) {
		this.progress = progress;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getSavePath() {
		return savePath;
	}
	public void setSavePath(String savePath) {
		this.savePath = savePath;
	}
	public int getThreadNum() {
		return threadNum;
	}
	public void setThreadNum(int threadNum) {
		this.threadNum = threadNum;
	}
	
}
