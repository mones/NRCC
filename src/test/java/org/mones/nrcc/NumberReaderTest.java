package org.mones.nrcc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.mones.nrcc.client.NumberWriter;
import org.mones.nrcc.common.Const;

@Isolated
class NumberReaderTest {

	public FutureTask<Socket> newWaitClientTask() {
		return new FutureTask<Socket>(
			new Callable<Socket>() {
				public Socket call() {
					try {
						ServerSocket server = new ServerSocket(Const.SERVER_PORT);
						Socket client = server.accept();
						server.close();
						return client;
					} catch (IOException e) {
						e.printStackTrace();
						return null;
					}
				}
			});
	}

	@Test
	void testRun() {
		try {
			FutureTask<Socket> waitClientTask = newWaitClientTask();
			ExecutorService executor = Executors.newSingleThreadExecutor();
			executor.submit(waitClientTask);
			// now the client
			NumberWriter nwf = new NumberWriter(Arrays.asList(TestData.DATA_SET_K));
			Thread writer = new Thread(nwf);
			writer.start();
			NumberReader nr = new NumberReader(waitClientTask.get(), 0);
			nr.run();
			assertEquals(0, nr.getClientId());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	void testDisconnect() {
		try {
			FutureTask<Socket> waitClientTask = newWaitClientTask();
			ExecutorService executor = Executors.newSingleThreadExecutor();
			executor.submit(waitClientTask);
			// now the client
			NumberWriter nwf = new NumberWriter(Arrays.asList(TestData.DATA_SET_K));
			Thread writer = new Thread(nwf);
			writer.start();
			NumberReader nr = new NumberReader(waitClientTask.get(), 0);
			assertFalse(nr.isClosed());
			nr.disconnect();
			assertTrue(nr.isClosed());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
}
