package de.hsrm.cs.wwwvs.hamster.lib;

import java.util.ArrayList;
import java.util.Date;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A SAX handler that can be used by a @see SAXParser to parse a
 * hamsterdatastore.xml file and crearte a list of DataStoreEntry objects based
 * on the xml file.
 * 
 * @author Olga Dedi
 */
public class HamsterSAXHandler extends DefaultHandler {

	/*
	 * entries generated from document
	 */
	private ArrayList<DataStoreEntry> entries = new ArrayList<>();
	/*
	 * Pointer to current entry
	 */
	DataStoreEntry current = null;

	/*
	 * Flag for reading text nodes
	 */
	boolean idFlag = false;
	boolean admissionTimeFlag = false;
	boolean ownerNameFlag = false;
	boolean hamsterNameFlag = false;
	boolean priceFlag = false;
	boolean treatsFlag = false;
	
	/*
	 * Flag for counting fiels in data store
	 */
	boolean idSeen = false;
	boolean admissionTimeSeen = false;
	boolean ownerNameSeen = false;
	boolean hamsterNameSeen = false;
	boolean priceSeen = false;
	boolean treatsSeen = false;

	/**
	 * return the entries that were generated from the xml file
	 * 
	 * @return List of DataStoreEntry
	 */
	public ArrayList<DataStoreEntry> getEntries() {
		return entries;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

		if (qName.equalsIgnoreCase("DataStoreEntry")) {
			current = new DataStoreEntry();
		} else if (qName.equalsIgnoreCase("id")) {
			idFlag = true;
			idSeen = true;
		} else if (qName.equalsIgnoreCase("admissionTime")) {
			admissionTimeFlag = true;
			admissionTimeSeen = true;
		} else if (qName.equalsIgnoreCase("ownerName")) {
			ownerNameFlag = true;
			ownerNameSeen = true;
		} else if (qName.equalsIgnoreCase("hamsterName")) {
			hamsterNameFlag = true;
			hamsterNameSeen = true;
		} else if (qName.equalsIgnoreCase("price")) {
			priceFlag = true;
			priceSeen = true;
		} else if (qName.equalsIgnoreCase("treats")) {
			treatsFlag = true;
			treatsSeen = true;
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {

		if (qName.equalsIgnoreCase("DataStoreEntry")) {
			boolean allFlagsSet = idSeen && admissionTimeSeen && ownerNameSeen && hamsterNameSeen && priceSeen && treatsSeen;
			idSeen = false;
			admissionTimeSeen = false;
			ownerNameSeen = false;
			hamsterNameSeen = false;
			priceSeen = false;
			treatsSeen = false;
			if (allFlagsSet) {
				entries.add(current);
			}
			else {
				throw new SAXException("HamsterRPCException_DatabaseCorrupt");
			} 
		}
	}

	@Override
	public void characters(char ch[], int start, int length) throws SAXException {

		if (idFlag) {
			current.id = Integer.parseInt(new String(ch, start, length));
			idFlag = false;
		} else if (admissionTimeFlag) {
			current.admissionTime = new Date(Long.parseLong(new String(ch, start, length)));
			admissionTimeFlag = false;
		} else if (ownerNameFlag) {
			current.ownerName = new String(ch, start, length);
			ownerNameFlag = false;
		} else if (hamsterNameFlag) {
			current.hamsterName = new String(ch, start, length);
			hamsterNameFlag = false;
		} else if (priceFlag) {
			current.price = Short.parseShort(new String(ch, start, length));
			priceFlag = false;
		} else if (treatsFlag) {
			current.treats = Short.parseShort(new String(ch, start, length));
			treatsFlag = false;
		}
	}
}
