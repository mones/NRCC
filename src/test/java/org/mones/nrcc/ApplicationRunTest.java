package org.mones.nrcc;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.mones.nrcc.client.NumberWriter;

@Isolated
class ApplicationRunTest {

	@Test
	void main() {
		try {
			Thread mainThread = new Thread(Application.getInstance());
			mainThread.start();
			Thread.sleep(400); // wait for server to initialize
			// create some clients
			NumberWriter nw0 = new NumberWriter(Arrays.asList(TestData.DATA_SET_1));
			nw0.setDelayBetweeeWrites(100);
			nw0.setReplayForever(true);
			NumberWriter nw1 = new NumberWriter(Arrays.asList(TestData.DATA_SET_2));
			nw1.setDelayBetweeeWrites(200);
			nw1.setReplayForever(true);
			NumberWriter nw2 = new NumberWriter(Arrays.asList(TestData.DATA_SET_2));
			nw2.setDelayBetweeeWrites(300);
			nw2.setReplayForever(true);
			NumberWriter nw3 = new NumberWriter(Arrays.asList(TestData.DATA_SET_2));
			nw3.setDelayBetweeeWrites(400);
			nw3.setReplayForever(true);
			// finalizing client, does not replay
			NumberWriter nwf = new NumberWriter(Arrays.asList(TestData.DATA_SET_T));
			nwf.setDelayBetweeeWrites(TestData.T_WAIT);
			// and launch them all
			(new Thread(nw0)).start();
			(new Thread(nw1)).start();
			(new Thread(nw2)).start();
			(new Thread(nw3)).start();
			(new Thread(nwf)).start();
			// wait until finalizing dataset is completely sent 
			Thread.sleep(TestData.T_WAIT * TestData.DATA_SET_T.length);
			assertTrue((new File("numbers.log")).exists());
		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail();
		}
	}
}