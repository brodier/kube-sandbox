package io.bat4j.tools;


import java.util.Base64;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.json.JsonValue;

import io.bat4j.jrf.JsonReactiveHandler;

public class TestingService {

	private final JsonReactiveHandler service;
	private final JsonReactiveHandler validator;
	
	public TestingService(JsonReactiveHandler testedService, JsonReactiveHandler validationService) {
		this.service = testedService;
		this.validator = validationService;
	}
	
	public void run() throws Exception {
		Random rand = new Random(System.currentTimeMillis());
		byte[] data = new byte[128];
		CompletionStage<Boolean> globalResult = CompletableFuture.completedStage(true);
		for(int i = 0; i < 1000; i++) {
			rand.nextBytes(data);
			final JsonValue rq = Tools.JSP.createValue(Base64.getEncoder().encodeToString(data));
			final CompletionStage<JsonValue> rpFuture = service.handle(rq);
			CompletionStage<Boolean> result = validator.handle(rq).thenCombine(rpFuture, (e,a) -> { return e.equals(a);});
			globalResult = globalResult.thenCombine(result, (r,g) -> { return r && g; });
		}
		globalResult.toCompletableFuture().get();
	}
	
}
