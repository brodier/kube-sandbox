package tools;

import org.junit.Test;

import io.bat4j.tools.DefaultHashService;
import io.bat4j.tools.TestingService;

public class TestHashService {

	@Test
	public void testHashService() throws Exception {
		new TestingService(new DefaultHashService(), new DefaultHashService()).run();
	}
}
