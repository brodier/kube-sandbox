package io.bat4j.tools;

import java.util.Optional;

import javax.json.JsonValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.bat4j.jrf.JsonReactiveHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ServerHandler extends ChannelInboundHandlerAdapter {
	private static final Logger logger = LoggerFactory.getLogger(ServerHandler.class);
	
	private final JsonReactiveHandler backend;
	
	public ServerHandler(JsonReactiveHandler backend) {
		this.backend = backend;
	}
	
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) { // (2)
    	if(! (msg instanceof JsonValue)) {
    		logger.warn("not recieving JsonValue object : " + msg); 
    		return;
    	}
    	JsonValue rq = (JsonValue) msg;
    	logger.info("Recieve rq : " + rq);
    	backend.handle(rq).whenComplete((r,e) -> this.handleReply(ctx, r, e));
    }
    
    private void handleReply(ChannelHandlerContext ctx,  JsonValue reply, Throwable exception) {
    	if(Optional.ofNullable(exception).isPresent()) {
    		logger.error("Exception during backend processing", exception);
    		return;
    	}
    	ctx.writeAndFlush(reply);
    }
}
