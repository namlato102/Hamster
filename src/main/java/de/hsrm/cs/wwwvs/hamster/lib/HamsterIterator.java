package de.hsrm.cs.wwwvs.hamster.lib;

import java.io.Serializable;
import java.util.Iterator;

public class HamsterIterator implements Iterator<DataStoreEntry>, Serializable {
	
	/**
	 * Generated serial version id
	 */
	private static final long serialVersionUID = 2339239149639120963L;
	static HamsterDataStore dataStore = HamsterDataStore.getInstance();
	int position = 0;
	
	public HamsterIterator() {
		this(0);
	}
	
	public HamsterIterator(int position) {
		this.position = position;
	}

	@Override
	public boolean hasNext() {
		return position < dataStore.size();
	}

	@Override
	public DataStoreEntry next() {
		DataStoreEntry entry = dataStore.get(position);
		position++;
		return entry;
	}

	public int getPosition() {
		return this.position;
	}

}
