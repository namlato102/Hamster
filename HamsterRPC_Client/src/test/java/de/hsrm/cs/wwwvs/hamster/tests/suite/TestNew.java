package de.hsrm.cs.wwwvs.hamster.tests.suite;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import de.hsrm.cs.wwwvs.hamster.rpc.*;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import de.hsrm.cs.wwwvs.hamster.rpc.client.HamsterRPCConnection;
import de.hsrm.cs.wwwvs.hamster.tests.HamsterTestDataStore;

public class TestNew {

	private Process sut = null;
	static HamsterTestDataStore store = HamsterTestDataStore.getInstance();	
	static HamsterRPCConnection hmstr = null;
	
	static int port = store.getPort();
	static String hostname = "localhost";
	
	@Rule
	public Timeout globalTimeout= new Timeout(HamsterTestDataStore.getInstance().testcaseTimeoutms, TimeUnit.MILLISECONDS);

	@Before
	public void setUp() {
		HamsterTestDataStore.getInstance().wipeHamsterfile();
	}

	private void connect() {

		try {
			sut = HamsterTestDataStore.getInstance().startHamsterServer(port);
			assertTrue("Server process is not running.", sut.isAlive());
			hmstr = new HamsterRPCConnection(hostname, port, true);
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
		try {
			if (hmstr != null) {
				hmstr.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
			fail("Connection failed");
		} 

		sut.destroy();
		sut.waitFor();
		assertFalse("Server process is not shuting down.", sut.isAlive());
	}


	@Test
	public void new_hamster() throws Exception {
		String owner_name = "otto";
		String hamster_name = "heinz";
		int treats = 23;

		int returnCode = -1;

		connect();

		returnCode = hmstr.new_(owner_name, hamster_name, treats);
		assertTrue("UUID must be greater or equal to 0.", returnCode >= 0);

		var hmstrOwner = new Hmstr.HamsterString();
		var hmstrName = new Hmstr.HamsterString();
		var hmstrPrice = new Hmstr.HamsterInteger();
		hmstr.readentry(returnCode, hmstrOwner, hmstrName, hmstrPrice);
		assertEquals(owner_name, hmstrOwner.str);
		assertEquals(hamster_name, hmstrName.str);
		assertEquals(17, hmstrPrice.i);
	}
	
	@Test
	public void new_duplicate() throws Exception {
		String owner_name = "otto";
		String hamster_name = "heinz";
		int treats = 23;

		int returnCode = -1;

		connect();

		returnCode = hmstr.new_(owner_name, hamster_name, treats);
		assertTrue("UUID must be greater or equal to 0.", returnCode >= 0);

		// insert duplicate
		try {
			returnCode = hmstr.new_(owner_name, hamster_name, treats);
			fail("Expected exception");
		} catch (HamsterRPCException_Extists e) {
		}

		var hmstrOwner = new Hmstr.HamsterString();
		var hmstrName = new Hmstr.HamsterString();
		var hmstrPrice = new Hmstr.HamsterInteger();
		hmstr.readentry(returnCode, hmstrOwner, hmstrName, hmstrPrice);
		assertEquals(owner_name, hmstrOwner.str);
		assertEquals(hamster_name, hmstrName.str);
		assertEquals(17, hmstrPrice.i);
	}

	@Test
	public void new_max_owner_name() throws Exception {
		String owner_name = "diesnameee123456789012345678901";
		String hamster_name = "langerName";
		int treats = 0;

		int returnCode = -1;

		connect();

		returnCode = hmstr.new_(owner_name, hamster_name, treats);
		assertTrue("UUID must be greater or equal to 0.", returnCode >= 0);

		var hmstrOwner = new Hmstr.HamsterString();
		var hmstrName = new Hmstr.HamsterString();
		var hmstrPrice = new Hmstr.HamsterInteger();
		hmstr.readentry(returnCode, hmstrOwner, hmstrName, hmstrPrice);
		assertEquals(owner_name, hmstrOwner.str);
		assertEquals(hamster_name, hmstrName.str);
		assertEquals(17, hmstrPrice.i);
	}
	
	@Test
	public void new_max_hamster_name() throws Exception {
		String owner_name = "diesnameee123456789012345678901";
		String hamster_name = "diesnameee123456789012345678902";
		int treats = 0;

		int returnCode = -1;

		connect();

		returnCode = hmstr.new_(owner_name, hamster_name, treats);
		assertTrue("UUID must be greater or equal to 0.", returnCode >= 0);
		var hmstrOwner = new Hmstr.HamsterString();
		var hmstrName = new Hmstr.HamsterString();
		var hmstrPrice = new Hmstr.HamsterInteger();
		hmstr.readentry(returnCode, hmstrOwner, hmstrName, hmstrPrice);
		assertEquals(owner_name, hmstrOwner.str);
		assertEquals(hamster_name, hmstrName.str);
		assertEquals(17, hmstrPrice.i);
	}
}
