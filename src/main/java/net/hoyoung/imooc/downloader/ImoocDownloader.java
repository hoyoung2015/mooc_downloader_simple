package net.hoyoung.imooc.downloader;

import java.io.File;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

import net.hoyoung.imooc.downloader.model.VideoItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.JsonPathSelector;
import us.codecraft.webmagic.selector.Selectable;

public class ImoocDownloader {
	protected Logger logger = LoggerFactory.getLogger(getClass());
	private Vector<VideoItem> videoItems;//视频列表
	private String courseName;//课程名称
	private String targetUrl;
	
	private int videoType = 2;//1超清，2高清，3普清
	
	private static String DOWN_URL = "http://www.imooc.com/course/ajaxmediainfo/?mid={mid}&mode=flash";
	
	public ImoocDownloader(String targetUrl) {
		videoItems  = new Vector<VideoItem>();
		
		this.targetUrl = targetUrl;
	}
	/**
	 * 
	 * @param targetUrl 下载url
	 * @param videoType 清晰度选择
	 */
	public ImoocDownloader(String targetUrl,int videoType) {
		this(targetUrl);
		this.videoType = videoType;
	}
	public void start(){
		Spider.create(new VideoItemPageProcessor())
		.addUrl(this.targetUrl)
		.thread(1)
		.run();
		System.out.println("课程名称：【"+this.courseName+"】");
		logger.info("共获取"+videoItems.size()+"条视频信息，准备提取下载地址......");
		System.out.println("共获取"+videoItems.size()+"条视频信息，准备提取下载地址......");
		Spider spider = Spider.create(new VideoItemFileUrlPageProcessor());
		
		for (VideoItem videoItem : videoItems) {
			Request req = new Request(DOWN_URL.replace("{mid}", videoItem.getMid()));
			req.putExtra("videoItem", videoItem);
			spider.addRequest(req);
		}
		spider.thread(5).run();
		logger.info("下载地址提取完成，共"+this.videoItems.size()+"条视频：");
		System.out.println("下载地址提取完成，共"+this.videoItems.size()+"条视频：");
		for (VideoItem videoItem : videoItems) {
			System.out.println(">"+videoItem.getName());
			logger.info(">"+videoItem.getName());
		}
		
		//创建目录
		
		File file = new File(this.courseName);
		if(!file.exists()){
			System.out.println("创建目录 "+this.courseName);
			file.mkdir();
		}else{
			System.out.println("目录 "+this.courseName+" 已存在");
		}
		for (VideoItem videoItem : videoItems) {
			String downloadUrl = null;
			if(this.videoType==1){
				downloadUrl = videoItem.getFileUrlUHD();
			}else if(this.videoType==2){
				downloadUrl = videoItem.getFileUrlHD();
			}else{
				downloadUrl = videoItem.getFileUrlSD();
			}
			String ext =downloadUrl.substring(downloadUrl.lastIndexOf("."));
			String fileName = videoItem.getName()+ext;
			DownloadUtils.download(downloadUrl, fileName, this.courseName, 5);
		}
//		System.out.println("课程 "+this.courseName+" 下载完成");
	}

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		System.out.print("输入要下载的课程编号：");
		int courseId = scanner.nextInt();
		System.out.print("选择清晰度（1：超清UHD，2：高清HD，3：普清SD）：");
		
		int videoType = scanner.nextInt();
		if(videoType!=1 && videoType!=2 && videoType!=3){
			System.err.println("输入有误");
			System.exit(0);
		}
		scanner.close();
		System.out.println("正在解析课程信息，请稍等......");
		new ImoocDownloader("http://www.imooc.com/learn/"+courseId,videoType).start();
	}
	
	//获取视频信息内部类
	class VideoItemPageProcessor implements PageProcessor {

		@Override
		public void process(Page page) {
			//获取课程名称
			ImoocDownloader.this.courseName = page.getHtml().xpath("//h2[@class='l']/text()").get();
			
			if(ImoocDownloader.this.videoType==1){
				ImoocDownloader.this.courseName += "(UHD)";
			}else if(ImoocDownloader.this.videoType==2){
				ImoocDownloader.this.courseName += "(HD)";
			}else{
				ImoocDownloader.this.courseName += "(SD)";
			}
			
			List<Selectable> list = page.getHtml().xpath("//a[@class='J-media-item studyvideo']").nodes();
			for (Selectable a : list) {
				String href = a.xpath("/a/@href").get();
				String code = href.substring(href.lastIndexOf("/")+1);
				String name = a.xpath("/a/text()").get().replace(":", "'");
				VideoItem vi = new VideoItem(code, name);
				ImoocDownloader.this.videoItems.add(vi);//加入列表
			}
		}
		private Site site = Site.me().setRetryTimes(10).setSleepTime(100);
		@Override
		public Site getSite() {
			site.setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.93 Safari/537.36");
			site.setDomain("www.imooc.com");
			return site;
		}

	}
	class VideoItemFileUrlPageProcessor implements PageProcessor {
		@Override
		public void process(Page page) {
			List<String> medias = new JsonPathSelector("$.data.result.mpath").selectList(page.getRawText());
			VideoItem videoItem = (VideoItem) page.getRequest().getExtra("videoItem");
			videoItem.setFileUrlUHD(medias.get(0));
			videoItem.setFileUrlHD(medias.get(1));
			videoItem.setFileUrlSD(medias.get(2));
		}
		private Site site = Site.me().setRetryTimes(10).setSleepTime(100);
		@Override
		public Site getSite() {
			site.setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.93 Safari/537.36");
			site.setDomain("www.imooc.com");
			return site;
		}
		
	}
}
