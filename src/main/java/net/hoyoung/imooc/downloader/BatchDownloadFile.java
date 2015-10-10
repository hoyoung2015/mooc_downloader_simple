package net.hoyoung.imooc.downloader;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <b>function:</b> 分批量下载文件
 * @author hoojo
 * @createDate 2011-9-22 下午05:51:54
 * @file BatchDownloadFile.java
 * @package com.hoo.download
 * @project MultiThreadDownLoad
 * @blog http://blog.csdn.net/IBM_hoojo
 * @email hoojo_@126.com
 * @version 1.0
 */
public class BatchDownloadFile implements Runnable {
	protected Logger logger = LoggerFactory.getLogger(getClass());
    //下载文件信息 
    private DownloadInfo downloadInfo;
    //一组开始下载位置
    private long[] startPos;
    //一组结束下载位置
    private long[] endPos;
    
    
  //一组开始下载位置，读取断点
    private long[] startPos_now;
    //一组结束下载位置，读取断点
    private long[] endPos_now;
    
    
    //休眠时间
    private static final int SLEEP_SECONDS = 500;
    //子线程下载
    private DownloadFile[] fileItem;
    //文件长度
    private long length;
    //是否第一个文件
    private boolean first = true;
    //是否停止下载
    private boolean stop = false;
    //临时文件信息
    private File tempFile;
    
    private ConsoleProgressBar consoleProgressBar;//控制台进度条
    
    public BatchDownloadFile(DownloadInfo downloadInfo) {
        this.downloadInfo = downloadInfo;
        String tempPath = this.downloadInfo.getFilePath() + File.separator + downloadInfo.getFileName() + ".position";
        tempFile = new File(tempPath);
        consoleProgressBar = new ConsoleProgressBar(0, 100, 20, '=',">"+this.downloadInfo.getFileName());//前缀为文件名
        //如果存在读入点位置的文件
        if (tempFile.exists()) {
            first = false;
            //就直接读取内容
            try {
                readPosInfo();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            //数组的长度就要分成多少段的数量
            startPos = new long[downloadInfo.getSplitter()];
            endPos = new long[downloadInfo.getSplitter()];
        }
    }
    
    @Override
    public void run() {
        //首次下载，获取下载文件长度
        if (first) {
            length = this.getFileSize();//获取文件长度
            System.out.print("开始下载 "+this.downloadInfo.getFileName());
            System.out.print("，"+(this.length/1024/1024)+"M");
            if (length == -1) {
                logger.info("file length is know!");
                stop = true;
            } else if (length == -2) {
                logger.info("read file length is error!");
                stop = true;
            } else if (length > 0) {
                /**
                 * eg 
                 * start: 1, 3, 5, 7, 9
                 * end: 3, 5, 7, 9, length
                 */
                for (int i = 0, len = startPos.length; i < len; i++) {
                    long size = i * (length / len);
                    startPos[i] = size;
                    
                    //设置最后一个结束点的位置
                    if (i == len - 1) {
                        endPos[i] = length;
                    } else {
                        size = (i + 1) * (length / len);
                        endPos[i] = size;
                    }
                    logger.info("start-end Position[" + i + "]: " + startPos[i] + "-" + endPos[i]);
                }
                this.startPos_now = this.startPos;
            } else {
                logger.info("get file length is error, download is stop!");
                stop = true;
            }
        }
        
        //子线程开始下载
        if (!stop) {
            //创建单线程下载对象数组
            fileItem = new DownloadFile[startPos.length];//startPos.length = downloadInfo.getSplitter()
            for (int i = 0; i < startPos.length; i++) {
                try {
                    //创建指定个数单线程下载对象，每个线程独立完成指定块内容的下载
                    fileItem[i] = new DownloadFile(
                        downloadInfo.getUrl(), 
                        this.downloadInfo.getFilePath() + File.separator + downloadInfo.getFileName(), 
                        startPos_now[i], endPos[i], i
                    );
                    fileItem[i].start();//启动线程，开始下载
                    logger.info("Thread: " + i + ", startPos: " + startPos[i] + ", endPos: " + endPos[i]);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
            //循环写入下载文件长度信息
            while (!stop) {
                try {
                    writePosInfo();
                    
                    Thread.sleep(SLEEP_SECONDS);
                    stop = true;
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                for (int i = 0; i < startPos.length; i++) {
                    if (!fileItem[i].isDownloadOver()) {
                        stop = false;
                        break;
                    }
                }
            }
            //任务完成
            logger.info("Download task is finished!");
            //删除记录信息
            this.tempFile.delete();
        }
    }
    
    /**
     * 将写入点数据保存在临时文件中
     * @author hoojo
     * @createDate 2011-9-23 下午05:25:37
     * @throws IOException
     */
    private void writePosInfo() throws IOException {
    	
    	long completeLength = 0;//总完成数
    	
        DataOutputStream dos = new DataOutputStream(new FileOutputStream(tempFile));
        dos.writeInt(startPos.length);
        for (int i = 0; i < startPos.length; i++) {
        	
        	dos.writeLong(this.startPos[i]);//记录初始的，用于恢复进度显示
//            dos.writeLong(this.endPos[i]);//记录初始的，用于恢复进度显示
        	
        	
            dos.writeLong(fileItem[i].getStartPos());
            dos.writeLong(fileItem[i].getEndPos());
            
            
            logger.info("[" + fileItem[i].getStartPos() + "#" + fileItem[i].getEndPos() + "]");
            completeLength += fileItem[i].getStartPos()-this.startPos[i];//完成的字节数
        }
        dos.close();
        
        //输出总的下载进度
        int progress = (int) Math.round(completeLength*1.0/this.length*100);//0~100
        logger.info("下载进度:"+progress);
        consoleProgressBar.show(progress);
    }
    
    /**
     * <b>function:</b>读取写入点的位置信息
     * @author hoojo
     * @createDate 2011-9-23 下午05:30:29
     * @throws IOException
     */
    private void readPosInfo() throws IOException {
        DataInputStream dis = new DataInputStream(new FileInputStream(tempFile));
        int startPosLength = dis.readInt();
        startPos = new long[startPosLength];
        endPos = new long[startPosLength];
        startPos_now = new long[startPosLength];
        endPos_now = new long[startPosLength];
        for (int i = 0; i < startPosLength; i++) {
        	startPos[i] = dis.readLong();//读取初始的
        	startPos_now[i] = dis.readLong();
        	endPos_now[i] = dis.readLong();
        	
        	endPos[i] = endPos_now[i];
        }
        dis.close();
        //恢复length，用于恢复显示进度
        this.length = this.endPos[this.endPos.length-1];
    }
    
    /**
     * <b>function:</b> 获取下载文件的长度
     * @author hoojo
     * @createDate 2011-9-26 下午12:15:08
     * @return
     */
    private int getFileSize() {
        int fileLength = -1;
        try {
            URL url = new URL(this.downloadInfo.getUrl());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            
            DownloadFile.setHeader(conn);
 
            int stateCode = conn.getResponseCode();
            //判断http status是否为HTTP/1.1 206 Partial Content或者200 OK
            if (stateCode != HttpURLConnection.HTTP_OK && stateCode != HttpURLConnection.HTTP_PARTIAL) {
                logger.info("Error Code: " + stateCode);
                return -2;
            } else if (stateCode >= 400) {
                logger.info("Error Code: " + stateCode);
                return -2;
            } else {
                //获取长度
                fileLength = conn.getContentLength();
                logger.info("FileLength: " + fileLength);
            }
            
            //读取文件长度
            /*for (int i = 1; ; i++) {
                String header = conn.getHeaderFieldKey(i);
                if (header != null) {
                    if ("Content-Length".equals(header)) {
                        fileLength = Integer.parseInt(conn.getHeaderField(i));
                        break;
                    }
                } else {
                    break;
                }
            }
            */
            
            DownloadFile.printHeader(conn);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileLength;
    }
}