package org.Util.sendCDN.sendFile;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.Util.StaticLangue.Lang;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Logger;

import static io.netty.handler.codec.http.HttpHeaderNames.*;

public class ClientHandler extends ChannelHandlerAdapter {

	private static final Logger logger = Logger
			.getLogger(ClientHandler.class.getName());
	private byte[] bytes;
	private String range;
	private String filename;
	/**
	 * Creates a client-side handler.
	 */
	public ClientHandler(byte[] bytes,String range,String filename) {
		this.bytes=bytes;
		this.range=range;
		this.filename=filename;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws IOException {
		//向CDN上传文件
		FullHttpRequest request=new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,HttpMethod.POST,"/"+filename);
		request.headers().set(CONTENT_TYPE,"multipart/form-data");
		request.headers().set(CONNECTION,KEEP_ALIVE);
		request.headers().set(ACCEPT,"*/*");
		request.headers().set(RANGE,range);
		request.headers().setInt(CONTENT_LENGTH,bytes.length);
		ByteBuf byteBuf= Unpooled.wrappedBuffer(bytes);
		request.content().writeBytes(byteBuf);
		ctx.writeAndFlush(request);
		logger.info("上传文件的报文: "+request.toString());
	}
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		FullHttpResponse response= (FullHttpResponse) msg;
		if (response.status().code()==200) {
			String range = (String) response.headers().get(CONTENT_RANGE);
			String content = response.content().toString(CharsetUtil.UTF_8);
			JSONObject object = JSON.parseObject(content);
			String result = object.getString("result");
			if (result.equals(Lang.OK)) {
				String filename = object.getString("filename");
				logger.info("CDN已接收到文件,文件名: " + filename + ",Range: " + range);
			}else {
				logger.info("出现错误。CDN未接收文件");
			}
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.close();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		// 释放资源
		logger.warning("Unexpected exception from downstream : "
				+ cause.getMessage());
		ctx.close();
	}
}
