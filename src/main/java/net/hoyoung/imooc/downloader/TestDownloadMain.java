package net.hoyoung.imooc.downloader;
/**
 * <b>function:</b> 下载测试
 * @author hoojo
 * @createDate 2011-9-23 下午05:49:46
 * @file TestDownloadMain.java
 * @package com.hoo.download
 * @project MultiThreadDownLoad
 * @blog http://blog.csdn.net/IBM_hoojo
 * @email hoojo_@126.com
 * @version 1.0
 */
public class TestDownloadMain {
 
    public static void main(String[] args) {
        /*DownloadInfo bean = new DownloadInfo("http://i7.meishichina.com/Health/UploadFiles/201109/2011092116224363.jpg");
        System.out.println(bean);
        BatchDownloadFile down = new BatchDownloadFile(bean);
        new Thread(down).start();*/
        
        //DownloadUtils.download("http://i7.meishichina.com/Health/UploadFiles/201109/2011092116224363.jpg");
        DownloadUtils.download("http://v1.mukewang.com/1c4a8fe3-1fa4-44a5-9b4b-31e6d71f39f9/H.mp4", "9904.mp4", "temp", 5);
    }
}