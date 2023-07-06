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

public class TestNew {

	private Process sut = null;
	static HamsterTestDataStore store = HamsterTestDataStore.getInstance();	
	static HamsterClient hmstr = null;
	
	static int port = store.getPort();
	
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
	public void new_hamster() throws Exception {
		String owner_name = "otto";
		String hamster_name = "heinz";
		int treats = 23;

		int returnCode = -1;

		connect();

		returnCode = hmstr.new_(owner_name, hamster_name, (short) treats);
		assertTrue("UUID must be greater or equal to 0.", returnCode >= 0);

		var searchResult = hmstr.search(owner_name, hamster_name);
		assertTrue("newly added hamster not found", searchResult.size() == 2);
	}
	
	@Test
	public void new_duplicate() throws Exception {
		String owner_name = "otto";
		String hamster_name = "heinz";
		int treats = 23;

		int returnCode = -1;

		connect();

		returnCode = hmstr.new_(owner_name, hamster_name, (short) treats);
		assertTrue("UUID must be greater or equal to 0.", returnCode >= 0);

		// insert duplicate
		try {
			returnCode = hmstr.new_(owner_name, hamster_name, (short) treats);
			fail("Expected exception");
		} catch (HamsterClientException e) {
			assertTrue("Error text should contain 'a hamster by that owner/name already exists'", e.getMessage().contains("a hamster by that owner/name already exists"));
		}
	}

	@Test
	public void new_max_owner_name() throws Exception {
		String owner_name = "diesnameee123456789012345678901";
		String hamster_name = "langerName";
		int treats = 0;

		int returnCode = -1;

		connect();

		returnCode = hmstr.new_(owner_name, hamster_name, (short) treats);
		assertTrue("UUID must be greater or equal to 0.", returnCode >= 0);
	}
	
	@Test
	public void new_max_hamster_name() throws Exception {
		String owner_name = "diesnameee123456789012345678901";
		String hamster_name = "diesnameee123456789012345678902";
		int treats = 0;

		int returnCode = -1;

		connect();

		returnCode = hmstr.new_(owner_name, hamster_name, (short) treats);
		assertTrue("UUID must be greater or equal to 0.", returnCode >= 0);
	}

	@Test
	public void new_owner_name_too_long() throws Exception {
		String owner_name = "diesnameee12345678901234567890143276478";
		String hamster_name = "langerName";
		int treats = 0;

		connect();

		try {
			hmstr.new_(owner_name, hamster_name, (short) treats);
			fail("Expected exception to be thrown");
		}
		catch (HamsterClientException ex) {
			assertTrue("error message should contain 'the specified name is too long'", ex.getMessage().contains("the specified name is too long"));
		}
	}

	@Test
	public void new_hamster_name_too_long() throws Exception {
		String owner_name = "langerName";
		String hamster_name = "diesnameee12345678901234567890143276478";
		int treats = 0;

		connect();

		try {
			hmstr.new_(owner_name, hamster_name, (short) treats);
			fail("Expected exception to be thrown");
		}
		catch (HamsterClientException ex) {
			assertTrue("error message should contain 'the specified name is too long'", ex.getMessage().contains("the specified name is too long"));
		}
	}
}
