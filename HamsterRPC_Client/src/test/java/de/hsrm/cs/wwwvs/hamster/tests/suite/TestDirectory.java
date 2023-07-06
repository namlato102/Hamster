package de.hsrm.cs.wwwvs.hamster.tests.suite;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import de.hsrm.cs.wwwvs.hamster.tests.client.HamsterClient;
import de.hsrm.cs.wwwvs.hamster.tests.client.HamsterClientException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import de.hsrm.cs.wwwvs.hamster.tests.HamsterTestDataStore;

public class TestDirectory {

private static Process sut = null;

	private static int port = HamsterTestDataStore.getInstance().getPort();
	
	private HamsterClient hamster = null;
	
	@Rule
	public Timeout globalTimeout= new Timeout(HamsterTestDataStore.getInstance().testcaseTimeoutms, TimeUnit.MILLISECONDS);
	
	
	@Before
	public void setUp() throws Exception {
		
		HamsterTestDataStore.getInstance().wipeHamsterfile();
	}

	private void connect() throws IOException {
		sut = HamsterTestDataStore.getInstance().startHamsterServer(port);

		hamster = new HamsterClient(port);
		HamsterTestDataStore.sleepMin();
	}

	@After
	public void tearDown() throws Exception {
		HamsterTestDataStore.sleepMin();
		if (sut != null) {
			sut.destroy();
			sut.waitFor();
			assertFalse("Server process is not shuting down.", sut.isAlive());
		}
	}

	private void assertHamsterAt(String responseLine, String ownerName, String hamsterName, int price, int treatsLeft) {
		var expectedStart = ownerName + "\t" + hamsterName + "\t";
		var expectedEnd = "\t" + treatsLeft;
		assertTrue("hamster result is incorrect, expected '" + expectedStart + price + " €" + expectedEnd + ", but received " + responseLine, responseLine.startsWith(expectedStart) && responseLine.contains(expectedEnd));
	}
	
	// testcase 1: einmal alle (1x heinz)
	@Test
	public void testAllHamster1() throws Exception {

		HamsterTestDataStore.getInstance().copyTestHamsterfile("td1.dat");
		connect();

		var response = hamster.search(null, null);
		assertTrue("wrong number of hamsters", response.size() == 2);
		assertHamsterAt(response.get(1), "otto", "heinz", 0, 23);
	}

	// testcase 2: einmal alle (zwei Hamster)
	@Test
	public void testAllHamster2() throws Exception {

		HamsterTestDataStore.getInstance().copyTestHamsterfile("td7.dat");
		connect();

		var response = hamster.search(null, null);
		assertTrue("wrong number of hamsters", response.size() == 3);
		assertHamsterAt(response.get(1), "otto", "heinz", 17, 23);
		assertHamsterAt(response.get(2), "karl", "blondy", 17, 42);
	}

	// testcase 3: einmal alle (50 Hamster)
	@Test
	public void testAllHamster50() throws Exception {

		HamsterTestDataStore.getInstance().copyTestHamsterfile("td6.dat");
		connect();

		var response = hamster.search(null, null);
		assertTrue("wrong number of hamsters", response.size() == 51);

		for (int i = 1; i < 50; i++) {
			var ownerName = "otto" + i;
			var hamsterName = "heinz" + i;
			assertHamsterAt(response.get(i), ownerName, hamsterName, 17, i);
		}
	}

	// testcase 4: einmal alle von otto (1x)
	@Test
	public void testAllHamsterOtto() throws Exception {
		HamsterTestDataStore.getInstance().copyTestHamsterfile("td7.dat");
		connect();

		var response = hamster.search("otto", null);
		assertTrue("wrong number of hamsters", response.size() == 2);
		assertHamsterAt(response.get(1), "otto", "heinz", 17, 23);
	}

	// testcase 5: einmal alle von otto (2x)
		@Test
		public void testAllHamsterOtto2() throws Exception {

			HamsterTestDataStore.getInstance().createTestdata8();
			connect();

			var response = hamster.search("otto", null);
			assertTrue("wrong number of hamsters", response.size() == 3);
			assertHamsterAt(response.get(1), "otto", "heinz", 17, 23);
			assertHamsterAt(response.get(2), "otto", "blondy", 17, 42);
		}

		// testcase 6: einmal alle von goldies (1x)
		@Test
		public void testAllHamsterBlondy() throws Exception {
			HamsterTestDataStore.getInstance().createTestdata8();
			connect();

			var response = hamster.search(null, "blondy");
			assertTrue("wrong number of hamsters", response.size() == 2);
			assertHamsterAt(response.get(1), "otto", "blondy", 17, 42);
		}

		// testcase 7: einmal alle von goldies (2x)
		@Test
		public void testAllHamsterBlondy2() throws Exception {

			assertTrue(HamsterTestDataStore.getInstance().copyTestHamsterfile("td12.dat"));
			connect();

			var response = hamster.search(null, "blondy");
			assertTrue("wrong number of hamsters", response.size() == 3);
			assertHamsterAt(response.get(1), "otto", "blondy", 17, 23);
			assertHamsterAt(response.get(2), "hans", "blondy", 17, 23);
		}
	
}
