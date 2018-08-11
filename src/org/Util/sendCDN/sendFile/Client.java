package org.Util.sendCDN.sendFile;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.util.LinkedList;

public class Client implements Runnable{
	private int port;
	private String host;
	private byte[] bytes;
	private String range;
	private String filename;
	public Client(int port, String host,byte[] bytes,String range,String filename){
		this.port=port;
		this.host=host;
		this.bytes=bytes;
		this.range=range;
		this.filename=filename;
	}
	public void run(){
		// 配置客户端NIO线程组
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			Bootstrap b = new Bootstrap();
			b.group(group).channel(NioSocketChannel.class)
					.option(ChannelOption.TCP_NODELAY, true)
					.handler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch)
								throws Exception {
							//client端接收的是httpResponse，所以用HttpResponse进行解码
							ch.pipeline().addLast(new HttpResponseDecoder());
							ch.pipeline().addLast(new HttpObjectAggregator(65535));
							//client端发送的是httpResponse，所以用HttpRequest进行编码
							ch.pipeline().addLast(new HttpRequestEncoder());
							ch.pipeline().addLast(new ChunkedWriteHandler());
							ch.pipeline().addLast(new ClientHandler(bytes,range,filename));
						}
					});
			// 发起异步连接操作
			ChannelFuture f = b.connect(host, port).sync();

			// 等待客户端链路关闭
			f.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			// 优雅退出，释放NIO线程组
			group.shutdownGracefully();
		}
	}

}
