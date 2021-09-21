package org.mones.nrcc;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mones.nrcc.common.Const;

class DataKeeperTest {

	@Test
	void testReceive() {
		try {
			final DataKeeper keeper = DataKeeper.getInstance();
			keeper.reset(); // start always fresh
			assertEquals(0, keeper.getDuplicates());
			assertEquals(0, keeper.getLastDuplicates());
			assertEquals(0, keeper.getLastUnique());
			assertEquals(0, keeper.getUnique());
			assertEquals(0, keeper.getNumbers().size());
			keeper.receive("123456789");
			assertEquals(0, keeper.getDuplicates());
			assertEquals(0, keeper.getLastDuplicates());
			assertEquals(0, keeper.getLastUnique());
			assertEquals(1, keeper.getUnique());
			assertEquals(1, keeper.getNumbers().size());
			keeper.receive("123456789");
			assertEquals(1, keeper.getDuplicates());
			assertEquals(0, keeper.getLastDuplicates());
			assertEquals(0, keeper.getLastUnique());
			assertEquals(1, keeper.getUnique());
			assertEquals(1, keeper.getNumbers().size());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@TempDir
	static Path tempDir;

	final static String N1 = "123456789";
	final static String N2 = "987654321";

	@Test
	void testSave() {
		try {
			assertTrue(tempDir.toFile().isDirectory());
			final DataKeeper keeper = DataKeeper.getInstance();
			keeper.reset(); // start always fresh
			File target = new File(tempDir.toFile(), Const.LOG_NAME);
			assertFalse(target.exists());
			keeper.setLogName(target.toString());

			keeper.save();
			assertTrue(target.exists());
			assertEquals(0, target.length()); // no numbers, empty file

			keeper.receive(N1);
			keeper.save();
			assertTrue(target.exists());
			 // one number plus EOL
			final int ex1 = N1.length() + System.lineSeparator().length();
			assertEquals(ex1, target.length());

			keeper.receive(N2);
			keeper.receive(N2); // received 3, still 2 unique
			keeper.save();
			assertTrue(target.exists());
			// two numbers plus 2 EOL
			final int ex2 = N1.length() + N2.length() + 2 * System.lineSeparator().length();
			assertEquals(ex2, target.length());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	void testReport() {
		try {
			final DataKeeper keeper = DataKeeper.getInstance();
			keeper.reset(); // start always fresh
			assertEquals(
				"Received 0 unique numbers, 0 duplicates. Unique total: 0",
				keeper.report());
			keeper.receive(N1);
			keeper.receive(N1);
			assertEquals(
				"Received 1 unique numbers, 1 duplicates. Unique total: 1",
				keeper.report());
			keeper.receive(N1);
			assertEquals(
				"Received 0 unique numbers, 1 duplicates. Unique total: 1",
				keeper.report());
			keeper.receive(N1);
			keeper.receive(N2);
			keeper.receive(N2);
			assertEquals(
				"Received 1 unique numbers, 2 duplicates. Unique total: 2",
				keeper.report());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

}
