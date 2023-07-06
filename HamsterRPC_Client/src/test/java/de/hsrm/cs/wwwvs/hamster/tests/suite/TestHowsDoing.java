package de.hsrm.cs.wwwvs.hamster.tests.suite;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.UnknownHostException;
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

public class TestHowsDoing {

	private static Process sut = null;
	static HamsterTestDataStore store = HamsterTestDataStore.getInstance();
	static HamsterClient hmstr = null;

	static int port = store.getPort();

	@Rule
	public Timeout globalTimeout= new Timeout(HamsterTestDataStore.getInstance().testcaseTimeoutms, TimeUnit.MILLISECONDS);

	@Before
	public void setUp() {
		HamsterTestDataStore.getInstance().wipeHamsterfile();
	}

	private static void connect() {

		try {
			sut = HamsterTestDataStore.getInstance().startHamsterServer(port);
			HamsterTestDataStore.sleepMin();
			assertTrue("Server process is not running.", sut.isAlive());
			hmstr = new HamsterClient(port);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			fail("Failed to connect to server: " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			fail("Failed to connect to server: " + e.getMessage());
		}

		HamsterTestDataStore.sleepMin();
	}

	@After
	public void tearDown() throws Exception {
		sut.destroy();
		sut.waitFor();
		assertFalse("Server process is not shuting down.", sut.isAlive());
	}


	@Test
	public void howsdoing_td1() throws Exception {
		HamsterTestDataStore.getInstance().createTestdata1();
		connect();

		var returnCode = hmstr.howsDoing("otto", "heinz");
		assertReturn(returnCode, "otto", "heinz", 23, 18);
	}

	private void assertReturn(String line, String ownerName, String hamsterName, int treatsLeft, int cost) {
		assertTrue("owner name is wrong, expected " + ownerName + " but received " + line, line.startsWith(ownerName));
		assertTrue("hamster name is wrong, expected " + hamsterName + " but received " + line, line.contains("hamster " + hamsterName + " has done"));
		assertTrue("remaining treats wrong, expected " + treatsLeft + " remaining treats but received " + line, line.contains("has " + treatsLeft + " treats left in store"));
		assertTrue("price is wrong, expected " + cost + "€ but received " + line, line.contains("price is " + cost + " "));
	}

	@Test
	public void howsdoing_not_found() throws Exception {
		HamsterTestDataStore.getInstance().createTestdata1();
		connect();

		try {
			hmstr.howsDoing("doesNot", "exist");
			fail("Expected exception");
		} catch (HamsterClientException e) {
			assertTrue("error message should contain 'A hamster or hamster owner could not be found.'", e.getMessage().contains("A hamster or hamster owner could not be found."));
		}
	}

	@Test
	public void howsdoing_td2() throws Exception {
		HamsterTestDataStore.getInstance().createTestdata2();
		connect();

		var returnCode = hmstr.howsDoing("otto", "heinz");
		assertReturn(returnCode, "otto", "heinz", 0, 23);
	}

	@Test
	public void howsdoing_td5() throws Exception {
		HamsterTestDataStore.getInstance().createTestdata5();
		connect();

		var returnCode = hmstr.howsDoing("otto", "heinz");
		assertReturn(returnCode, "otto", "heinz", 32767, 50);
	}

	// testcase 7: two calls in a row

	@Test
	public void testTwoCallsInARow() throws Exception {

		HamsterTestDataStore.getInstance().createTestdata13();
		connect();

		var returnCode1 = hmstr.howsDoing("otto", "heinz");
		assertReturn(returnCode1, "otto", "heinz", 23, 18);
		var returnCode2 = hmstr.howsDoing("bernd", "blondy");
		assertReturn(returnCode2, "bernd", "blondy", 42, 18);
	}
}
