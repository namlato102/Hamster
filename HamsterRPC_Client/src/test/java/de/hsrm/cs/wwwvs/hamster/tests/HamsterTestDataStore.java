package de.hsrm.cs.wwwvs.hamster.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.function.Consumer;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class HamsterTestDataStore {

	private String pathToHamsterExe = "java -jar ../tests/hamster.jar";
	private String pathToHamsterServer = "java -jar ../build/libs/hamster_server.jar";
	private String pathToHamsterFile = System.getProperty("user.dir");
	private String hamsterFileName = "hamsterdatastore.xml";
	private String testdatenPath = "../testdaten/";

	private int port = 8088;
	
	private  static int sleepMin = 100;
	private  static int sleepMed = 200;
	private  static int sleepMax = 4000;
	
	public int testcaseTimeoutms = 600000;

	private static Consumer<String> logProcessor;

	/*
	 * 
	 * Testdaten
	 * 			besitzer	hamster		treats	price	revol
	 * td1:		otto		heinz		23		0
	 * 
	 * td2:		otto		heinz		0		23
	 * 
	 * td3:		diesnameee123456789012345678901		langerName	0		0		
	 * 
	 * td4:		diesnameee123456789012345678901		diesnameee123456789012345678902		0		0
	 * 
	 * td5 		otto		heinz		65535	0
	 * 
	 * td6		otto{1..50} heinz{1..50} {1..50} 0
	 * 
	 * td7		otto 		heinz 		23
	 * 			karl 		blondy 		42
	 * 
	 * td8		otto		heinz 		23
	 * 			otto		blondy 		42
	 * 
	 * td9		otto		heinz 		18
	 * 
	 * td10		otto		heinz		0 (after giving 50)
	 * 
	 * td11		otto		heinz		0		65535
	 * 
	 * td12		otto 		blondy 23
	 *			hans 		blondy 23
     * 			ernst 		foo 23
     * 
     * td13		otto		heinz		23
     * 			bernd		blondy		42
	 */
	
	
	public static void sleepMin() {
		try {
			Thread.sleep(sleepMin);
		} catch (InterruptedException e) {
			
		}
	}
	public static void sleepMid() {
		try {
			Thread.sleep(sleepMed);
		} catch (InterruptedException e) {
			
		}
	}
	public static void sleepMax() {
		try {
			Thread.sleep(sleepMax);
		} catch (InterruptedException e) {
			
		}
	}
	
	/**
	 * deletes the hamsterfile.dat
	 * @return
	 */
	public boolean wipeHamsterfile() {
		
		Path path = Paths.get(this.pathToHamsterFile, this.hamsterFileName);
		
		try {
			Files.deleteIfExists(path);
		} catch (IOException e) {
			showException(e);
			return false;
		}

		return true;
	}
	
	public boolean compareHamsterFileEqual(String hamsterTestFileName) throws IOException {
		
		String userDir = System.getProperty("user.dir");
		Path testFilePath = Paths.get(userDir, this.testdatenPath, hamsterTestFileName);
		
		
		byte[] sutFile = Files.readAllBytes(Paths.get(this.pathToHamsterFile, this.hamsterFileName));
		byte[] testFile = Files.readAllBytes(testFilePath);
		
		int countByteSUT = sutFile.length;
		int countByteTest = testFile.length;
		
		if (countByteSUT != countByteTest) {
			System.out.println("Hamsterfiles have different sizes");
			return false;
		}
		
		return true;		
	}

	public boolean copyTestHamsterfile(String hamsterTestFileName) {
			
		String userDir = System.getProperty("user.dir");
				
		Path srcFile = Paths.get(userDir, this.testdatenPath, hamsterTestFileName);		
		Path destFile = Paths.get(this.pathToHamsterFile, hamsterFileName);
		
		try {
			System.out.println(String.format("Copy %s to %s", srcFile, destFile));
			Files.copy(srcFile, destFile, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			showException(e);
			return false;
		}

		return true;
	}

	public void createTestdata1() throws IOException {

		Process sut = Runtime.getRuntime().exec(this.pathToHamsterExe + " add otto heinz 23", null, new File(pathToHamsterFile));
		waitToCompletion(sut);
	}

	public void createTestdata2() throws IOException {

		Process sut = Runtime.getRuntime().exec(this.pathToHamsterExe + " add otto heinz 0", null, new File(pathToHamsterFile));
		waitToCompletion(sut);
		for (int i = 18; i < 23; i++) {
			sut = Runtime.getRuntime().exec(this.pathToHamsterExe + " state otto heinz", null, new File(pathToHamsterFile));
			waitToCompletion(sut);
		}
	}

	public void createTestdata5() throws IOException {

		Process sut = Runtime.getRuntime().exec(this.pathToHamsterExe + " add otto heinz 32767", null, new File(pathToHamsterFile));
		waitToCompletion(sut);
		for (int i = 18; i < 50; i++) {
			sut = Runtime.getRuntime().exec(this.pathToHamsterExe + " state otto heinz", null, new File(pathToHamsterFile));
			waitToCompletion(sut);
		}
	}
	
	public void createTestdata11( ) throws IOException {
		
		Process sut = Runtime.getRuntime().exec(this.pathToHamsterExe + " add otto heinz 0", null, new File(pathToHamsterFile));
		waitToCompletion(sut);
		sut = Runtime.getRuntime().exec(this.pathToHamsterExe + " feed otto heinz 16374", null, new File(pathToHamsterFile));
		waitToCompletion(sut);
	}
	
	
	
	public void createTestdata8( ) throws IOException {
		Process sut = Runtime.getRuntime().exec(this.pathToHamsterExe + " add otto heinz 23", null, new File(pathToHamsterFile));
		waitToCompletion(sut);
		sut = Runtime.getRuntime().exec(this.pathToHamsterExe + " add otto blondy 42", null, new File(pathToHamsterFile));
		waitToCompletion(sut);
	}

	private void waitToCompletion(Process sut) {
		try {
			var exitCode = sut.waitFor();
			if (exitCode != 0) {
				System.err.println("Warning: Hamster returned non-zero exit-code " + exitCode);
			}
		} catch (InterruptedException e) {
			showException(e);
		}
		sleepMin();
	}

	public void createTestdata13() throws IOException {
		Process sut = Runtime.getRuntime().exec(this.pathToHamsterExe + " add otto heinz 23", null, new File(pathToHamsterFile));
		waitToCompletion(sut);
		sut = Runtime.getRuntime().exec(this.pathToHamsterExe + " add bernd blondy 42", null, new File(pathToHamsterFile));
		waitToCompletion(sut);
	}
	public Process startHamsterServer(int port) throws IOException {
		return startHamsterServer(port, 0.0);
	}

	public Process startHamsterServer(int port, double feedFailProbability) throws IOException {
		String sutPath = getPathToHamsterServer();
		if (feedFailProbability > 0) {
			sutPath += " -f " + feedFailProbability;
		}

		System.out.println("Starting server on port " + port);
		Process sut = Runtime.getRuntime().exec(sutPath + " -p " + port, null, new File(pathToHamsterFile));

		Process finalSut = sut;
		Thread outThread = new Thread(() -> {
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(finalSut.getInputStream()))) {
				String line;
				while ((line = reader.readLine()) != null) {
					System.out.println("Server: " + line);
					if (logProcessor != null) {
						logProcessor.accept(line);
					}
				}
			} catch (Exception e) {
			}
		});
		outThread.start();

		assertTrue("Server process is not running.", sut.isAlive());

		HamsterTestDataStore.sleepMax();
		return sut;
	}

	public static Consumer<String> getLogProcessor() {
		return logProcessor;
	}

	public static void setLogProcessor(Consumer<String> processor) {
		logProcessor = processor;
	}
	
	public String getPathToHamsterServer( ) {
		
		return this.pathToHamsterServer;
	}
	
	public String getPathToHamsterFile() {
		
		return this.pathToHamsterFile;
	}

	public void setPathToHamsterServer(String p) {
		this.pathToHamsterServer = p;
	}

	public void setPathToHamsterFile(String p) {
		this.pathToHamsterFile = p;
	}
	
	public void setPathToHamsterExe(String p) {
		this.pathToHamsterExe = p;
	}
	public String getPathToHamsterExe() {
		return this.pathToHamsterExe;
	}

	public String getHamsterFileName() { return this.hamsterFileName; }

	public void setHamsterFileName(String hamsterFileName) { this.hamsterFileName = hamsterFileName; }

	private static HamsterTestDataStore inst = null;

	public static HamsterTestDataStore getInstance() {

		if (inst == null) {
			inst = new HamsterTestDataStore();
		}

		return inst;
	}

	private HamsterTestDataStore() {
	}

	private void showException(Exception e) {
		System.out.println("Error during hamsterfile tampering: " + e.getMessage());
	}

	public int getPort() {
		return port++;
	}

	public void setPort(int port) {
		this.port = port;
	}

}
