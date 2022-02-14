package io.bat4j.tools;

import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.json.JsonValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.bat4j.jrf.JsonReactiveHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class Client implements JsonReactiveHandler {
	private static final Logger logger = LoggerFactory.getLogger(Client.class);

	private final Bootstrap b;
	private final InetSocketAddress endpoint;
	private final Channel ch;

	public Client(InetSocketAddress endpoint) {
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		b = new Bootstrap();
		b.group(workerGroup);
		b.channel(NioSocketChannel.class);
		b.option(ChannelOption.SO_KEEPALIVE, true);
		b.handler(new JsonChannelInitializer());
		this.endpoint = endpoint;
		ch = null;
	}

	private Client(Client from, Channel ch) {
		b = from.b;
		endpoint = from.endpoint;
		this.ch = ch;
	}

	public CompletionStage<Client> connect() {
		ChannelFuture f = b.connect(endpoint);
		CompletableFuture<Client> connectedFuture = new CompletableFuture<>();
		f.addListener((ChannelFutureListener) (cf -> this.connectionComplete(connectedFuture, cf)));
		f.channel().closeFuture().addListener((ChannelFutureListener) this::channelClosed);
		return connectedFuture;
	}

	private void connectionComplete(CompletableFuture<Client> connectedFuture, ChannelFuture f) {
		if (f.isSuccess()) {
			logger.info("Client connected");
		}
		connectedFuture.complete(new Client(this, f.channel()));
	}

	private void channelClosed(ChannelFuture f) {
		logger.info("Channel " + f.channel() + " is closed");
	}

	@Override
	public CompletionStage<JsonValue> handle(JsonValue msg) {
		if (Optional.ofNullable(ch).isEmpty()) {
			logger.warn("Client not yet connected");
			return CompletableFuture.failedStage(new IllegalStateException());
		}
		if (JsonValue.EMPTY_JSON_OBJECT.equals(msg)) {
			ch.close();
			return CompletableFuture.completedFuture(JsonValue.EMPTY_JSON_OBJECT);
		}
		ReactRequest rq = new ReactRequest(msg);
		ch.writeAndFlush(rq);
		return rq.getFututre();
	}

}
