package com.nettytest.bio;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client extends Thread {
	Socket socket = null;
	BufferedReader reader = null;
	PrintWriter wtr = null;
	BufferedReader keyin = null;

	public Client() {
		keyin = new BufferedReader(new InputStreamReader(System.in));
		try {
			socket = new Socket("127.0.0.1", 8000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() {
		try {
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			wtr = new PrintWriter(socket.getOutputStream());
			String get = keyin.readLine();

			while (true) {
				if (get != null & get.length() > 0) {
					wtr.println(get);
					wtr.flush();
					// wtr.close();
					// System.out.println(get + "发送完毕");
				}
				if (reader != null) {
					String line = reader.readLine();
					System.out.println("从服务器来的信息：" + line);

				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new Client().start();
	}
}
