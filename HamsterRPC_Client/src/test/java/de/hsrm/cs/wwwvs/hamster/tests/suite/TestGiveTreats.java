package de.hsrm.cs.wwwvs.hamster.tests.suite;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Random;
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

public class TestGiveTreats {

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
		}

		assertFalse("Server process is not shuting down.", sut.isAlive());
	}
	
	// testcase 1: before x after x-2
	@Test
	public void testGive5Treats() throws Exception {
		HamsterTestDataStore.getInstance().copyTestHamsterfile("td1.dat");

		connect();
		int left = hamster.giveTreats("otto", "heinz", 5);

		assertSame(18, left);
	}

	@Test
	public void testHamsterDoesNotExist() throws Exception {
		HamsterTestDataStore.getInstance().copyTestHamsterfile("td1.dat");

		connect();

		try {
			hamster.giveTreats("doesNot", "exist", 42);
			fail("Giving treats to non-existing hamster should have resulted in an error");
		}
		catch (HamsterClientException e) {
			assertTrue("Error text should contain 'A hamster or hamster owner could not be found.' but was " + e.getMessage(), e.getMessage().contains("hamster or hamster owner could not be found."));
		}
	}

	@Test
	public void testGiveNegativeTreats() throws Exception {
		HamsterTestDataStore.getInstance().copyTestHamsterfile("td1.dat");

		connect();

		try {
			hamster.giveTreats("otto", "heinz", -42);
			fail("Giving negative number of treats should have resulted in an error");
		}
		catch (HamsterClientException e) {
			// not exactly important what exactly failed
		}
	}

	// testcase 4: treats = 0
	@Test
	public void testGiveZeroTreats() throws Exception {
		HamsterTestDataStore.getInstance().copyTestHamsterfile("td1.dat");

		connect();
		int left = hamster.giveTreats("otto", "heinz", 0);

		boolean ok = HamsterTestDataStore.getInstance().compareHamsterFileEqual("td1.dat");
		assertTrue("After giveTreats of 0, the hamsterfile.dat is not as expeced", ok);

		assertSame(23, left);
	}

	// testcase 5: treats > x
	@Test
	public void testGiveMoreTreats() throws Exception {

		HamsterTestDataStore.getInstance().copyTestHamsterfile("td1.dat");

		connect();
		int left = hamster.giveTreats("otto", "heinz", 50);

		boolean ok = HamsterTestDataStore.getInstance().compareHamsterFileEqual("td10.dat");

		assertTrue("After giveTreats of 50, the hamsterfile.dat is not as expeced", ok);

		assertSame(0, left);
	}

	// testcase 6: treats = UINT16_MAX
	@Test
	public void testGiveMaxTreats() throws Exception {
		HamsterTestDataStore.getInstance().copyTestHamsterfile("td1.dat");

		connect();
		int left = hamster.giveTreats("otto", "heinz", 32767);

		assertSame(0, left);
	}
}
