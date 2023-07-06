package de.hsrm.cs.wwwvs.hamster.tests.suite;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
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

public class TestCollect {
	
	
	private static Process sut = null;
	
	private static int port = HamsterTestDataStore.getInstance().getPort();
	
	private HamsterClient hamster = null;
	
	@Rule
	public Timeout globalTimeout= new Timeout(HamsterTestDataStore.getInstance().testcaseTimeoutms, TimeUnit.MILLISECONDS);

	@Before
	public void setup() throws IOException {
		HamsterTestDataStore.getInstance().wipeHamsterfile();
	}

	public void connect() throws IOException {
		sut = HamsterTestDataStore.getInstance().startHamsterServer(port);
		
		hamster = new HamsterClient(port);
		
		HamsterTestDataStore.sleepMin();
	}

	@After
	public void tearDown() throws Exception {
		if (sut != null) {
			sut.destroy();
			sut.waitFor();
			assertFalse("Server process is not shuting down.", sut.isAlive());
		}
	}
	
	
	// testcase 1: collect one hamster
	@Test
	public void testCollectOneHamster() throws  Exception {
		try {
			HamsterTestDataStore.getInstance().createTestdata1();
			connect();
		} catch (IOException e1) {
			fail("Unexpected Exception: " + e1.getClass().getSimpleName() + " msg " + e1.getMessage());
			return;
		}

		int price = hamster.collect("otto");

		assertSame(17, price);
	}
	// testcase 2: collect two hamster
	@Test
	public void testCollectTwoHamster() throws  Exception {
		
		try {
			HamsterTestDataStore.getInstance().createTestdata8();
			connect();
		} catch (IOException e) {
			fail("Unexpected Exception: " + e.getClass().getSimpleName() + " msg " + e.getMessage());
			return;
		}

		int price = hamster.collect("otto");
		assertSame(34, price);
	}
	// testcase 3: collect not existing owner
	@Test
	public void testCollectNoExtOwner() throws Exception {
		HamsterTestDataStore.getInstance().copyTestHamsterfile("td1.dat");

		int price;
		try {
			connect();
			hamster.collect("karl");
			fail("Expected error" );
		} catch (HamsterClientException e) {
			assertTrue("error text should indicate problem but was: " + e.getMessage(), e.getMessage().contains("A hamster or hamster owner could not be found."));
		}
	}
	// testcase 4: collect no database
	@Test
	public void testCollectNoDatabase() throws Exception {

		int price;
		try {
			connect();
			hamster.collect("karl");
			fail("Expected error" );
		} catch (HamsterClientException e) {
			assertTrue("error message should contain 'A hamster or hamster owner could not be found.' but was " + e.getMessage(), e.getMessage().contains("A hamster or hamster owner could not be found."));
		}
	}
	
	// testcase 8: two calls in a row
	
	@Test
	public void testTwoCallsInARow() throws Exception {

		try {
			HamsterTestDataStore.getInstance().createTestdata13();
			connect();
		} catch (IOException e1) {
			fail("Unexpected Exception: " + e1.getClass().getSimpleName() + " msg " + e1.getMessage());
			return;
		}
		int price = hamster.collect("otto");
		assertSame(17, price);

		price = hamster.collect("bernd");
		assertSame(17, price);
	}

	
}
