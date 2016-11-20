package net.hoyoung.imooc.downloader;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import net.hoyoung.imooc.downloader.download.DownloadScheduler;
import net.hoyoung.imooc.downloader.model.DownloadInfo;
import net.hoyoung.imooc.downloader.model.VideoItem;
import net.hoyoung.imooc.downloader.model.DownloadInfo.DownloadStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.hoyoung.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.JsonPathSelector;
import us.codecraft.webmagic.selector.Selectable;

public class ImoocDownloader {
	private static int THREAD_NUM = 5;
	static Logger logger = LoggerFactory.getLogger(ImoocDownloader.class);
	private Vector<VideoItem> videoItems;// 视频列表
	private String courseName;// 课程名称
	private String targetUrl;
	private static Map<Integer, String> VIDEO_TYPE;
	static {
		VIDEO_TYPE = new HashMap<Integer, String>();
		VIDEO_TYPE.put(1, "UHD");
		VIDEO_TYPE.put(2, "HD");
		VIDEO_TYPE.put(3, "SD");
	}

	private int videoType = 2;// 1超清，2高清，3普清

	private static String DOWN_URL = "http://www.imooc.com/course/ajaxmediainfo/?mid={mid}&mode=flash";

	public ImoocDownloader(String targetUrl) {
		videoItems = new Vector<VideoItem>();

		this.targetUrl = targetUrl;
	}

	/**
	 * 
	 * @param targetUrl
	 *            下载url
	 * @param videoType
	 *            清晰度选择
	 */
	public ImoocDownloader(String targetUrl, int videoType) {
		this(targetUrl);
		this.videoType = videoType;
	}

	public void parseCourse() {
		Spider.create(new VideoItemPageProcessor()).addUrl(this.targetUrl).thread(1).run();
		if (videoItems.isEmpty()) {
			System.err.println("下载地址提取失败，请重试！");
			System.exit(1);
		}
		System.out.println("课程名称：【" + this.courseName + "】");
		logger.info("共获取" + videoItems.size() + "条视频信息，准备提取下载地址......");
		System.out.println("共获取" + videoItems.size() + "条视频信息，准备提取下载地址......");
		Spider spider = Spider.create(new VideoItemFileUrlPageProcessor());

		for (VideoItem videoItem : videoItems) {
			Request req = new Request(DOWN_URL.replace("{mid}", videoItem.getMid()));
			req.putExtra("videoItem", videoItem);
			spider.addRequest(req);
		}
		spider.thread(2).run();
		logger.info("下载地址提取完成，共" + this.videoItems.size() + "条视频：");
		System.out.println("下载地址提取完成，共" + this.videoItems.size() + "条视频：");
		for (VideoItem videoItem : videoItems) {
			System.out.println(videoItem.getName());
			logger.info(">" + videoItem.getName());
		}
	}

	public void start(int videoType) {
		this.videoType = videoType;

		// 文件夹标注出清晰度
		this.courseName += "(" + VIDEO_TYPE.get(this.videoType) + ")";
		// 创建目录
		File file = new File(this.courseName);
		if (!file.exists()) {
			System.out.println("创建目录 " + this.courseName);
			file.mkdir();
		} else {
			System.out.println("目录 " + this.courseName + " 已存在");
		}
		List<DownloadInfo> tasks = new ArrayList<DownloadInfo>();

		try {
			Method method = VideoItem.class.getMethod("getFileUrl" + VIDEO_TYPE.get(this.videoType));
			for (VideoItem videoItem : videoItems) {

				String downloadUrl = (String) method.invoke(videoItem);

				String ext = downloadUrl.substring(downloadUrl.lastIndexOf("."));

				String authKey = "?auth_key";
				if (ext.contains(authKey)) {
					ext = ext.substring(0, ext.indexOf(authKey));
				}

				String fileName = videoItem.getName() + ext;
				DownloadInfo bean = new DownloadInfo(downloadUrl, fileName, this.courseName, THREAD_NUM);
				bean.setDownloadStatus(DownloadStatus.UNDOWNLOAD);
				tasks.add(bean);
				// System.out.println(downloadUrl);
			}
			System.out.println("开始下载...");
			new DownloadScheduler(tasks).start();// 开始下载
			System.out.println("\n课程 " + this.courseName + " 下载完成");
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		System.out.println("#####################################################################");
		System.out.println("#慕课网视频下载器 made by hoyoung");
		System.out.println("#需要源码的微博 @噬魂brilliant 私信我哦！*^^*");
		System.out.println("#到慕课网官网打开想要下载的课程的章节列表页面，查看当前url链接");
		System.out.println("#例如http://www.imooc.com/learn/414，则课程编号为414");
		System.out.println("#####################################################################");

		Scanner scanner = new Scanner(System.in);
		System.out.print("输入要下载的课程编号：");
		int courseId = scanner.nextInt();

		System.out.println("正在解析课程信息，请稍等......");

		ImoocDownloader imoocDownloader = new ImoocDownloader("http://www.imooc.com/learn/" + courseId);
		// 解析课程列表
		imoocDownloader.parseCourse();

		System.out.print("选择清晰度（1：超清UHD，2：高清HD，3：普清SD）：");
		int videoType = scanner.nextInt();
		if (videoType != 1 && videoType != 2 && videoType != 3) {
			System.err.println("输入有误");
			System.exit(0);
		}
		scanner.close();
		imoocDownloader.start(videoType);
	}

	// 获取视频信息内部类
	class VideoItemPageProcessor implements PageProcessor {

		@Override
		public void process(Page page) {
			System.out.println(page.getUrl());
//			System.out.println(page.getHtml());
			// 获取课程名称
			ImoocDownloader.this.courseName = page.getHtml().xpath("//h2[@class='l']/text()").get();

			List<Selectable> list = page.getHtml().xpath("//a[@class='J-media-item']").nodes();
			for (Selectable a : list) {
				String href = a.xpath("/a/@href").get();
				String code = href.substring(href.lastIndexOf("/") + 1);
				String name = a.xpath("/a/text()").get().replace(":", "'").replace(" ", "");
				VideoItem vi = new VideoItem(code, name);
				ImoocDownloader.this.videoItems.add(vi);// 加入列表
			}
		}

		private Site site = Site.me().setRetryTimes(10).setSleepTime(300);

		@Override
		public Site getSite() {
			site.setUserAgent(
					"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.93 Safari/537.36");
			site.setDomain("www.imooc.com");
			return site;
		}

	}

	class VideoItemFileUrlPageProcessor implements PageProcessor {
		@Override
		public void process(Page page) {
			List<String> medias = new JsonPathSelector("$.data.result.mpath").selectList(page.getRawText());
			VideoItem videoItem = (VideoItem) page.getRequest().getExtra("videoItem");
			// 新改版的对顺序做了调整
			videoItem.setFileUrlUHD(medias.get(2));
			videoItem.setFileUrlHD(medias.get(1));
			videoItem.setFileUrlSD(medias.get(0));
		}

		private Site site = Site.me().setRetryTimes(10).setSleepTime(100);

		@Override
		public Site getSite() {
			site.setUserAgent(
					"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.93 Safari/537.36");
			site.setDomain("www.imooc.com");
			return site;
		}

	}
}
