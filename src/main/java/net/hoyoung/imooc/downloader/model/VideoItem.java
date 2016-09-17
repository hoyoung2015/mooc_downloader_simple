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

	private final static String AUTH_KEY = "?auth_key";

	public void setFileUrlUHD(String fileUrlUHD) {


//		if(fileUrlUHD!=null && fileUrlUHD.contains(AUTH_KEY)){
//			int pos = fileUrlUHD.indexOf(AUTH_KEY);
//			this.fileUrlUHD = fileUrlUHD.substring(0,pos);
//		}else{
//			this.fileUrlUHD = fileUrlUHD;
//		}
		this.fileUrlUHD = fileUrlUHD;
//		System.out.println(this.fileUrlUHD);
	}
	public String getFileUrlHD() {
		return fileUrlHD;
	}
	public void setFileUrlHD(String fileUrlHD) {
//		if(fileUrlHD!=null && fileUrlHD.contains(AUTH_KEY)){
//			int pos = fileUrlHD.indexOf(AUTH_KEY);
//			this.fileUrlHD = fileUrlHD.substring(0,pos);
//		}else{
//			this.fileUrlHD = fileUrlHD;
//		}

		this.fileUrlHD = fileUrlHD;
	}
	public String getFileUrlSD() {
		return fileUrlSD;
	}
	public void setFileUrlSD(String fileUrlSD) {
//		if(fileUrlSD!=null && fileUrlSD.contains(AUTH_KEY)){
//			int pos = fileUrlSD.indexOf(AUTH_KEY);
//			this.fileUrlSD = fileUrlSD.substring(0,pos);
//		}else{
//			this.fileUrlSD = fileUrlSD;
//		}
//
		this.fileUrlSD = fileUrlSD;
	}
	@Override
	public String toString() {
		return "VideoItem [code=" + mid + ", name=" + name + ", fileUrlUHD="
				+ fileUrlUHD + ", fileUrlHD=" + fileUrlHD + ", fileUrlSD="
				+ fileUrlSD + "]";
	}
}
