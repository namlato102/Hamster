package de.hsrm.cs.wwwvs.hamster.console;

import de.hsrm.cs.wwwvs.hamster.lib.*;


public class HamsterManagement {
    /**
     * HMSTR_MAX_NAME = 31, therefore spacing is hardcoded (32)
     */
    public void addHamster(HamsterLib hamsterLib, String ownerName, String hamsterName, short treats) throws HamsterNameTooLongException, HamsterStorageException, HamsterAlreadyExistsException, HamsterDatabaseCorruptException {
        hamsterLib.new_(ownerName, hamsterName, treats);
        System.out.println("Hamster " + hamsterName + " owned by " + ownerName + " was added ");
    }

    public void listEverything(HamsterLib hamsterLib)
    {
        HamsterIterator iterator = hamsterLib.iterator();
        if (iterator.hasNext())
            System.out.printf("%-32s %-32s %-10s %-10s%n", "Owner", "Name", "Price", "Treats");
        else
            System.out.println("No hamster found.");
        while(iterator.hasNext())
        {
            DataStoreEntry dataStoreEntry = iterator.next();
            System.out.printf("%-32s %-32s %-10s %-10d%n", dataStoreEntry.ownerName, dataStoreEntry.hamsterName, String.valueOf(dataStoreEntry.price) + " €", dataStoreEntry.treats);
        }
    }
    public void listHamster(HamsterLib hamsterLib, String ownerName) throws HamsterNameTooLongException, HamsterNotFoundException {
        if (ownerName.length() > HamsterLib.HMSTR_MAX_NAME)
            throw new HamsterNameTooLongException();

        HamsterIterator iterator = hamsterLib.iterator();
        if (!iterator.hasNext())
            System.out.println("No hamster found.");

        int index = -1; // index for finding hamster owner

        while(iterator.hasNext())
        {
            DataStoreEntry dataStoreEntry = iterator.next();
            if (dataStoreEntry.ownerName.equals(ownerName)) {
                System.out.printf("%-32s %-32s %-10s %-10d%n", dataStoreEntry.ownerName, dataStoreEntry.hamsterName, String.valueOf(dataStoreEntry.price) + " €", dataStoreEntry.treats);
                index = hamsterLib.lookup(dataStoreEntry.ownerName, dataStoreEntry.hamsterName);
            }
        }

        if (index == -1)
            System.out.println("No hamster matching criteria (owner) found");
    }
    public void feed(HamsterLib hamsterLib, String ownerName, String hamsterName, short treats) throws HamsterNotFoundException, HamsterStorageException, HamsterNameTooLongException {
        System.out.printf("Feeding %s%n ", hamsterName);
        int id = hamsterLib.lookup(ownerName, hamsterName);

        hamsterLib.givetreats(id, treats);
        HamsterLib.OutString owner = hamsterLib.new OutString();
        HamsterLib.OutString hamster = hamsterLib.new OutString();
        HamsterLib.OutShort price = hamsterLib.new OutShort();

        owner.setValue(ownerName);
        hamster.setValue(hamsterName);
        price.setValue((short) 0);

        System.out.println("Treats left: " + hamsterLib.readentry(id, owner, hamster, price));
    }
    public void checkState(HamsterLib hamsterLib, String ownerName, String hamsterName) throws HamsterNameTooLongException, HamsterNotFoundException, HamsterStorageException {
        System.out.println("Checking state ...");
        int id = hamsterLib.lookup(ownerName, hamsterName);
        HamsterState state = new HamsterState();

        if (hamsterLib.howsdoing(id, state) == 0) {
            System.out.printf("%-32s %-32s %-10s %-10s %-10s%n", "Owner", "Name", "Wheel runs", "Price", "Treats");
            System.out.printf("%-32s %-32s %-10s %-10s %-10s%n", ownerName, hamsterName, state.getRounds(), state.getCost() + " €", state.getTreatsLeft());
        }
        else
            System.out.println("Error when looking up state");
    }

    public void bill(HamsterLib hamsterLib, String ownerName) throws HamsterNameTooLongException, HamsterNotFoundException, HamsterStorageException {
        System.out.printf("Collected the hamsters. %s has to pay: %s%n", ownerName, hamsterLib.collect(ownerName) +" €");
    }
}
