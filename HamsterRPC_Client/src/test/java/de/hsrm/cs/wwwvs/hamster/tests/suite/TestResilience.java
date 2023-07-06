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
import org.junit.*;
import org.junit.rules.Timeout;

import de.hsrm.cs.wwwvs.hamster.tests.HamsterTestDataStore;

public class TestResilience {

    private static Process sut = null;

    private static int port = HamsterTestDataStore.getInstance().getPort();

    private HamsterClient hamster = null;

    @Rule
    public Timeout globalTimeout= new Timeout(HamsterTestDataStore.getInstance().testcaseTimeoutms, TimeUnit.MILLISECONDS);



    @Before
    public void setUp() throws Exception {
        HamsterTestDataStore.getInstance().wipeHamsterfile();
        HamsterTestDataStore.setLogProcessor(line -> analyzeServerLog(line));
    }

    private void connect() throws IOException {
        sut = HamsterTestDataStore.getInstance().startHamsterServer(port, 0.5);

        hamster = new HamsterClient(port);

        HamsterTestDataStore.sleepMin();
    }

    @After
    public void tearDown() {
        HamsterTestDataStore.sleepMin();

        if (sut != null) {
            sut.destroy();
            try {
                sut.waitFor();
            } catch (InterruptedException e) {}
        }

        assertFalse("Server process is not shuting down.", sut.isAlive());
    }

    private String lastSickHamsterLog;
    private String lastRefusedTreatLog;

    private int errors = 0;

    public int getErrors() {
        return errors;
    }

    private void analyzeServerLog(String logLine) {
        if (logLine.contains("refused treat, trying again")) {
            lastRefusedTreatLog = logLine;
        }
        if (logLine.contains("has refused treats even after retry, we need a veterinarian.")) {
            lastSickHamsterLog = logLine;
        }
    }

    private void reset() {
        lastSickHamsterLog = null;
        lastRefusedTreatLog = null;
    }

    private void checkHamsterRefusedAndReset(String owner, String hamster) {
        if (lastRefusedTreatLog != null && lastRefusedTreatLog.contains(owner) && lastRefusedTreatLog.contains(hamster)) {
            System.out.println("- Correctly identified that " + hamster + " refused treat");
        } else {
            System.out.println("=== Missing log message that " + hamster + " refused the treat ===");
            errors++;
        }
        reset();
    }

    private void checkHamsterSickAndReset(String owner, String hamster) {
        if (lastSickHamsterLog != null && lastSickHamsterLog.contains(owner) && lastSickHamsterLog.contains(hamster)) {
            System.out.println("- Correctly identified that " + hamster + " got sick");
        } else {
            System.out.println("=== Missing log message that " + hamster + " got sick ===");
            errors++;
        }
        reset();
    }

    private void checkNoRefusalAndReset() {
        if (lastSickHamsterLog != null) {
            System.out.println("=== Did not expect to see any sick hamsters, but received " + lastSickHamsterLog);
            errors++;
        }
        if (lastRefusedTreatLog != null) {
            System.out.println("=== Did not expect to see any errors, but received " + lastRefusedTreatLog);
            errors++;
        }
        reset();
    }

    private void checkTreatsLeft(String owner, String hamsterName, int treatsLeft) throws Exception {
        String howsDoingStatement = hamster.howsDoing(owner, hamsterName);
        if (!howsDoingStatement.contains(treatsLeft + " treats left in store")) {
            errors++;
            System.out.println("=== Treats left is wrong, expected " + treatsLeft + " but received " + howsDoingStatement);
        }
    }

    @Test
    public void feedingHamsterCausesSickness() throws Exception {
        HamsterTestDataStore.getInstance().createTestdata13();
        connect();

        hamster.giveTreats("otto", "heinz", 1);
        checkNoRefusalAndReset();
        checkTreatsLeft("otto", "heinz", 22);
        hamster.giveTreats("otto", "heinz", 1);
        checkHamsterRefusedAndReset("otto", "heinz");
        checkTreatsLeft("otto", "heinz", 21);
        hamster.giveTreats("bernd", "blondy", 1);
        checkHamsterRefusedAndReset("bernd", "blondy");
        checkTreatsLeft("bernd", "blondy", 41);
        hamster.giveTreats("otto", "heinz", 1);
        checkNoRefusalAndReset();
        checkTreatsLeft("otto", "heinz", 20);
        hamster.giveTreats("otto", "heinz", 1);
        checkHamsterRefusedAndReset("otto", "heinz");
        checkTreatsLeft("otto", "heinz", 19);
        hamster.giveTreats("otto", "heinz", 1);
        checkHamsterRefusedAndReset("otto", "heinz");
        checkTreatsLeft("otto", "heinz", 18);
        try {
            // expect that hamster is sick now
            hamster.giveTreats("otto", "heinz", 1);
        } catch (HamsterClientException ex) {
            checkHamsterSickAndReset("otto", "heinz");
            checkTreatsLeft("otto", "heinz", 18);
        }
        Thread.sleep(1000);
        try {
            hamster.giveTreats("otto", "heinz", 1);
            errors++;
            System.out.println("Hamster Heinz is sick, do not bother!");
        } catch (HamsterClientException ex) {
            reset();
        }
        checkTreatsLeft("otto", "heinz", 18);
        hamster.giveTreats("bernd", "blondy", 1);
        checkNoRefusalAndReset();
        assertTrue("Errors found, check log. In total, " + errors + " error found", errors == 0);
    }
}
