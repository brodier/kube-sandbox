package io.bat4j.tools;

import java.net.InetSocketAddress;
import java.util.ServiceConfigurationError;
import java.util.concurrent.CompletableFuture;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import io.bat4j.jrf.JsonReactiveHandler;

public class ServiceFactory {

	
	public JsonReactiveHandler getMain(Config c) {
		String mainSvc = c.getString("main");
		if(mainSvc.equals("hash")) {
			return new DefaultHashService();
		}
		
		if(mainSvc.equals("cli")) {
			return new Client(new InetSocketAddress(c.getString("remote"), c.getInt("port")));
		}

		if(mainSvc.equals("srv")) {
			new Thread(new Server(c.getInt("port"), new DefaultHashService())).start();
			return r -> CompletableFuture.completedFuture(r);
		}
		
		throw new ServiceConfigurationError("Invalid service configuration");
	}
}
