package de.hsrm.cs.wwwvs.hamster.tests.suite;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.UnknownHostException;
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
import de.hsrm.cs.wwwvs.hamster.rpc.Hmstr.HamsterInteger;
import de.hsrm.cs.wwwvs.hamster.rpc.Hmstr.HamsterString;
import de.hsrm.cs.wwwvs.hamster.rpc.client.HamsterRPCConnection;
import de.hsrm.cs.wwwvs.hamster.tests.HamsterTestDataStore;

public class TestReadEntry {


	private static Process sut = null;

	private static int port = HamsterTestDataStore.getInstance().getPort();
	private static String sutPath = HamsterTestDataStore.getInstance().getPathToHamsterServer();

	private HamsterRPCConnection hamster = null;

	@Rule
	public Timeout globalTimeout= new Timeout(HamsterTestDataStore.getInstance().testcaseTimeoutms, TimeUnit.MILLISECONDS);


	@Before
	public void setUp() throws Exception {
		HamsterTestDataStore.getInstance().wipeHamsterfile();
	}

	private void connect() {

		try {
			sut = HamsterTestDataStore.getInstance().startHamsterServer(port);
			assertTrue("Server process is not running.", sut.isAlive());
			hamster = new HamsterRPCConnection("localhost", port, true);
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

		if (hamster != null) {
			hamster.close();
		}

		HamsterTestDataStore.sleepMin();
		if (sut != null) {
			sut.destroy();
			sut.waitFor();
			assertFalse("Server process is not shuting down.", sut.isAlive());
		}
	}

	// testcase 1: test heinz
	@Test
	public void testHeinz() {
		try {
			HamsterTestDataStore.getInstance().createTestdata1();
			connect();
		} catch (IOException e) {
			fail("Unexpected Exception: " + e.getClass().getSimpleName() + " msg " + e.getMessage());
			return;
		}

		HamsterString owner = new HamsterString();
		HamsterString name = new HamsterString();
		HamsterInteger price = new HamsterInteger();

		try {
			var id = hamster.lookup("otto", "heinz");
			int left = hamster.readentry(id, owner, name, price);

			assertEquals(23, left);
			assertEquals("otto", owner.str);
			assertEquals("heinz", name.str);
			assertEquals(17, price.i);


		} catch (HamsterRPCException_NotFound e) {
			fail("Unexpected Exception: " + e.getClass().getSimpleName());
		} catch (HamsterRPCException_StorageError e) {
			fail("Unexpected Exception: " + e.getClass().getSimpleName());
		} catch (HamsterRPCException_DatabaseCorrupt e) {
			fail("Unexpected Exception: " + e.getClass().getSimpleName());
		} catch (IOException e) {
			fail("Unexpected Exception: " + e.getClass().getSimpleName());
		} catch (HamsterRPCException e) {
			fail("Unexpected Exception: " + e.getClass().getSimpleName());
		}


	}

	// testcase 2: test large name
	@Test
	public void testLargeName() {
		HamsterTestDataStore.getInstance().copyTestHamsterfile("td3.dat");
		connect();

		HamsterString owner = new HamsterString();
		HamsterString name = new HamsterString();
		HamsterInteger price = new HamsterInteger();

		try {

			int id = hamster.lookup("diesnameee123456789012345678901", "langerName");

			System.out.println("id " + id);


			int left = hamster.readentry(id, owner, name, price);


			assertEquals("diesnameee123456789012345678901", owner.str);
			assertEquals("langerName", name.str);
			assertEquals(0, left);
			//	assertEquals(17, price.i);


		} catch (HamsterRPCException_NotFound e) {
			fail("Unexpected Exception: " + e.getClass().getSimpleName());
		} catch (HamsterRPCException_StorageError e) {
			fail("Unexpected Exception: " + e.getClass().getSimpleName());
		} catch (HamsterRPCException_DatabaseCorrupt e) {
			fail("Unexpected Exception: " + e.getClass().getSimpleName());
		} catch (IOException e) {
			fail("Unexpected Exception: " + e.getClass().getSimpleName());
		} catch (HamsterRPCException e) {
			fail("Unexpected Exception: " + e.getClass().getSimpleName());
		}


	}

	// testcase 3: test nonext id
	@Test
	public void testNonExtID() {
		HamsterTestDataStore.getInstance().copyTestHamsterfile("td3.dat");
		connect();

		HamsterString owner = new HamsterString();
		HamsterString name = new HamsterString();
		HamsterInteger price = new HamsterInteger();

		try {
			int left = hamster.readentry(42, owner, name, price);

			fail("Expected HamsterRPCException_NotFound");


		} catch (HamsterRPCException_NotFound e) {
			// expected
		} catch (HamsterRPCException_StorageError e) {
			fail("Unexpected Exception: " + e.getClass().getSimpleName());
		} catch (HamsterRPCException_DatabaseCorrupt e) {
			fail("Unexpected Exception: " + e.getClass().getSimpleName());
		} catch (IOException e) {
			fail("Unexpected Exception: " + e.getClass().getSimpleName());
		} catch (HamsterRPCException e) {
			fail("Unexpected Exception: " + e.getClass().getSimpleName());
		}


	}

	// testcase 4: test double large name
	@Test
	public void testDoubleLargeName() {

		HamsterTestDataStore.getInstance().copyTestHamsterfile("td4.dat");
		connect();

		HamsterString owner = new HamsterString();
		HamsterString name = new HamsterString();
		HamsterInteger price = new HamsterInteger();

		try {

			int id = hamster.lookup("diesnameee123456789012345678901", "diesnameee123456789012345678902");

			int left = hamster.readentry(id, owner, name, price);


			assertEquals("diesnameee123456789012345678901", owner.str);
			assertEquals("diesnameee123456789012345678902", name.str);
			assertEquals(0, left);
			//	assertEquals(17, price.i);


		} catch (HamsterRPCException_NotFound e) {
			fail("Unexpected Exception: " + e.getClass().getSimpleName());
		} catch (HamsterRPCException_StorageError e) {
			fail("Unexpected Exception: " + e.getClass().getSimpleName());
		} catch (HamsterRPCException_DatabaseCorrupt e) {
			fail("Unexpected Exception: " + e.getClass().getSimpleName());
		} catch (IOException e) {
			fail("Unexpected Exception: " + e.getClass().getSimpleName());
		} catch (HamsterRPCException e) {
			fail("Unexpected Exception: " + e.getClass().getSimpleName());
		}


	}

	// testcase 5: test large price
	// 32759 treats
	@Test
	public void testMaxPrice() {
		try {
			HamsterTestDataStore.getInstance().createTestdata11();
			connect();
		} catch (IOException e) {
			fail("Unexpected Exception: " + e.getClass().getSimpleName() + " msg " + e.getMessage());
			return;
		}

		HamsterString owner = new HamsterString();
		HamsterString name = new HamsterString();
		HamsterInteger price = new HamsterInteger();

		try {
			int id = hamster.lookup("otto", "heinz");
			int left = hamster.readentry(id, owner, name, price);


			assertEquals(32765, price.i);
			assertEquals(0, left);
			assertEquals("otto", owner.str);
			assertEquals("heinz", name.str);


		} catch (HamsterRPCException_NotFound e) {
			fail("Unexpected Exception: " + e.getClass().getSimpleName());
		} catch (HamsterRPCException_StorageError e) {
			fail("Unexpected Exception: " + e.getClass().getSimpleName());
		} catch (HamsterRPCException_DatabaseCorrupt e) {
			fail("Unexpected Exception: " + e.getClass().getSimpleName());
		} catch (IOException e) {
			fail("Unexpected Exception: " + e.getClass().getSimpleName());
		} catch (HamsterRPCException e) {
			fail("Unexpected Exception: " + e.getClass().getSimpleName());
		}


	}

	// testcase 6: test no payload after successful call
	@Test
	public void testNoPayloadAfterSuccessCall() {
		try {
			HamsterTestDataStore.getInstance().createTestdata1();
			connect();
		} catch (IOException e) {
			fail("Unexpected Exception: " + e.getClass().getSimpleName() + " msg " + e.getMessage());
			return;
		}

		HamsterString owner = new HamsterString();
		HamsterString name = new HamsterString();
		HamsterInteger price = new HamsterInteger();

		hamster.setTestNoPayloadAfterMessage(true);

		try {
			int id = hamster.lookup("otto", "heinz");
			int left = hamster.readentry(id, owner, name, price);

			assertEquals("otto", owner.str);
			assertEquals("heinz", name.str);

		} catch (HamsterRPCException_NotFound e) {
			fail("Unexpected Exception: " + e.getClass().getSimpleName());
		} catch (HamsterRPCException_StorageError e) {
			fail("Unexpected Exception: " + e.getClass().getSimpleName());
		} catch (HamsterRPCException_DatabaseCorrupt e) {
			fail("Unexpected Exception: " + e.getClass().getSimpleName());
		} catch (IOException e) {
			fail("Unexpected Exception: " + e.getClass().getSimpleName());
		} catch (HamsterRPCException e) {
			fail("Unexpected Exception: " + e.getClass().getSimpleName());
		}

		try {
			int addByte = hamster.receiveOneByte();
			assertTrue("Received payload after sucessfull rpc call", (addByte == -1));
		} catch (IOException e) {

			fail("Unexpected Exception: " + e.getClass().getSimpleName() + " msg " + e.getMessage());
		}

	}


	// testcase 7: test no payload after error message
	@Test
	public void testNoPayloadAfterError() {
		try {
			HamsterTestDataStore.getInstance().createTestdata1();
			connect();
		} catch (IOException e) {
			fail("Unexpected Exception: " + e.getClass().getSimpleName() + " msg " + e.getMessage());
			return;
		}

		HamsterString owner = new HamsterString();
		HamsterString name = new HamsterString();
		HamsterInteger price = new HamsterInteger();

		hamster.setTestNoPayloadAfterMessage(true);

		try {
			hamster.readentry(2342, owner, name, price);
		} catch (HamsterRPCException_NotFound e) {
			//
		} catch (HamsterRPCException_StorageError e) {
			fail("Unexpected Exception: " + e.getClass().getSimpleName());
		} catch (HamsterRPCException_DatabaseCorrupt e) {
			fail("Unexpected Exception: " + e.getClass().getSimpleName());
		} catch (IOException e) {
			fail("Unexpected Exception: " + e.getClass().getSimpleName());
		} catch (HamsterRPCException e) {
			//
		}

		try {
			int addByte = hamster.receiveOneByte();
			assertTrue("Received payload after sucessfull rpc call", (addByte == -1));
		} catch (IOException e) {

			fail("Unexpected Exception: " + e.getClass().getSimpleName() + " msg " + e.getMessage());
		}

	}

	// testcase 8: test two calls in a row
	@Test
	public void testTwoCalls() {
		try {
			HamsterTestDataStore.getInstance().createTestdata1();
			connect();
		} catch (IOException e) {
			fail("Unexpected Exception: " + e.getClass().getSimpleName() + " msg " + e.getMessage());
			return;
		}

		int id = 0;

		HamsterString owner = new HamsterString();
		HamsterString name = new HamsterString();
		HamsterInteger price = new HamsterInteger();

		try {
			id = hamster.lookup("otto", "heinz");
			int left = hamster.readentry(id, owner, name, price);

			assertEquals(23, left);
			assertEquals("otto", owner.str);
			assertEquals("heinz", name.str);
			assertEquals(17, price.i);


		} catch (HamsterRPCException_NotFound e) {
			fail("Unexpected Exception: " + e.getClass().getSimpleName());
		} catch (HamsterRPCException_StorageError e) {
			fail("Unexpected Exception: " + e.getClass().getSimpleName());
		} catch (HamsterRPCException_DatabaseCorrupt e) {
			fail("Unexpected Exception: " + e.getClass().getSimpleName());
		} catch (IOException e) {
			fail("Unexpected Exception: " + e.getClass().getSimpleName());
		} catch (HamsterRPCException e) {
			fail("Unexpected Exception: " + e.getClass().getSimpleName());
		}

		try {
			int left = hamster.readentry(id, owner, name, price);

			assertEquals(23, left);
			assertEquals("otto", owner.str);
			assertEquals("heinz", name.str);
			assertEquals(17, price.i);


		} catch (HamsterRPCException_NotFound e) {
			fail("Unexpected Exception: " + e.getClass().getSimpleName());
		} catch (HamsterRPCException_StorageError e) {
			fail("Unexpected Exception: " + e.getClass().getSimpleName());
		} catch (HamsterRPCException_DatabaseCorrupt e) {
			fail("Unexpected Exception: " + e.getClass().getSimpleName());
		} catch (IOException e) {
			fail("Unexpected Exception: " + e.getClass().getSimpleName());
		} catch (HamsterRPCException e) {
			fail("Unexpected Exception: " + e.getClass().getSimpleName());
		}


	}

}
