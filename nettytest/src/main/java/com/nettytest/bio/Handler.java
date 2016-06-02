package com.nettytest.bio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Handler implements Runnable {
	private Socket socket;

	public Handler(Socket socket) {
		this.socket = socket;
	}

	public void run() {
		try {
			BufferedReader rdr = null;
			PrintWriter wtr = null;

			wtr = new PrintWriter(socket.getOutputStream());
			rdr = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String line = rdr.readLine();
			System.out.println("从客户端来的信息：" + line);
			// 特别，下面这句得加上 “\n”,
			wtr.println("你好，服务器已经收到您的信息！'" + line + "'\n");
			wtr.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				System.out.println("关闭连接:" + socket.getInetAddress() + ":" + socket.getPort());
				if (socket != null)
					socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}