package net.hoyoung.imooc.downloader.model;

public class VideoItem {
	private String mid;
	private String name;
	
	private String fileUrlUHD;//超清
	private String fileUrlHD;//高清
	private String fileUrlSD;//标清
	
	public VideoItem(String code, String name) {
		super();
		this.mid = code;
		this.name = name;
	}
	
	public String getMid() {
		return mid;
	}

	public void setMid(String mid) {
		this.mid = mid;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getFileUrlUHD() {
		return fileUrlUHD;
	}
	public void setFileUrlUHD(String fileUrlUHD) {
		this.fileUrlUHD = fileUrlUHD;
	}
	public String getFileUrlHD() {
		return fileUrlHD;
	}
	public void setFileUrlHD(String fileUrlHD) {
		this.fileUrlHD = fileUrlHD;
	}
	public String getFileUrlSD() {
		return fileUrlSD;
	}
	public void setFileUrlSD(String fileUrlSD) {
		this.fileUrlSD = fileUrlSD;
	}
	@Override
	public String toString() {
		return "VideoItem [code=" + mid + ", name=" + name + ", fileUrlUHD="
				+ fileUrlUHD + ", fileUrlHD=" + fileUrlHD + ", fileUrlSD="
				+ fileUrlSD + "]";
	}
}
