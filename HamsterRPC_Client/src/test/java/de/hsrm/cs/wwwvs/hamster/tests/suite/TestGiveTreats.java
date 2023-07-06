package de.hsrm.cs.wwwvs.hamster.tests.suite;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import de.hsrm.cs.wwwvs.hamster.rpc.HamsterRPCException;
import de.hsrm.cs.wwwvs.hamster.rpc.HamsterRPCException_DatabaseCorrupt;
import de.hsrm.cs.wwwvs.hamster.rpc.HamsterRPCException_NotFound;
import de.hsrm.cs.wwwvs.hamster.rpc.HamsterRPCException_StorageError;
import de.hsrm.cs.wwwvs.hamster.rpc.client.HamsterRPCConnection;
import de.hsrm.cs.wwwvs.hamster.tests.HamsterTestDataStore;

public class TestGiveTreats {

	private static Process sut = null;
	
	private static int port = HamsterTestDataStore.getInstance().getPort();
	
	private HamsterRPCConnection hamster = null;
	
	@Rule
	public Timeout globalTimeout= new Timeout(HamsterTestDataStore.getInstance().testcaseTimeoutms, TimeUnit.MILLISECONDS);

	
	
	@Before
	public void setUp() throws Exception {
		HamsterTestDataStore.getInstance().wipeHamsterfile();
	}

	private void connect() throws IOException {
		sut = HamsterTestDataStore.getInstance().startHamsterServer(port);

		hamster = new HamsterRPCConnection("localhost", port, true);

		HamsterTestDataStore.sleepMin();
	}

	@After
	public void tearDown() throws Exception {

		if (hamster != null) {
			hamster.close();
		}
		
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
		int id = hamster.lookup("otto", "heinz");
		int left = hamster.givetreats(id, 5);

		assertSame(18, left);
	}

	// testcase 2: id  = 0
	@Test
	public void testZeroID() throws Exception {
		HamsterTestDataStore.getInstance().copyTestHamsterfile("td1.dat");

		int expectedUUID = 0;

		boolean ok = false;
		try {
			connect();
			int left = hamster.givetreats(expectedUUID, 5);


		} catch (HamsterRPCException_NotFound e) {
			// should be thrown
		}
		ok = HamsterTestDataStore.getInstance().compareHamsterFileEqual("td1.dat");
		assertTrue("After giveTreats of 0, the hamsterfile.dat is not as expeced", ok);
	}

	// testcase 4: treats = 0
	@Test
	public void testGiveZeroTreats() throws Exception {
		HamsterTestDataStore.getInstance().copyTestHamsterfile("td1.dat");

		connect();
		int id = hamster.lookup("otto", "heinz");
		int left = hamster.givetreats(id, 0);

		boolean ok = HamsterTestDataStore.getInstance().compareHamsterFileEqual("td1.dat");
		assertTrue("After giveTreats of 0, the hamsterfile.dat is not as expeced", ok);

		assertSame(23, left);
	}

	// testcase 5: treats > x
	@Test
	public void testGiveMoreTreats() throws Exception {

		HamsterTestDataStore.getInstance().copyTestHamsterfile("td1.dat");

		connect();
		int id = hamster.lookup("otto", "heinz");
		int left = hamster.givetreats(id, 50);

		boolean ok = HamsterTestDataStore.getInstance().compareHamsterFileEqual("td10.dat");

		assertTrue("After giveTreats of 50, the hamsterfile.dat is not as expeced", ok);

		assertSame(0, left);
	}

	// testcase 6: treats = UINT16_MAX
	@Test
	public void testGiveMaxTreats() throws Exception {
		HamsterTestDataStore.getInstance().copyTestHamsterfile("td1.dat");

		connect();
		int id = hamster.lookup("otto", "heinz");
		int left = hamster.givetreats(id, 32767);

		assertSame(0, left);
	}
}
