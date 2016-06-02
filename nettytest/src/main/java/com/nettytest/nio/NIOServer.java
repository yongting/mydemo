package com.nettytest.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class NIOServer {
	private int port = 8888;
	// 解码buffer
	private Charset cs = Charset.forName("gbk");
	/* 接受数据缓冲区 */
	private static ByteBuffer sBuffer = ByteBuffer.allocate(1024);
	/* 发送数据缓冲区 */
	private static ByteBuffer rBuffer = ByteBuffer.allocate(1024);
	/* 映射客户端channel */
	private Map<String, SocketChannel> clientsMap = new HashMap<String, SocketChannel>();
	private static Selector selector;

	public NIOServer(int port) {
		this.port = port;
		try {
			init();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void init() throws IOException {
		/*
		 * 启动服务器端，配置为非阻塞，绑定端口，注册accept事件 ACCEPT事件：当服务端收到客户端连接请求时，触发该事件
		 */
		ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.configureBlocking(false);
//		ServerSocket serverSocket = serverSocketChannel.socket();
//		serverSocket.bind(new InetSocketAddress(port));
		serverSocketChannel.bind(new InetSocketAddress(port));
		selector = Selector.open();
		serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		System.out.println("server start on port:" + port);
	}

	/**
	 * 服务器端轮询监听，select方法会一直阻塞直到有相关事件发生或超时
	 */
	private void listen() {
		while (true) {
			try {
				selector.select();// 返回值为本次触发的事件数
				Set<SelectionKey> selectionKeys = selector.selectedKeys();
				for (SelectionKey key : selectionKeys) {
					handle(key);
				}
				selectionKeys.clear();// 清除处理过的事件
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}

		}
	}

	/**
	 * 处理不同的事件
	 */
	private void handle(SelectionKey selectionKey) throws IOException {
		ServerSocketChannel server = null;
		SocketChannel client = null;
		String receiveText = null;
		int count = 0;
		if (selectionKey.isAcceptable()) {
			/*
			 * 客户端请求连接事件 serversocket为该客户端建立socket连接，将此socket注册READ事件，监听客户端输入
			 * READ事件：当客户端发来数据，并已被服务器控制线程正确读取时，触发该事件
			 */
			server = (ServerSocketChannel) selectionKey.channel();
			client = server.accept();
			client.configureBlocking(false);
			client.register(selector, SelectionKey.OP_READ);
			System.out.println("接受到一个新连接："+client);
		} else if (selectionKey.isReadable()) {
			/*
			 * READ事件，收到客户端发送数据，读取数据后继续注册监听客户端
			 */
			client = (SocketChannel) selectionKey.channel();
			System.out.println("从连接读取数据："+client);
			rBuffer.clear();
			count = client.read(rBuffer);
			if (count > 0) {
				rBuffer.flip();
				receiveText = String.valueOf(cs.decode(rBuffer).array());
				System.out.println("读取数据"+client.toString() + ":" + receiveText);
				dispatch(client, receiveText);
				client = (SocketChannel) selectionKey.channel();
				client.register(selector, SelectionKey.OP_READ);
			}
		}
	}

	/**
	 * 把当前客户端信息 推送到其他客户端
	 */
	private void dispatch(SocketChannel client, String info) throws IOException {
		Socket s = client.socket();
		String name = "[" + s.getInetAddress().toString().substring(1) + ":" + Integer.toHexString(client.hashCode())
				+ "]";
		if (!clientsMap.isEmpty()) {
			for (Map.Entry<String, SocketChannel> entry : clientsMap.entrySet()) {
				SocketChannel temp = entry.getValue();
				if (client.equals(temp)) {
					sBuffer.clear();
					sBuffer.put((name + ":" + info).getBytes());
					sBuffer.flip();
					// 输出到通道
					System.out.println("输出到通道"+(name + ":" + info));
					temp.write(sBuffer);
				}
			}
		}
		clientsMap.put(name, client);
	}

	public static void main(String[] args) throws IOException {
		NIOServer server = new NIOServer(8888);
		server.listen();
	}
}
