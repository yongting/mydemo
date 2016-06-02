package com.nettytest.netty5;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

public class TimeClientHandler extends ChannelHandlerAdapter{
	public TimeClientHandler() {
//		byte[] req = "".getBytes();
	}
	
	public void channelActive(ChannelHandlerContext ctx) {
		ByteBuf firstMessage = Unpooled.buffer("test rq".getBytes().length);
		firstMessage.writeBytes("test rq".getBytes());
		ctx.writeAndFlush(firstMessage);
	}
	
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf buf = (ByteBuf) msg;
		byte[] req = new byte[buf.readableBytes()];
		buf.readBytes(req);
		System.out.println("from svr: " + new String(req, "utf-8"));
	}
	
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		ctx.close();
	}
}
