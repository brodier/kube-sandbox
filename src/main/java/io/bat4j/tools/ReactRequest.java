package io.bat4j.tools;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.json.JsonValue;

public class ReactRequest {

	private final JsonValue request;
	private final CompletableFuture<JsonValue> replyFuture;
	
	public ReactRequest(JsonValue rq) {
		this.request = rq;
		replyFuture = new CompletableFuture<JsonValue>();
	}
	
	public JsonValue getRequest() {
		return request;
	}
	
	public void setResult(JsonValue result) {
		replyFuture.complete(result);
	}
	
	public CompletionStage<JsonValue> getFututre() {
		return replyFuture;
	}
	
	public void setFailure(Throwable ex) {
		replyFuture.completeExceptionally(ex);
	}
	
}
