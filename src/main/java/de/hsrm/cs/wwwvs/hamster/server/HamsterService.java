package de.hsrm.cs.wwwvs.hamster.server;

import de.hsrm.cs.wwwvs.hamster.lib.*;
import de.hsrm.cs.wwwvs.hamster.server.DTO.HamsterDTO;
import io.github.resilience4j.decorators.Decorators;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static de.hsrm.cs.wwwvs.hamster.server.HamsterController.*;

@Service
public class HamsterService {

    private final HamsterLib lib;

    public HamsterService() {
        this.lib = new HamsterLib();
    }

    public List<HamsterDTO> list(HamsterIterator iterator, String ownerName, String hamsterName)
            throws HamsterNameTooLongException, HamsterNotFoundException, HamsterEndOfDirectoryException {
        List<HamsterDTO> list = new ArrayList<>();
        try {
            while (iterator.hasNext()) {
                int ret = lib.directory(iterator, ownerName, hamsterName);

                HamsterLib.OutString owner = lib.new OutString();
                HamsterLib.OutString hamster = lib.new OutString();
                HamsterLib.OutShort price = lib.new OutShort();

                int treats = lib.readentry(ret, owner, hamster, price);

                HamsterDTO hamsterDTO = new HamsterDTO();
                hamsterDTO.setName(hamster.getValue());
                hamsterDTO.setOwner(owner.getValue());
                hamsterDTO.setTreats(treats);
                hamsterDTO.setPrice((int) price.getValue());

                list.add(hamsterDTO);
            }
        } catch (HamsterNameTooLongException e) {
            throw new HamsterNameTooLongException(e.getMessage());
        } catch (HamsterNotFoundException e) {
            throw new HamsterNotFoundException(e.getMessage());
        } catch (HamsterEndOfDirectoryException ignored) {
        }

        return list;
    }

    public int lookup(String ownerName, String hamsterName)
            throws HamsterNameTooLongException, HamsterNotFoundException {
        try {
            return lib.lookup(ownerName, hamsterName);
        } catch (HamsterNameTooLongException e) {
            throw new HamsterNameTooLongException(e.getMessage());
        } catch (HamsterNotFoundException e) {
            throw new HamsterNotFoundException(e.getMessage());
        }
    }

    public int add(String ownerName, String hamsterName, int treats)
            throws HamsterNameTooLongException, HamsterAlreadyExistsException, HamsterStorageException, HamsterDatabaseCorruptException {
        try {
            return lib.new_(ownerName, hamsterName, (short) treats);
        } catch (HamsterNameTooLongException e) {
            throw new HamsterNameTooLongException(e.getMessage());
        } catch (HamsterAlreadyExistsException e) {
            throw new HamsterAlreadyExistsException(e.getMessage());
        } catch (HamsterStorageException e) {
            throw new HamsterStorageException(e.getMessage());
        } catch (HamsterDatabaseCorruptException e) {
            throw new HamsterDatabaseCorruptException(e.getMessage());
        }
    }

    public int state(int id, HamsterState state)
            throws HamsterNotFoundException, HamsterStorageException {
        try {
            return lib.howsdoing(id, state);
        } catch (HamsterNotFoundException e) {
            throw new HamsterNotFoundException(e.getMessage());
        } catch (HamsterStorageException e) {
            throw new HamsterStorageException(e.getMessage());
        }
    }

    public int collect(String ownerName)
            throws HamsterStorageException, HamsterNotFoundException, HamsterNameTooLongException {
        try {
            return lib.collect(ownerName);
        } catch (HamsterNameTooLongException e) {
            throw new HamsterNameTooLongException(e.getMessage());
        } catch (HamsterNotFoundException e) {
            throw new HamsterNotFoundException(e.getMessage());
        } catch (HamsterStorageException e) {
            throw new HamsterStorageException(e.getMessage());
        }
    }

    public short feed(String ownerName, String hamsterName, short treats)
            throws HamsterRefusedTreatException, HamsterNameTooLongException, HamsterNotFoundException, HamsterStorageException {
        try {
            Callable<Short> feed = () -> lib.givetreats(lib.lookup(ownerName, hamsterName), treats);
            var retry = retries.retry(hamsterName + " from " + ownerName, retryConfig);
            var circuit = circuitBreakers.circuitBreaker(hamsterName + " from " + ownerName, circuitBreakerConfig);

            var handleFeed = Decorators.ofCallable(feed)
                    .withRetry(retry)
                    .withCircuitBreaker(circuit)
                    .decorate();
            var ret = handleFeed.call();
            return ret;

        } catch (HamsterNameTooLongException e) {
            throw new HamsterNameTooLongException(e.getMessage());
        } catch (HamsterNotFoundException e) {
            throw new HamsterNotFoundException(e.getMessage());
        } catch (HamsterStorageException e) {
            throw new HamsterStorageException(e.getMessage());
        } catch (HamsterRefusedTreatException e) {
            throw new HamsterRefusedTreatException(e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
