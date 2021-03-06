package com.coding.download;

import java.io.File;
import java.util.concurrent.CyclicBarrier;

import com.coding.download.api.Connection;
import com.coding.download.api.ConnectionException;
import com.coding.download.api.ConnectionManager;
import com.coding.download.api.DownloadListener;
import com.coding.util.IOUtils;




public class FileDownloader {
	
	String url;
	
	DownloadListener listener;
	
	ConnectionManager cm;
	
	private static String localFile = "c:/test/test.jpg"; 
	private final static int MAX_THREAD_NUM = 3;
	

	public FileDownloader(String _url) {
		this.url = _url;
		
	}
	
	public void execute(){
		// 在这里实现你的代码， 注意： 需要用多线程实现下载
		// 这个类依赖于其他几个接口, 你需要写这几个接口的实现代码
		// (1) ConnectionManager , 可以打开一个连接，通过Connection可以读取其中的一段（用startPos, endPos来指定）
		// (2) DownloadListener, 由于是多线程下载， 调用这个类的客户端不知道什么时候结束，所以你需要实现当所有
		//     线程都执行完以后， 调用listener的notifiedFinished方法， 这样客户端就能收到通知。
		// 具体的实现思路：
		// 1. 需要调用ConnectionManager的open方法打开连接， 然后通过Connection.getContentLength方法获得文件的长度
		// 2. 至少启动3个线程下载，  注意每个线程需要先调用ConnectionManager的open方法
		// 然后调用read方法， read方法中有读取文件的开始位置和结束位置的参数， 返回值是byte[]数组
		// 3. 把byte数组写入到文件中
		// 4. 所有的线程都下载完成以后， 需要调用listener的notifiedFinished方法
		
		// 下面的代码是示例代码， 也就是说只有一个线程， 你需要改造成多线程的。
		/* my
		try {
			Connection conn = cm.open(url);
			int length = conn.getContentLength();
			File file = new File(localFile);
			IOUtils.createFile(length, localFile);
			Resource res = new Resource(url,file);
			Thread c = new CreateThread(res,length);
			Thread r = new RemoveThread(res,listener);
			c.start();
			r.start();
		} catch (ConnectionException e) {
			e.printStackTrace();
		} 
		*/
		try {
			CyclicBarrier barrier = new CyclicBarrier(MAX_THREAD_NUM, new Runnable() {
				@Override
				public void run() {
					listener.notifyFinished();
				}
			});
			Connection conn = cm.open(url);
			int length = conn.getContentLength();
			IOUtils.createFile(length, localFile);
			File file = new File(localFile);
			int size = length/MAX_THREAD_NUM;
			int last = length%MAX_THREAD_NUM;
			for(int i=0;i<MAX_THREAD_NUM;i++){
				int startPos = i*size;
				int endPos = (i+1)*size-1;
				endPos = i==(MAX_THREAD_NUM-1)?(endPos+last):endPos;
				DownloadThread dt = new DownloadThread(cm.open(url),startPos ,endPos , file, barrier);
				dt.start();
			}
		} catch (ConnectionException e) {
			e.printStackTrace();
		} 
	}
	
	public void setListener(DownloadListener listener) {
		this.listener = listener;
	}

	
	
	public void setConnectionManager(ConnectionManager ucm){
		this.cm = ucm;
	}
	
	public DownloadListener getListener(){
		return this.listener;
	}
	
}
