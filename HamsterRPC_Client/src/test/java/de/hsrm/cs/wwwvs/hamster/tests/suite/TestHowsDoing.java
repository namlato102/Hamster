package de.hsrm.cs.wwwvs.hamster.tests.suite;

import static org.junit.Assert.*;

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
import de.hsrm.cs.wwwvs.hamster.rpc.Hmstr.State;
import de.hsrm.cs.wwwvs.hamster.rpc.client.HamsterRPCConnection;
import de.hsrm.cs.wwwvs.hamster.tests.HamsterTestDataStore;

public class TestHowsDoing {

	private static Process sut = null;
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

	private static void connect() {

		try {
			sut = HamsterTestDataStore.getInstance().startHamsterServer(port);
			HamsterTestDataStore.sleepMin();
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
	public void howsdoing_td1() throws Exception {
		HamsterTestDataStore.getInstance().createTestdata1();
		connect();

		int uuid = hmstr.lookup("otto", "heinz");

		State state = new State();
		State expectedState = new State();
		expectedState.treatsLeft = 23;
		expectedState.cost = 18;

		int returnCode = hmstr.howsdoing(uuid, state);
		assertTrue("returnCode should be 0.", returnCode == 0);
		assertTrue("treatsLeft expected " + expectedState.treatsLeft + ", received " + state.treatsLeft, state.treatsLeft == expectedState.treatsLeft);
		assertTrue("cost expected " + expectedState.cost + ", received " + state.cost, state.cost == expectedState.cost);
	}

	@Test
	public void howsdoing_not_found() throws Exception {
		HamsterTestDataStore.getInstance().createTestdata1();
		connect();

		int uuid = 12345678;

		State state = new State();

		try {
			hmstr.howsdoing(uuid, state);
			fail("Expected exception");
		} catch (HamsterRPCException_NotFound e) {
		}
	}

	@Test
	public void howsdoing_td2() throws Exception {
		HamsterTestDataStore.getInstance().createTestdata2();
		connect();

		int uuid = hmstr.lookup("otto", "heinz");

		State state = new State();
		State expectedState = new State();
		expectedState.treatsLeft = 0;
		expectedState.cost = 23;

		int returnCode = hmstr.howsdoing(uuid, state);
		assertTrue("returnCode should be 0.", returnCode == 0);
		assertTrue("treatsLeft expected " + expectedState.treatsLeft + ", received " + state.treatsLeft, state.treatsLeft == expectedState.treatsLeft);
		assertTrue("cost expected " + expectedState.cost + ", received " + state.cost, state.cost == expectedState.cost);
	}

	@Test
	public void howsdoing_td5() throws Exception {
		HamsterTestDataStore.getInstance().createTestdata5();
		connect();

		int uuid = hmstr.lookup("otto", "heinz");

		State state = new State();
		State expectedState = new State();
		expectedState.treatsLeft = -1;  // workaround because of unsigned value not available in java (65535 => -1)
		expectedState.cost = 50;

		int returnCode = hmstr.howsdoing(uuid, state);
		assertTrue("returnCode should be 0.", returnCode == 0);
		assertTrue("treatsLeft expected " + expectedState.treatsLeft + ", received " + state.treatsLeft, state.treatsLeft == expectedState.treatsLeft || state.treatsLeft == 32767);
		assertTrue("cost expected " + expectedState.cost + ", received " + state.cost, state.cost == expectedState.cost);
	}

	// testcase 5: not additional payload after successful call
	@Test
	public void testNoAddPayloadAfterSuccCall() throws Exception {

		HamsterTestDataStore.getInstance().createTestdata1();
		connect();

		int uuid = hmstr.lookup("otto", "heinz");

		State state = new State();
		State expectedState = new State();
		expectedState.treatsLeft = 23;
		expectedState.cost = 18;

		hmstr.setTestNoPayloadAfterMessage(true);
		int returnCode = hmstr.howsdoing(uuid, state);
		assertTrue("returnCode should be 0.", returnCode == 0);

		int addByte = hmstr.receiveOneByte();
		assertTrue("Received payload after sucessful rpc call", (addByte == -1));
	}

	// testcase 6: no payload after error message
	@Test
	public void testNoAddPayloadAfterErrorCall() throws Exception {

		HamsterTestDataStore.getInstance().createTestdata1();
		connect();

		int uuid = 232342;

		State state = new State();
		State expectedState = new State();
		expectedState.treatsLeft = 23;
		expectedState.cost = 18;

		hmstr.setTestNoPayloadAfterMessage(true);

		try {
			int returnCode = hmstr.howsdoing(uuid, state);
			fail("Expected HamsterRPCException_StorageError");
		} catch (HamsterRPCException_NotFound e) {
		}

		int addByte = hmstr.receiveOneByte();
		assertTrue("Received payload after sucessfull rpc call", (addByte == -1));
	}

	// testcase 7: two calls in a row

	@Test
	public void testTwoCallsInARow() throws Exception {

		HamsterTestDataStore.getInstance().createTestdata13();
		connect();

		State state = new State();
		State expectedState = new State();
		expectedState.treatsLeft = 23;
		expectedState.cost = 18;

		int uuid = hmstr.lookup("otto", "heinz");

		int returnCode = hmstr.howsdoing(uuid, state);
		assertTrue("returnCode should be 0.", returnCode == 0);
		assertTrue("treatsLeft expected " + expectedState.treatsLeft + ", received " + state.treatsLeft, state.treatsLeft == expectedState.treatsLeft);
		assertTrue("cost expected " + expectedState.cost + ", received " + state.cost, state.cost == expectedState.cost);

		State state2 = new State();
		State expectedState2 = new State();
		expectedState2.treatsLeft = 42;
		expectedState2.cost = 18;

		int uuid2 = hmstr.lookup("bernd", "blondy");
		returnCode = hmstr.howsdoing(uuid2, state2);
		assertTrue("returnCode should be 0.", returnCode == 0);
		assertTrue("treatsLeft expected " + expectedState2.treatsLeft + ", received " + state2.treatsLeft, state2.treatsLeft == expectedState2.treatsLeft);
		assertTrue("cost expected " + expectedState2.cost + ", received " + state2.cost, state.cost == expectedState2.cost);
	}
}
