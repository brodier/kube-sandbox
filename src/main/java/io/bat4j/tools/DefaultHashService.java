package io.bat4j.tools;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.json.JsonValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.bat4j.jrf.JsonReactiveHandler;

public class DefaultHashService implements JsonReactiveHandler {
	private static final Logger logger = LoggerFactory.getLogger(DefaultHashService.class);
	
	private final MessageDigest hashEngine;
	private final Encoder b64Encoder = Base64.getEncoder();

	public DefaultHashService() {
		try {
			hashEngine = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw new UnsupportedOperationException(e);
		}
	}

	@Override
	public CompletionStage<JsonValue> handle(JsonValue request) {
		String rq = request.toString();
		String hash = b64Encoder.encodeToString(hashEngine.digest(rq.getBytes(StandardCharsets.UTF_8)));
		logger.info("hashing {} => {}", rq, hash);
		return CompletableFuture.completedFuture(Tools.JSP.createValue(hash));
	}

}
