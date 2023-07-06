package de.hsrm.cs.wwwvs.hamster.server;

import de.hsrm.cs.wwwvs.hamster.lib.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static org.springframework.http.HttpStatus.*;

@ControllerAdvice
public class ExceptionHandlerConfig {

    @ExceptionHandler({HamsterException.class})
    public ResponseEntity<String> handleHamsterException(HamsterException e) {
        return new ResponseEntity<>(e.getMessage(), BAD_REQUEST);
    }

    @ExceptionHandler({HamsterAlreadyExistsException.class})
    public ResponseEntity<String> handleHamsterAlreadyExistsException(HamsterAlreadyExistsException e) {
        return new ResponseEntity<>(e.getMessage(), BAD_REQUEST);
    }

    @ExceptionHandler({HamsterDatabaseCorruptException.class})
    public ResponseEntity<String> handleHamsterDatabaseCorruptException(HamsterDatabaseCorruptException e) {
        return new ResponseEntity<>(e.getMessage(), INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({HamsterNameTooLongException.class})
    public ResponseEntity<String> handleHamsterNameTooLongException(HamsterNameTooLongException e) {
        return new ResponseEntity<>(e.getMessage(), BAD_REQUEST);
    }

    @ExceptionHandler({HamsterNotFoundException.class})
    public ResponseEntity<String> handleHamsterNotFoundException(HamsterNotFoundException e) {
        return new ResponseEntity<>(e.getMessage(), NOT_FOUND);
    }

    @ExceptionHandler({HamsterRefusedTreatException.class})
    public ResponseEntity<String> handleHamsterRefusedTreatException(HamsterRefusedTreatException e) {
        return new ResponseEntity<>(e.getMessage(), METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler({HamsterStorageException.class})
    public ResponseEntity<String> handleHamsterStorageException(HamsterStorageException e) {
        return new ResponseEntity<>(e.getMessage(), INSUFFICIENT_STORAGE);
    }
}
