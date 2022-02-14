package io.bat4j.jrf;

import java.util.concurrent.CompletionStage;

import javax.json.JsonValue;

@FunctionalInterface
public interface JsonReactiveHandler {

	public CompletionStage<JsonValue> handle(JsonValue request);
	
}
