package io.bat4j.jrf;

public interface JsonReactiveService extends JsonReactiveHandler {

	public void stop();
	
	public boolean isReady();
}
