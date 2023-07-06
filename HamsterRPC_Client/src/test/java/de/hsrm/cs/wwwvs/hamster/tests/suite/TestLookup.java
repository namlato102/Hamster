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

public class TestLookup {

	private Process sut = null;
	static HamsterTestDataStore store = HamsterTestDataStore.getInstance();	
	static HamsterClient hmstr = null;
	
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
		if (sut != null) {
			sut.destroy();
			sut.waitFor();
		}
		assertFalse("Server process is not shuting down.", sut.isAlive());
	}

	@Test
	public void lookup_td1() throws Exception {
		assertTrue("Failed to setup test.", HamsterTestDataStore.getInstance().copyTestHamsterfile("td1.dat"));
		connect();
		
		String owner_name = "otto";
		String hamster_name = "heinz";

		hmstr.search(owner_name, hamster_name);
	}
	
	@Test
	public void lookup_not_found() throws Exception {
		assertTrue("Failed to setup test.", HamsterTestDataStore.getInstance().copyTestHamsterfile("td1.dat"));
		connect();
		
		String owner_name = "not";
		String hamster_name = "found";
		
		try {
			hmstr.search(owner_name, hamster_name);
			fail("Expected NotFound error");
		} catch (HamsterClientException e) {
			assertTrue("error message should contain 'No hamsters matching criteria found' but was " + e.getMessage(), e.getMessage().contains("No hamsters matching criteria found"));
		}
	}
	
	@Test
	public void lookup_empty_owner() throws Exception {
		assertTrue("Failed to setup test.", HamsterTestDataStore.getInstance().copyTestHamsterfile("td1.dat"));
		connect();
		
		String owner_name = "";
		String hamster_name = "heinz";

	    assertTrue(hmstr.search(owner_name, hamster_name).size() == 2);
	}
	
	@Test
	public void lookup_empty_hamster() throws Exception {
		assertTrue("Failed to setup test.", HamsterTestDataStore.getInstance().copyTestHamsterfile("td1.dat"));
		connect();
		
		String owner_name = "otto";
		String hamster_name = "";

		assertTrue(hmstr.search(owner_name, hamster_name).size() == 2);
	}
	
	@Test
	public void lookup_empty_string() throws Exception {
		assertTrue("Failed to setup test.", HamsterTestDataStore.getInstance().copyTestHamsterfile("td1.dat"));
		connect();
		
		String owner_name = "";
		String hamster_name = "";
		
		assertTrue(hmstr.search(owner_name, hamster_name).size() == 2);
	}
	
	@Test
	public void lookup_td3() throws Exception {
		assertTrue("Failed to setup test.", HamsterTestDataStore.getInstance().copyTestHamsterfile("td3.dat"));
		connect();
		
		String owner_name = "diesnameee123456789012345678901";
		String hamster_name = "langerName";
		
		assertTrue(hmstr.search(owner_name, hamster_name).size() == 2);
	}
	
	@Test
	public void lookup_td4() throws Exception {
		assertTrue("Failed to setup test.", HamsterTestDataStore.getInstance().copyTestHamsterfile("td4.dat"));
		connect();
		
		String owner_name = "diesnameee123456789012345678901";
		String hamster_name = "diesnameee123456789012345678902";

		assertTrue(hmstr.search(owner_name, hamster_name).size() == 2);
	}
}
