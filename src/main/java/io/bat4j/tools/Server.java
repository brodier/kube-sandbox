package io.bat4j.tools;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.bat4j.jrf.JsonReactiveHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class Server implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(Server.class);

	private final int port;
	private final JsonReactiveHandler backend;
	private volatile Channel serverChannel = null;
	
	public Server(int port, JsonReactiveHandler backend) {
		this.port = port;
		this.backend = backend;
	}

	public void close() {
		Optional.ofNullable(serverChannel).ifPresent(c -> c.close());
	}
	
	public void run() {
		EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap(); // (2)
			b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class) // (3)
					.childHandler(new JsonChannelInitializer(backend))
					.option(ChannelOption.SO_BACKLOG, 128) // (5)
					.childOption(ChannelOption.SO_KEEPALIVE, true); // (6)
			try {
				// Bind and start to accept incoming connections.
				ChannelFuture f = b.bind(port).sync(); // (7)
				serverChannel = f.channel();
				// Wait until the server socket is closed.
				// In this example, this does not happen, but you can do that to gracefully
				// shut down your server.

				f.channel().closeFuture().sync();
			} catch (InterruptedException e) {
				throw new Error(e);
			}
		} finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
	}

}
