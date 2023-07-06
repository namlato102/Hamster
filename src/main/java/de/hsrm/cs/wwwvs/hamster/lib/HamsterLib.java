package de.hsrm.cs.wwwvs.hamster.lib;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class HamsterLib {

	private static double feedFailureProbability;

	public static void setFeedFailureProbability(double value) {
		feedFailureProbability = value;
	}

	private static Random random = new Random(68729);
	
	public class OutString {
		String value;
		
		public OutString() {
			this(null);
		}
		
		public OutString(String value) {
			this.value = value;
		}
		
		public String getValue() {
			return this.value;
		}
		
		public void setValue(String value) {
			this.value = value;
		}
	}
	
	public class OutShort {
		short value;
		
		public OutShort() {
			this((short)0);
		}
		
		public OutShort(short value) {
			this.value = value;
		}
		
		public short getValue() {
			return this.value;
		}
		
		public void setValue(short value) {
			this.value = value;
		}
	}

	public static final int HMSTR_MAX_NAME = 31;
	public static final int HMSTR_RPM = 25;
	public static final short HMSTR_BASE_PRICE = 17;
	public static final short HMSTR_STAY_PRICE = 5;
	public static final short HMSTR_FEED_PRICE = 2;
	public static final short HMSTR_CARE_PRICE = 1;

	private static HamsterDataStore dataStore = HamsterDataStore.getInstance();

	/**
	 * @brief Put a new hamster into the hamstercare institute
	 * 
	 *        This function makes a new entry into the hamster database. It returns
	 *        a unique ID by which the hamster can later be referenced. A Hamster
	 *        may be given a number of treats to feed.
	 * 
	 * @param ownerName name of hamster's owner
	 * @param hamsterName name of hamster
	 * @param treats initial provision of treats
	 * @return If successful: Unique ID (always >= 0) of the new entry
	 * @return On error: Error code (always < 0)
	 * @throws HamsterNameTooLongException
	 * @throws HamsterAlreadyExistsException
	 * @throws HamsterStorageException
	 * @throws HamsterDatabaseCorruptException
	 * 
	 */
	public int new_(String ownerName, String hamsterName, short treats) throws HamsterNameTooLongException,
			HamsterAlreadyExistsException, HamsterStorageException, HamsterDatabaseCorruptException {

		if (ownerName.length() > HMSTR_MAX_NAME || hamsterName.length() > HMSTR_MAX_NAME) {
			throw new HamsterNameTooLongException();
		}

		if (treats < 0) {
			treats = 0;
		}

		return dataStore.addEntry(ownerName, hamsterName, treats);
	}

	/**
	 * @brief Find hamster in the hamstercare institute
	 * 
	 *        This function locates an entry in the hamster database. It returns a
	 *        unique ID by which the hamster can be referenced. A Hamster is
	 *        uniquely identified by the combination of the owner's name and the
	 *        hamster's name.
	 * 
	 * @param ownerName name of hamster's owner
	 * @param hamsterName name of hamster
	 * @return If successful: Unique ID (always >= 0) of the entry
	 * @return On error: Error code (always < 0)
	 * @throws HamsterNameTooLongException
	 * @throws HamsterNotFoundException
	 */
	public int lookup(String ownerName, String hamsterName)
			throws HamsterNameTooLongException, HamsterNotFoundException {
		if (ownerName.length() > HMSTR_MAX_NAME || hamsterName.length() > HMSTR_MAX_NAME) {
			throw new HamsterNameTooLongException();
		}

		for (DataStoreEntry entry : dataStore) {
			if (entry.ownerName.equals(ownerName) && entry.hamsterName.equals(hamsterName)) {
				return entry.id;
			}
		}

		throw new HamsterNotFoundException();
	}

	/**
	 * @brief Get a directory of entries in the database
	 * 
	 *        This function enables a "wildcard search" of the database. It delivers
	 *        UIDs of matching entries. The caller may specify an owner name or a
	 *        hamster name, thus specifying a particular entry (in this case the
	 *        function is similar to hmstr_lookup()). However, it is also possible
	 *        to specify __only__ an owner name or __only__ a hamster name by
	 *        passing a NULL value for the name that should not be specified. In
	 *        this case, the function delivers UIDs of __all__ entries matching the
	 *        specified name. If both names are passed as NULL, the function
	 *        delivers the UIDs of __all__ entries in the database.
	 * 
	 *        The function delivers __one__ __UID__ __per__ __call__. In order to
	 *        keep the context across calls, the caller must pass a pointer to an
	 *        int variable. This variable (__not__ the pointer!) must be initialized
	 *        with -1 before making the first call.
	 * 
	 * @param iterator iterator that will be used to get the next entry
	 * @param ownerName name of hamster's owner or null if not specified
	 * @param hamsterName name of hamster or null if not specified
	 * @return If successful: Unique ID (always >= 0) of the next matching entry
	 * @return On error: Error code (always < 0)
	 * @throws HamsterEndOfDirectoryException
	 * @throws HamsterNameTooLongException
	 * @throws HamsterNotFoundException
	 */
	public int directory(HamsterIterator iterator, String ownerName, String hamsterName)
			throws HamsterNameTooLongException, HamsterNotFoundException, HamsterEndOfDirectoryException {
		// length check for ownerName and hamsterName
		if ((ownerName != null && ownerName.length() > HMSTR_MAX_NAME)
				|| (hamsterName != null && hamsterName.length() > HMSTR_MAX_NAME)) {
			throw new HamsterNameTooLongException();
		}

		int iterations = 0;
		DataStoreEntry entry;
		while (iterator.hasNext()) {
			iterations++;
			entry = iterator.next();
			if ((ownerName == null || entry.ownerName.equals(ownerName))
					&& (hamsterName == null || entry.hamsterName.equals(hamsterName))) {
				return entry.id;
			}
		}

		// Each entry was seen in one run, but the requested entry wasn't found.
		if (iterations == dataStore.size()) {
			throw new HamsterNotFoundException();
		}

		throw new HamsterEndOfDirectoryException();
	}

	/**
	 * @brief How is my hamster doing?
	 * 
	 *        This function checks upon hamster (at a cost!) identified by ID. It
	 *        returns the hamster's state in the given data structure.
	 * 
	 * @param id Hamster's unique ID
	 * @param state pointer to data structure where to store information
	 * @return If successful: 0, state of hamster stored in state struct
	 * @return On error: Error code (always < 0)
	 * @throws HamsterNotFoundException
	 * @throws HamsterStorageException
	 */
	public int howsdoing(int id, HamsterState state) throws HamsterNotFoundException, HamsterStorageException {
		DataStoreEntry entry = dataStore.getEntryById(id);
		Date now = new Date();
		long duration = now.getTime() - dataStore.get(0).admissionTime.getTime();
		duration = duration / 1000; // milliseconds to seconds
		int rounds = (int) ((duration * HMSTR_RPM) / 60); // duration is in milliseconds
		state.setRounds(rounds);
		
		/*
         * add cost of stay (= HMSTR_STAY_PRICE * rounds/1000)
         */
		entry.price += HMSTR_CARE_PRICE;
		state.setCost(entry.price);
		state.setTreatsLeft(entry.treats);
		dataStore.updateEntry(entry);
		
		return 0;
	}

	/**
	 * @brief Get contents of an entry in the database
	 * 
	 *        This function delivers details of a hamster identified by UID. The
	 *        price is __not__ changed by a call to this function.
	 * 
	 * @param id Hamster's unique ID
	 * @param ownerName where to store the owner's name
	 * @param hamsterName where to store the hamster's name
	 * @param price where to store the price
	 * @return If successful: number of treats left in hamster' store
	 * @return On error: Error code (always < 0)
	 * @throws HamsterNotFoundException
	 */
	public short readentry(int id, OutString ownerName, OutString hamsterName, OutShort price)
			throws HamsterNotFoundException {
		DataStoreEntry entry = dataStore.getEntryById(id);
		
		ownerName.setValue(entry.ownerName);
		hamsterName.setValue(entry.hamsterName);
		price.setValue(entry.price);
		
		return entry.treats;
	}

	/**
	 * @brief Give treats to my hamster
	 * 
	 *        This function gives treats to the hamster identified by ID. The
	 *        Hamster's stock of treats will be used up first. If stock is
	 *        insufficient, more treats will be dispensed (at a cost!) and the
	 *        function returns a benign error.
	 * 
	 * @param id
	 *            Hamster's unique ID
	 * @param treats
	 *            How many treats to feed
	 * @return If successful: number of treats left in stock (always >=0)
	 * @return On error: Error code (always < 0)
	 * @throws HamsterNotFoundException
	 * @throws HamsterStorageException
	 * @throws HamsterRefusedTreatException
	 */
	public short givetreats(int id, short treats)
			throws HamsterNotFoundException, HamsterStorageException, HamsterRefusedTreatException {

		if (random.nextDouble() < feedFailureProbability) {
			throw new HamsterRefusedTreatException();
		}

		if (treats < 0) {
			treats = 0;
		}
		
		DataStoreEntry entry = dataStore.getEntryById(id);
		entry.treats -= treats;
		if (entry.treats < 0) {
			entry.price  -= HMSTR_FEED_PRICE * entry.treats;
			entry.treats = 0;
		}
		dataStore.updateEntry(entry);	
		return entry.treats;
	}

	/**
	 * @brief Collect all my hamsters and pay the bill
	 * 
	 *        This function collects (i.e. deletes from the database) all hamsters
	 *        owned by the specified owner and sums up all their expenses to produce
	 *        a final bill.
	 * 
	 * @param ownerName
	 *            name of hamster owner
	 * @return If successful: number of euros to pay
	 * @return On error: Error code (always < 0)
	 * @throws HamsterNotFoundException
	 * @throws HamsterStorageException
	 */
	public short collect(String ownerName)
			throws HamsterNotFoundException, HamsterStorageException, HamsterNameTooLongException {
		if (ownerName != null && ownerName.length() > HMSTR_MAX_NAME) {
			throw new HamsterNameTooLongException();
		}
		
		Date now = new Date();		
		short price = 0;
		HamsterIterator it = iterator();
		
		ArrayList<Integer> deleteIds = new ArrayList<>();
		
		while (it.hasNext()) {
			try {
				int id = directory(it, ownerName, null);
				long duration = now.getTime() - dataStore.get(0).admissionTime.getTime();
				duration = duration / 1000; // milliseconds to seconds
				int rounds = (int) ((duration * HMSTR_RPM) / 60); // duration is in milliseconds
				price += dataStore.getEntryById(id).price + (rounds * HMSTR_STAY_PRICE) / 1000;
				deleteIds.add(id);
			} catch (HamsterEndOfDirectoryException e) {
				// expected
			}
		}

		if (deleteIds.size() == 0) {
			throw new HamsterNotFoundException();
		}
		
		for (Integer id : deleteIds) {
			dataStore.removeEntryById(id);
		}
		
		return price;
	}

	/**
	 * Creates a HasterIterator object
	 * @return HamsterIterator
	 */
	public HamsterIterator iterator() {
		return dataStore.iterator();
	}
}
