package de.hsrm.cs.wwwvs.hamster.lib;

import java.util.Date;

/**
 * DataStoreEntrs objects are managed by the @see HamsterDataStore class.
 * @author Olga Dedi
 */
public class DataStoreEntry {
	public int id;
	public Date admissionTime;
	public String ownerName;
	public String hamsterName;
	public short price;
	public short treats;
	
	/**
	 * Creates a copy of this DataStoreEntry object
	 * @return copy of this objects
	 */
	public DataStoreEntry copy() {
		return new DataStoreEntry(this);
	}
	
	/**
	 * Default constructor
	 */
	public DataStoreEntry() {
		this.id = 0;
		this.admissionTime = null;
		this.ownerName = null;
		this.hamsterName = null;
		this.price = 0;
		this.treats = 0;
	}
	
	/**
	 * Creates DataStoreEntry object. Admission tie is set to now, default price is set
	 * @param id entry id
	 * @param ownerName owner name
	 * @param hamsterName hamster name
	 * @param treats treat count the hamster is given
	 */
	public DataStoreEntry(int id, String ownerName, String hamsterName, short treats) {
		this(id, ownerName, hamsterName, treats, HamsterLib.HMSTR_BASE_PRICE, new Date());
	}
	
	/**
	 * Creates DataStoreEntry object. Admission tie is set to now, default price is set, treats are set to 0
	 * @param id entry id
	 * @param ownerName owner name
	 * @param hamsterName hamster name
	 */
	public DataStoreEntry(int id, String ownerName, String hamsterName) {
		this(id, ownerName, hamsterName, (short)0, HamsterLib.HMSTR_BASE_PRICE, new Date());
	}
	
	/**
	 * Creates DataStoreEntry object
	 * @param id entry id
	 * @param ownerName owner name
	 * @param hamsterName hamster name
	 * @param treats treats
	 * @param price prive
	 * @param admissionTime time when hamster was admitted
	 */
	private DataStoreEntry(int id, String ownerName, String hamsterName, short treats, short price, Date admissionTime) {
		this.id = id;
		this.ownerName = ownerName;
		this.hamsterName = hamsterName;
		this.admissionTime = admissionTime;
		this.price = price;
		this.treats = treats;
	}
	
	/**
	 * creates a copy of given entry
	 * @param original entry to copy
	 */
	private DataStoreEntry(DataStoreEntry original) {
		this(original.id, original.ownerName, original.hamsterName, original.treats, original.price, original.admissionTime);
	}

	/**
	 * Compares to DataStoreEntry objects. Admission time is considered
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		DataStoreEntry other = (DataStoreEntry) obj;
		if (hamsterName == null) {
			if (other.hamsterName != null)
				return false;
		} else if (!hamsterName.equals(other.hamsterName))
			return false;
		if (id != other.id)
			return false;
		if (ownerName == null) {
			if (other.ownerName != null)
				return false;
		} else if (!ownerName.equals(other.ownerName))
			return false;
		if (price != other.price)
			return false;
		if (treats != other.treats)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "{id: "+id+" ,"+ownerName+", "+hamsterName+", admission: "+admissionTime.toString()+", "+price+" €, "+treats+" treats}";
	}
}