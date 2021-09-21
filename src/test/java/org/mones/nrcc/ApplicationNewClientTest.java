package org.mones.nrcc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.mones.nrcc.client.NumberWriter;

@Isolated
class ApplicationNewClientTest {

	@Test
	void newClient() {
		try {
			Application app = Application.getInstance();
			Thread mainThread = new Thread(app);
			mainThread.start();
			Thread.sleep(400); // wait for server to initialize

			assertEquals(0, app.getClientThreadsSize());
			assertEquals(0, app.getRunningClients());
			// finalizing client
			NumberWriter nwf = new NumberWriter(Arrays.asList(TestData.DATA_SET_K));
			nwf.run();
			assertEquals(1, app.getClientThreadsSize());
			assertEquals(1, app.getRunningClients());
			Thread.sleep(200);
			assertEquals(0, app.getClientThreadsSize());
			assertEquals(0, app.getRunningClients());
		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail();
		}
	}
}