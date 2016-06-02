package com.nettytest;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static org.jboss.netty.handler.codec.http.HttpMethod.GET;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.METHOD_NOT_ALLOWED;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.frame.TooLongFrameException;
import org.jboss.netty.handler.codec.http.DefaultHttpChunk;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.util.CharsetUtil;

public class HttpChunkedServerHandler extends SimpleChannelUpstreamHandler {
	private static final AtomicInteger count = new AtomicInteger(0);

	private void increment() {
		System.out.format("online user %d\n", count.incrementAndGet());
	}

	private void decrement() {
		if (count.get() <= 0) {
			System.out.format("~online user %d\n", 0);
		} else {
			System.out.format("~online user %d\n", count.decrementAndGet());
		}
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		HttpRequest request = (HttpRequest) e.getMessage();
		if (request.getMethod() != GET) {
			sendError(ctx, METHOD_NOT_ALLOWED);
			return;
		}

		sendPrepare(ctx);
		increment();
	}

	@Override
	public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		decrement();
		super.channelDisconnected(ctx, e);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		Throwable cause = e.getCause();
		if (cause instanceof TooLongFrameException) {
			sendError(ctx, BAD_REQUEST);
			return;
		}
	}

	private static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
		HttpResponse response = new DefaultHttpResponse(HTTP_1_1, status);
		response.setHeader(CONTENT_TYPE, "text/plain; charset=UTF-8");
		response.setContent(ChannelBuffers.copiedBuffer("Failure: " + status.toString() + "\r\n", CharsetUtil.UTF_8));

		// Close the connection as soon as the error message is sent.
		ctx.getChannel().write(response).addListener(ChannelFutureListener.CLOSE);
	}

	private void sendPrepare(ChannelHandlerContext ctx) {
		HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
		response.setChunked(true);
		response.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/html; charset=UTF-8");
		response.addHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
		response.setHeader(HttpHeaders.Names.TRANSFER_ENCODING, HttpHeaders.Values.CHUNKED);

		Channel chan = ctx.getChannel();
		chan.write(response);

		// 缓冲必须凑够256字节，浏览器端才能够正常接收 ...
		StringBuilder builder = new StringBuilder();
		builder.append("<html><body><script>var _ = function (msg) { parent.s._(msg, document); };</script>");
		int leftChars = 256 - builder.length();
		for (int i = 0; i < leftChars; i++) {
			builder.append(" ");
		}

		writeStringChunk(chan, builder.toString());
	}

	private void writeStringChunk(Channel channel, String data) {
		ChannelBuffer chunkContent = ChannelBuffers.dynamicBuffer(channel.getConfig().getBufferFactory());
		chunkContent.writeBytes(data.getBytes());
		HttpChunk chunk = new DefaultHttpChunk(chunkContent);

		channel.write(chunk);
	}
}
