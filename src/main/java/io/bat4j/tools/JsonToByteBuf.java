package io.bat4j.tools;

import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.json.JsonValue;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParsingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.codec.MessageToMessageCodec;

@Sharable
public class JsonToByteBuf extends MessageToMessageCodec<ByteBuf,JsonValue>{
	private static final Logger logger = LoggerFactory.getLogger(JsonToByteBuf.class);
	
	@Override
	protected void encode(ChannelHandlerContext ctx, JsonValue msg, List<Object> out) throws Exception {
		logger.info("Sending : " + msg.toString());
		out.add(Unpooled.wrappedBuffer(msg.toString().getBytes(StandardCharsets.UTF_8)));
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
		logger.info("Recieving : " + new String(new ByteBufInputStream( msg.slice()).readAllBytes(), StandardCharsets.UTF_8));

		try {
			JsonParser p = Tools.JSP.createParser(new ByteBufInputStream(msg));
			p.next();
			out.add(p.getValue());
		} catch(JsonParsingException e) {
			logger.error("Failed to parse json", e);
		}
	}

}
