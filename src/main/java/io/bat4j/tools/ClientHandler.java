package io.bat4j.tools;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import javax.json.JsonValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

public class ClientHandler extends ChannelDuplexHandler {
    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);
    
    private final AtomicReference<ReactRequest> pendingRequest = new AtomicReference<>();

    public ClientHandler() {
		
	}
	
	@Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) { // (2)
    	logger.info("-Receiving reply");
    	ReactRequest curRq = pendingRequest.get();
    	Optional.ofNullable(curRq).ifPresent(pr -> pr.setResult((JsonValue)msg));
    	pendingRequest.compareAndSet(curRq, null);
    	
    }
	
	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
		if( ! (msg instanceof ReactRequest) ) {
			return;
		}
		ReactRequest rq = (ReactRequest) msg;
		if(pendingRequest.compareAndSet(null, rq)) {
	    	logger.info("Sending request");
			ctx.writeAndFlush(rq.getRequest());	
		} else {
	    	logger.info("Failed to send request");
			rq.setFailure(new IllegalStateException());
		}
		
	}
}
