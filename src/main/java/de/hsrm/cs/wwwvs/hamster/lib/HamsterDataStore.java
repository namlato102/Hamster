package de.hsrm.cs.wwwvs.hamster.lib;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * HamsterDataStores manages @see DataStoreEntry objects. On creation entiers
 * will be read from hamsterdatastore.xml. Modifikations made to the dataStore
 * result in an update of hamsterdatastore.xml
 * 
 * @author Olga Dedi
 */
public class HamsterDataStore implements Iterable<DataStoreEntry> {

	private ArrayList<DataStoreEntry> dataStore;

	/*
	 * single DataStore instance
	 */
	private static HamsterDataStore instance = null;

	/*
	 * make default constructor private
	 */
	private HamsterDataStore() {
		dataStore = new ArrayList<>();
		try {
			createDataStoreFromXML();
		} catch (HamsterStorageException | HamsterDatabaseCorruptException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	/**
	 * Singleton pattern to create a single DataStore instance
	 * 
	 * @return HamsterDataStore instance
	 */
	public static HamsterDataStore getInstance() {
		if (instance == null) {
			instance = new HamsterDataStore();
		}
		return instance;
	}

	/**
	 * Creates a hash code of both parameters
	 * 
	 * @param ownerName
	 *            Name of the owner
	 * @param hamsterName
	 *            Name of the hamster
	 * @return hash code generated from both parameters
	 */
	private int createHash(String ownerName, String hamsterName) {
		String key = ownerName + hamsterName;
		var code = key.hashCode();
		return Math.abs(code);
	}

	/**
	 * Write a new entry into the DataStore
	 * 
	 * @param ownerName
	 *            name of the owner
	 * @param hamsterName
	 *            name of the hamster
	 * @param treats
	 *            avaliable treats
	 * @return id of the created entry
	 * @throws HamsterAlreadyExistsException
	 * @throws HamsterStorageException
	 */
	public int addEntry(String ownerName, String hamsterName, short treats)
			throws HamsterAlreadyExistsException, HamsterStorageException {

		int id = createHash(ownerName, hamsterName);
		
		if (entryExists(id)) {
			throw new HamsterAlreadyExistsException();
		}

		DataStoreEntry newEntry = new DataStoreEntry(id, ownerName, hamsterName, treats);
		dataStore.add(newEntry);

		writeDataStoreToXML();

		return id;
	}

	/**
	 * Searches for the given id and returns true ifthe entry exists
	 * 
	 * @param id
	 *            the id to search for
	 * @return true if an entry with the given id exists, else false
	 */
	public boolean entryExists(int id) {
		for (DataStoreEntry dataStoreEntry : dataStore) {
			if (dataStoreEntry.id == id) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Searches for the given id and returns the DataStoreEntry object or null
	 * 
	 * @param id
	 *            the id to search for
	 * @return DataStoreEntry
	 * @throws HamsterNotFoundException
	 */
	public DataStoreEntry getEntryById(int id) throws HamsterNotFoundException {
		for (DataStoreEntry dataStoreEntry : dataStore) {
			if (dataStoreEntry.id == id) {
				return dataStoreEntry.copy();
			}
		}
		throw new HamsterNotFoundException();
	}

	/**
	 * Removes the entry with the given id
	 * 
	 * @param id
	 *            id to remove
	 * @throws HamsterNotFoundException
	 *             if the given id doesn't exist in the data store
	 * @throws HamsterStorageException
	 */
	public void removeEntryById(int id) throws HamsterNotFoundException, HamsterStorageException {
		for (DataStoreEntry dataStoreEntry : dataStore) {
			if (dataStoreEntry.id == id) {
				dataStore.remove(dataStoreEntry);
				writeDataStoreToXML();
				return;
			}
		}
		throw new HamsterNotFoundException();
	}

	public void updateEntry(DataStoreEntry newEntry) throws HamsterNotFoundException, HamsterStorageException {
		int index = dataStore.indexOf(getEntryById(newEntry.id));
		if (index == -1) {
			throw new HamsterNotFoundException();
		}
		
		DataStoreEntry entry = dataStore.get(index);
		entry.price = newEntry.price;
		entry.treats = newEntry.treats;
		
		writeDataStoreToXML();
	}

	/**
	 * Gets the entry on a given position
	 * 
	 * @param index
	 *            entry position
	 * @return element in position index
	 */
	public DataStoreEntry get(int index) {
		return dataStore.get(index).copy();
	}

	/**
	 * The current size of the data store
	 * 
	 * @return Current size of the data store
	 */
	public int size() {
		return dataStore.size();
	}

	/**
	 * Deletes all entries from DataStore
	 * 
	 * @throws HamsterStorageException
	 */
	public void clear() throws HamsterStorageException {
		dataStore.clear();
		writeDataStoreToXML();
	}

	/**
	 * Create new HamsterIterator
	 */
	@Override
	public HamsterIterator iterator() {
		return new HamsterIterator();
	}

	/**
	 * Creates a DOM document for all DataStoreEntries
	 * 
	 * @return DOM document
	 * @throws ParserConfigurationException
	 */
	private Document createXML() throws ParserConfigurationException {
		// Create DOM document
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.newDocument();
		// create root element
		Element rootElement = doc.createElement("HamsterDataStore");
		doc.appendChild(rootElement);

		for (DataStoreEntry entry : dataStore) {
			// create data store entry
			Element entryElement = doc.createElement("DataStoreEntry");
			rootElement.appendChild(entryElement);
			// create id element
			Element idElement = doc.createElement("id");
			idElement.appendChild(doc.createTextNode(String.valueOf(entry.id)));
			entryElement.appendChild(idElement);
			// create ownerName element
			Element ownerNameElement = doc.createElement("ownerName");
			ownerNameElement.appendChild(doc.createTextNode(entry.ownerName));
			entryElement.appendChild(ownerNameElement);
			// create hamsterName element
			Element hamsterNameElement = doc.createElement("hamsterName");
			hamsterNameElement.appendChild(doc.createTextNode(entry.hamsterName));
			entryElement.appendChild(hamsterNameElement);
			// create admissionTime element
			Element admissionTimeElement = doc.createElement("admissionTime");
			admissionTimeElement.appendChild(doc.createTextNode(String.valueOf(entry.admissionTime.getTime())));
			entryElement.appendChild(admissionTimeElement);
			// create price element
			Element priceElement = doc.createElement("price");
			priceElement.appendChild(doc.createTextNode(String.valueOf(entry.price)));
			entryElement.appendChild(priceElement);
			// create price element
			Element treatsElement = doc.createElement("treats");
			treatsElement.appendChild(doc.createTextNode(String.valueOf(entry.treats)));
			entryElement.appendChild(treatsElement);
		}

		return doc;
	}

	/**
	 * Writes the content of the data store to a XML file
	 * 
	 * @throws HamsterStorageException
	 */
	private void writeDataStoreToXML() throws HamsterStorageException {
		Document doc;
		try {
			doc = createXML();

			// write xml document to file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File("hamsterdatastore.xml"));
			transformer.transform(source, result);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			throw new HamsterStorageException();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
			throw new HamsterStorageException();
		} catch (TransformerException e) {
			e.printStackTrace();
			throw new HamsterStorageException();
		}
	}

	/**
	 * Reads a xml file and creates all data store entries according to the xml
	 * 
	 * @return List of entries
	 * @throws HamsterStorageException
	 * @throws HamsterDatabaseCorruptException
	 */
	private ArrayList<DataStoreEntry> readXML()
			throws HamsterStorageException, HamsterDatabaseCorruptException {
		File inputFile = new File("hamsterdatastore.xml");
		if (!inputFile.exists()) {
			return new ArrayList<DataStoreEntry>();
		}

		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser;
		try {
			saxParser = factory.newSAXParser();
			HamsterSAXHandler hamsterHandler = new HamsterSAXHandler();
			saxParser.parse(inputFile, hamsterHandler);
			return hamsterHandler.getEntries();
		} catch (ParserConfigurationException | IOException e) {
			e.printStackTrace();
			throw new HamsterStorageException();
		} catch (SAXException e) {
			e.printStackTrace();
			throw new HamsterDatabaseCorruptException();
		}
	}

	/**
	 * Clears the current data store and reads entries from xml file
	 * 
	 * @throws HamsterStorageException
	 * @throws HamsterDatabaseCorruptException
	 */
	private void createDataStoreFromXML() throws HamsterStorageException, HamsterDatabaseCorruptException {
		ArrayList<DataStoreEntry> entries = readXML();
		dataStore.clear();
		dataStore.addAll(entries);
	}
}
