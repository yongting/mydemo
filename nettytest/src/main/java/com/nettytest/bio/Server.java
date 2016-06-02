package com.nettytest.bio;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	private int port = 8000;
	private ServerSocket serverSocket;

	public Server() throws IOException {
		this.serverSocket = new ServerSocket(this.port, 10);
		System.out.println("服务器启动, "+this.serverSocket.getInetAddress());
	}

	public void service() {
		while(true){
	        Socket socket=null;
	        try{
	            socket = serverSocket.accept();                        //主线程获取客户端连接
	            Thread workThread = new Thread(new Handler(socket));    //创建线程
	            workThread.start();                                    //启动线程
	        }catch(Exception e){
	            e.printStackTrace();
	        }
	    }
	}
	
	public static void main(String [] args) throws IOException {
		Server server = new Server();
		server.service();
	}
}
