package tools;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import javax.json.JsonValue;

import org.junit.Test;

import io.bat4j.tools.Client;
import io.bat4j.tools.Server;
import io.bat4j.tools.Tools;

public class ClientServerTest {

	@Test
	public void test() throws Exception {
		final Server s = new Server(2323, CompletableFuture::completedFuture);
		Thread st = new Thread(s);
		st.start();
		final Client c = new Client(new InetSocketAddress("localhost", 2323));
		final CompletionStage<Client> connectionStage = c.connect();
		final Client connectedClient = connectionStage.toCompletableFuture().get();
		connectionStage.thenCompose(cli -> {
			JsonValue rq = Tools.JSP.createValue("test");
			return cli.handle(rq);
		}).thenCompose(reply -> {
			connectedClient.handle(JsonValue.EMPTY_JSON_OBJECT);
			s.close();
			return CompletableFuture.completedStage(reply);
		});
		st.join();
	}

}
