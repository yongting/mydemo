package com.nettytest.netty5;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class TimeClient {
	public void connect(int port, String host) throws Exception {
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			Bootstrap b = new Bootstrap();
			b.group(group).channel(NioSocketChannel.class)
				.option(ChannelOption.TCP_NODELAY, true)
				.handler(new ChannelInitializer<SocketChannel>() {

					@Override
					protected void initChannel(SocketChannel ch) throws Exception {
						// TODO Auto-generated method stub
						ch.pipeline().addLast(new TimeClientHandler());
					}
				});
			
			ChannelFuture f = b.connect(host, port).sync();
			
			f.channel().closeFuture().sync();
		} finally {
			group.shutdownGracefully();
		}
	}
	
	public static void main(String [] args) throws Exception {
		new TimeClient().connect(8888, "127.0.0.1");
	}
}
