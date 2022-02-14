package io.bat4j.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.spi.JsonProvider;
import javax.json.stream.JsonParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import io.bat4j.jrf.JsonReactiveHandler;

public class Application {
	private static final Logger logger = LoggerFactory.getLogger(Application.class);
	private static final int HTTP_FOUND = 200;
	
	//private static final int DEFAULT_PORT = 22222;
	private static final int DEFAULT_HTTP_PORT = 80;
	private static final int MAX_INCOMMING_CONNEXION = 50;
	private final Thread shutdownHook;
	private final JsonReactiveHandler backend;

	public Application(Config c) {
		try {
			backend = new ServiceFactory().getMain(c);
			int httpPort = c.hasPath("http.port") ? c.getInt("http.port") : DEFAULT_HTTP_PORT ;
			
			logger.info("Starting http server on port : {}", httpPort);
			final HttpServer server = HttpServer.create(new InetSocketAddress(httpPort), MAX_INCOMMING_CONNEXION);
			shutdownHook = new Thread(() ->  this.stop(server));
			server.createContext("/health", this::health);
			server.createContext("/stop", this::shutdown);
			server.createContext("/", this::handleRq);
			server.setExecutor(null); // creates a default executor
			server.start();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		/*
		if (Optional.ofNullable(endpointHost).isPresent()) {
			Client cl = new Client(new InetSocketAddress(endpointHost, DEFAULT_PORT));
			// srv.bind(addr, backlog);
		} else {
			logger.info("Server mode not yet implemented");
		}
		*/
	}

	private void health(HttpExchange exchange) {
		try {
			exchange.getRequestBody().close();
			JsonObject status = Tools.JSP.createObjectBuilder().add("STATUS", "OK").build();
			exchange.getResponseHeaders().add("Content-Type", "application/json");
			exchange.sendResponseHeaders(HTTP_FOUND, status.toString().length());
			OutputStream out = exchange.getResponseBody();
			out.write(status.toString().getBytes());
			out.close();
			logger.info("Replying ok to health request");
		} catch (IOException e) {
			logger.error("Failed to reply to health request", e);
			return;
		}

	}

	private void handleRq(HttpExchange exchange) {
		try {
			String method = exchange.getRequestMethod();
			InputStream in = exchange.getRequestBody();
			JsonParser p = Tools.JSP.createParser(in);
			p.next();
			JsonValue rq = p.getValue();
			in.close();
			backend.handle(rq).whenComplete((r,e) -> this.sendReply(exchange, r, e));
		} catch (Exception e) {
			logger.error("Failed to reply to default request", e);
			return;
		}

	}

	private void sendReply(HttpExchange httpCtx, JsonValue result, Throwable exception) {
		try {
			if(Optional.ofNullable(exception).isPresent()) {
				logger.error("Exception in backend", exception);
				httpCtx.sendResponseHeaders(500, 0);
				httpCtx.getResponseBody().close();
				return;
			}
			Headers hdrs = httpCtx.getResponseHeaders();
			hdrs.add("Content-Type", "application/json");
			httpCtx.sendResponseHeaders(HTTP_FOUND, result.toString().length());
			OutputStream out = httpCtx.getResponseBody();
			out.write(result.toString().getBytes());
			out.close();
		} catch (IOException e) {
			logger.error("Exception on posting reply", e);
		}
	}
	
	private void shutdown(HttpExchange exchange) {
		shutdownHook.start();  
		try {
			exchange.getRequestBody().close();
			JsonObject status = Tools.JSP.createObjectBuilder().add("STATUS", "SHUTTING DOWN").build();
			exchange.getResponseHeaders().add("Content-Type", "application/json");
			exchange.sendResponseHeaders(HTTP_FOUND, status.toString().length());
			OutputStream out = exchange.getResponseBody();
			out.write(status.toString().getBytes());
			out.close();
			logger.info("Replying ok to shutdown request, server stopping");
		} catch (IOException e) {
			logger.error("Failed to reply to health request", e);
			return;
		}
	}
	
	private void stop(HttpServer s) {
		logger.info("Stopping Server");
		s.stop(3);
		logger.info("Server stop");
	}
	
	public static void main(String[] args) throws InterruptedException {
		logger.info("Application booting");
		new Application(ConfigFactory.load());
	}
	
	
}
