package de.hsrm.cs.wwwvs.hamster.tests.suite;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import de.hsrm.cs.wwwvs.hamster.tests.client.HamsterClient;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import de.hsrm.cs.wwwvs.hamster.tests.HamsterTestDataStore;

public class TestCornerCases {

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
	public void cornerCase_new_hamster() throws Exception {
		String owner_name = "otto";
		String hamster_name = "heinz";
		short treats = 23;

		int returnCode = -1;

		connect();
		returnCode = hmstr.new_(owner_name, hamster_name, treats);
		assertTrue("UUID must be greater or equal to 0.", returnCode >= 0);
	}

	@Test
	public void cornerCase_howsdoing_td1() throws Exception {
		try {
			HamsterTestDataStore.getInstance().createTestdata1();
			connect();
		} catch (IOException e1) {
			fail("Unexpected Exception: " + e1.getClass().getSimpleName() + " msg " + e1.getMessage());
			return;
		}

		var returnCode = hmstr.howsDoing("otto", "heinz");
		assertTrue("treatsLeft expected 23",
				returnCode.contains("has 23 treats left"));
		assertTrue("cost expected 18",
				returnCode.contains("Current price is 18 "));
	}

	@Test
	public void cornerCase_testGive5Treats() throws Exception {
		HamsterTestDataStore.getInstance().copyTestHamsterfile("td1.dat");
		connect();

		int left = hmstr.giveTreats("otto", "heinz", (short) 5);

		assertSame(18, left);
	}

	@Test
	public void cornerCase_testHeinz() throws Exception {
		try {
			HamsterTestDataStore.getInstance().createTestdata1();
			connect();
		} catch (IOException e) {
			fail("Unexpected Exception: " + e.getClass().getSimpleName() + " msg " + e.getMessage());
			return;
		}

		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
		}

		var lookup = hmstr.search("otto", "heinz");
		assertTrue("client returned incorrect number of entries, expected 1 entry in 1 line", lookup.size() == 2);
		var heinz = lookup.get(1);
		assertTrue("wrong owner name", heinz.startsWith("otto"));
		assertTrue("wrong hamster name", heinz.contains("heinz"));
		assertTrue("price missing", heinz.contains("17 "));
		assertTrue("treats remaining missing", heinz.endsWith("23"));
	}

	@Test
	public void cornerCase_testCollectOneHamster() throws Exception {
		try {
			HamsterTestDataStore.getInstance().createTestdata1();
			connect();
		} catch (IOException e1) {
			fail("Unexpected Exception: " + e1.getClass().getSimpleName() + " msg " + e1.getMessage());
			return;
		}

		int price = hmstr.collect("otto");
		assertSame(17, price);
	}

}
