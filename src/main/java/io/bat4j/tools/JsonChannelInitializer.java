package io.bat4j.tools;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.bat4j.jrf.JsonReactiveHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

public class JsonChannelInitializer extends ChannelInitializer<SocketChannel> {
	private static final Logger logger = LoggerFactory.getLogger(JsonChannelInitializer.class);
	
	private static final ChannelHandler LEN_PREPENDER= new LengthFieldPrepender(2);
	private static final ChannelHandler JSON2BUF_CODEC = new JsonToByteBuf();
	
	private final JsonReactiveHandler server;
	
	public JsonChannelInitializer() {
		server = null;
	}
	
	public JsonChannelInitializer(JsonReactiveHandler server) {
		this.server = server;
	}
	
	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline p =  ch.pipeline();
		p.addLast(LEN_PREPENDER);
		p.addLast(new LengthFieldBasedFrameDecoder(Short.MAX_VALUE, 0, 2, 0, 2));
		p.addLast(JSON2BUF_CODEC);
		if(Optional.ofNullable(server).isPresent()) {
			logger.info("Initializing Server channel");
			p.addLast(new ServerHandler(server));
		} else {
			logger.info("Initializing Client channel");
			p.addLast(new ClientHandler());
		}
	}

}
