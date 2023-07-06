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

import de.hsrm.cs.wwwvs.hamster.rpc.client.HamsterRPCConnection;
import de.hsrm.cs.wwwvs.hamster.tests.HamsterTestDataStore;

public class TestMultipleClients {

	private static Process sut = null;
	static HamsterTestDataStore store = HamsterTestDataStore.getInstance();
	static HamsterRPCConnection hmstr = null;

	static int port = store.getPort();
	static String hostname = "localhost";
	
	@Rule
	public Timeout globalTimeout= new Timeout(HamsterTestDataStore.getInstance().testcaseTimeoutms, TimeUnit.MILLISECONDS);

	@Before
	public void setUp() {

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

		HamsterTestDataStore.getInstance().wipeHamsterfile();
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
	public void connect_two_clients() {
		assertTrue("Server process is not running.", sut.isAlive());
		HamsterRPCConnection hmstr2 = null;
		try {
			hmstr2 = new HamsterRPCConnection(hostname, port, true);
			assertTrue(hmstr2 != null);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			fail("Second client failed to connect to server: " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			fail("Second client failed to connect to server: " + e.getMessage());
		}

		try {
			if (hmstr2 != null) {
				hmstr2.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void connect_three_clients() {
		assertTrue("Server process is not running.", sut.isAlive());
		HamsterRPCConnection hmstr2 = null;
		HamsterRPCConnection hmstr3 = null;
		try {
			hmstr2 = new HamsterRPCConnection(hostname, port, true);
			assertTrue(hmstr2 != null);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			fail("Second client failed to connect to server: " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			fail("Second client failed to connect to server: " + e.getMessage());
		}
		
		try {
			hmstr3 = new HamsterRPCConnection(hostname, port, true);
			assertTrue(hmstr3 != null);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			fail("Third client failed to connect to server: " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			fail("third client failed to connect to server: " + e.getMessage());
		}

		try {
			if (hmstr2 != null) {
				hmstr2.close();
			}
			if (hmstr3 != null) {
				hmstr3.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void connect_four_clients() {
		assertTrue("Server process is not running.", sut.isAlive());
		HamsterRPCConnection hmstr2 = null;
		HamsterRPCConnection hmstr3 = null;
		HamsterRPCConnection hmstr4 = null;
		try {
			hmstr2 = new HamsterRPCConnection(hostname, port, true);
			assertTrue(hmstr2 != null);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			fail("Second client failed to connect to server: " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			fail("Second client failed to connect to server: " + e.getMessage());
		}
		
		try {
			hmstr3 = new HamsterRPCConnection(hostname, port, true);
			assertTrue(hmstr3 != null);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			fail("Third client failed to connect to server: " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			fail("Third client failed to connect to server: " + e.getMessage());
		}
		
		try {
			hmstr4 = new HamsterRPCConnection(hostname, port, true);
			assertTrue(hmstr4 != null);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			fail("Fourth client failed to connect to server: " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			fail("Fourth client failed to connect to server: " + e.getMessage());
		}

		try {
			if (hmstr2 != null) {
				hmstr2.close();
			}
			if (hmstr3 != null) {
				hmstr3.close();
			}
			if (hmstr4 != null) {
				hmstr4.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void connect_fife_clients() {
		assertTrue("Server process is not running.", sut.isAlive());
		HamsterRPCConnection hmstr2 = null;
		HamsterRPCConnection hmstr3 = null;
		HamsterRPCConnection hmstr4 = null;
		HamsterRPCConnection hmstr5 = null;
		try {
			hmstr2 = new HamsterRPCConnection(hostname, port, true);
			assertTrue(hmstr2 != null);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			fail("Second client failed to connect to server: " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			fail("Second client failed to connect to server: " + e.getMessage());
		}
		
		try {
			hmstr3 = new HamsterRPCConnection(hostname, port, true);
			assertTrue(hmstr3 != null);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			fail("Third client failed to connect to server: " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			fail("Third client failed to connect to server: " + e.getMessage());
		}
		
		try {
			hmstr4 = new HamsterRPCConnection(hostname, port, true);
			assertTrue(hmstr4 != null);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			fail("Fourth client failed to connect to server: " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			fail("Fourth client failed to connect to server: " + e.getMessage());
		}
		
		try {
			hmstr5 = new HamsterRPCConnection(hostname, port, true);
			assertTrue(hmstr5 != null);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			fail("Fifth client failed to connect to server: " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			fail("Fifth client failed to connect to server: " + e.getMessage());
		}

		try {
			if (hmstr2 != null) {
				hmstr2.close();
			}
			if (hmstr3 != null) {
				hmstr3.close();
			}
			if (hmstr4 != null) {
				hmstr4.close();
			}
			if (hmstr5 != null) {
				hmstr5.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
