package de.hsrm.cs.wwwvs.hamster.tests.suite;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
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
import de.hsrm.cs.wwwvs.hamster.rpc.Hmstr.HamsterHandle;
import de.hsrm.cs.wwwvs.hamster.rpc.client.HamsterRPCConnection;
import de.hsrm.cs.wwwvs.hamster.tests.HamsterTestDataStore;

public class TestDirectory {

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
			assertFalse("Server process is not shuting down.", sut.isAlive());
		}
	}
	
	// testcase 1: einmal alle (1x heinz)
	@Test
	public void testAllHamster1() throws Exception {

		HamsterTestDataStore.getInstance().copyTestHamsterfile("td1.dat");

		HamsterHandle fdptr = new HamsterHandle();

		connect();
		int expectedUUID = hamster.lookup("otto", "heinz");

		fdptr.fdptr = -1;

		int uuid = hamster.directory(fdptr, null, null);

		assertEquals(expectedUUID, uuid);
		assertNotEquals(-1, fdptr.fdptr);

		try {
			hamster.directory(fdptr, null, null);
			fail("Expected HamsterRPCException_NotFound");
		} catch (HamsterRPCException_NotFound e) {
		}
	}

	// testcase 2: einmal alle (zwei Hamster)
	@Test
	public void testAllHamster2() throws Exception {

		HamsterTestDataStore.getInstance().copyTestHamsterfile("td7.dat");


		HamsterHandle fdptr = new HamsterHandle();
		connect();
		int expectedUUID = hamster.lookup("otto", "heinz");

		fdptr.fdptr = -1;

		int uuid = hamster.directory(fdptr, null, null);

		assertEquals(expectedUUID, uuid);
		assertNotEquals(-1, fdptr.fdptr);

		uuid = hamster.directory(fdptr, null, null);

		assertEquals(hamster.lookup("karl", "blondy"), uuid);
		assertNotEquals(-1, fdptr.fdptr);

		try {
			uuid = hamster.directory(fdptr, null, null);
			fail("Expected HamsterRPCException_NotFound");
		} catch (HamsterRPCException_NotFound e) {
		}
	}

	// testcase 3: einmal alle (50 Hamster)
	@Test
	public void testAllHamster50() throws Exception {

		HamsterTestDataStore.getInstance().copyTestHamsterfile("td6.dat");

		HamsterHandle fdptr = new HamsterHandle();
		connect();

		fdptr.fdptr = -1;

		for (int i = 1; i <= 50; i++) {
			var ownerName = "otto" + i;
			var hamsterName = "heinz" + i;
			var id = hamster.lookup(ownerName, hamsterName);
			int uuid = hamster.directory(fdptr, null, null);

			assertEquals(id, uuid);
			assertNotEquals(-1, fdptr.fdptr);
		}

		try {
			hamster.directory(fdptr, null, null);
			fail("Expected HamsterRPCException_NotFound");
		} catch (HamsterRPCException_NotFound e) {
		}
	}

	// testcase 4: einmal alle von otto (1x)
	@Test
	public void testAllHamsterOtto() throws Exception {
		HamsterTestDataStore.getInstance().copyTestHamsterfile("td7.dat");
		HamsterHandle fdptr = new HamsterHandle();
		connect();
		int expectedUUID = hamster.lookup("otto", "heinz");

		fdptr.fdptr = -1;

		int uuid = hamster.directory(fdptr, "otto", null);

		assertEquals(expectedUUID, uuid);
		assertNotEquals(-1, fdptr.fdptr);

		try {
			hamster.directory(fdptr, "otto", null);
			fail("Expected HamsterRPCException_NotFound");
		} catch (HamsterRPCException_NotFound e) {
		}
	}

	// testcase 5: einmal alle von otto (2x)
		@Test
		public void testAllHamsterOtto2() throws Exception {

			try {
				HamsterTestDataStore.getInstance().createTestdata8();
				connect();
			} catch (IOException e1) {
				e1.printStackTrace();
			}


			HamsterHandle fdptr = new HamsterHandle();
			int expectedUUID = hamster.lookup("otto", "heinz");

			fdptr.fdptr = -1;

			int uuid = hamster.directory(fdptr, "otto", null);

			assertEquals(expectedUUID, uuid);
			assertNotEquals(-1, fdptr.fdptr);

			uuid = hamster.directory(fdptr, "otto", null);

			assertEquals(hamster.lookup("otto", "blondy"), uuid);
			assertNotEquals(-1, fdptr.fdptr);

			try {
				uuid = hamster.directory(fdptr, null, null);
				fail("Expected HamsterRPCException_NotFound");
			} catch (HamsterRPCException_NotFound e) {
			}
		}

		// testcase 6: einmal alle von goldies (1x)
		@Test
		public void testAllHamsterBlondy() throws Exception {

			try {
				HamsterTestDataStore.getInstance().createTestdata8();
				connect();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				fail();
			}


			HamsterHandle fdptr = new HamsterHandle();
			int expectedUUID = hamster.lookup("otto", "blondy");

			fdptr.fdptr = -1;

			int uuid = hamster.directory(fdptr, null, "blondy");

			assertEquals(expectedUUID, uuid);
			assertNotEquals(-1, fdptr.fdptr);

			try {
				uuid = hamster.directory(fdptr, "blondy", null);
				fail("Expected HamsterRPCException_NotFound");
			} catch (HamsterRPCException_NotFound e) {
			}
		}

		// testcase 7: einmal alle von goldies (2x)
		@Test
		public void testAllHamsterBlondy2() throws Exception {

			assertTrue(HamsterTestDataStore.getInstance().copyTestHamsterfile("td12.dat"));

			HamsterHandle fdptr = new HamsterHandle();
			connect();
			int expectedUUID = hamster.lookup("otto", "blondy");

			fdptr.fdptr = -1;

			int uuid = hamster.directory(fdptr, null, "blondy");

			assertEquals(expectedUUID, uuid);
			assertNotEquals(-1, fdptr.fdptr);

			uuid = hamster.directory(fdptr, null, "blondy");

			assertEquals(hamster.lookup("hans", "blondy"), uuid);
			assertNotEquals(-1, fdptr.fdptr);

			try {
				hamster.directory(fdptr, null, "blondy");
				fail("Expected HamsterRPCException_NotFound");
			} catch (HamsterRPCException_NotFound e) {
			}
		}

		// testcase 8: falscher fdptr
		@Test
		public void testWrongFdptr() throws Exception {
		
			HamsterTestDataStore.getInstance().copyTestHamsterfile("td1.dat");
			HamsterHandle fdptr = new HamsterHandle();
			
			try {
				connect();
				
				fdptr.fdptr = 3849;
				
				hamster.directory(fdptr, null, null);
				
				// do not necessarily expect storage error
				
			} catch (HamsterRPCException_StorageError e) {
			}
		}
	
}
