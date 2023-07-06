package de.hsrm.cs.wwwvs.hamster.server;

import de.hsrm.cs.wwwvs.hamster.lib.*;
import de.hsrm.cs.wwwvs.hamster.server.DTO.HamsterDTO;
import de.hsrm.cs.wwwvs.hamster.server.DTO.StateDTO;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.registry.EntryAddedEvent;
import io.github.resilience4j.core.registry.EntryRemovedEvent;
import io.github.resilience4j.core.registry.EntryReplacedEvent;
import io.github.resilience4j.core.registry.RegistryEventConsumer;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType.COUNT_BASED;

@RestController
public class HamsterController {

    @Autowired
    private HamsterService hamsterService;

    @Autowired
    private Environment env;

    // TODO: Ciruit breaker config, Retry config
    public static CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
            .recordExceptions(HamsterRefusedTreatException.class)
            .failureRateThreshold(50)
            .waitDurationInOpenState(Duration.ofSeconds(30))
            .enableAutomaticTransitionFromOpenToHalfOpen()
            .slidingWindow(1, 1, COUNT_BASED)
            .build();

    public static RetryConfig retryConfig = RetryConfig.custom()
            .retryExceptions(HamsterRefusedTreatException.class)
            .maxAttempts(3)
            .waitDuration(Duration.ofMillis(100))
            .build();

    private static Logger logger = LoggerFactory.getLogger(HamsterController.class);

    public static CircuitBreakerRegistry circuitBreakers = CircuitBreakerRegistry
            .custom()
            .addRegistryEventConsumer(new RegistryEventConsumer<CircuitBreaker>() {
                @Override
                public void onEntryAddedEvent(EntryAddedEvent<CircuitBreaker> entryAddedEvent) {
                    var publisher = entryAddedEvent.getAddedEntry().getEventPublisher();
                    var name = entryAddedEvent.getAddedEntry().getName();

                    publisher.onCallNotPermitted(event -> {
                        logger.warn("Attempted to feed sick hamster {}", name);
                    });
                    publisher.onError(event -> {
                        logger.warn("Hamster {} has refused treats even after retry, we need a veterinarian.", name);
                    });
                    publisher.onStateTransition(event -> {
                        switch (event.getStateTransition().getToState()) {
                            case CLOSED: {
                                logger.warn("Hamster {} has recovered, hurray!", name);
                                break;
                            }
                            case OPEN: {
                                logger.warn("Hamster {} has refused treats even after retry, we need a veterinarian.", name);
                                break;
                            }
                            case HALF_OPEN: {
                                logger.warn("Hamster {} had a rest for a while, maybe we can try another treat", name);
                                break;
                            }
                        }
                    });
                }

                @Override
                public void onEntryRemovedEvent(EntryRemovedEvent<CircuitBreaker> entryRemoveEvent) {
                }

                @Override
                public void onEntryReplacedEvent(EntryReplacedEvent<CircuitBreaker> entryReplacedEvent) {
                }
            })
            .build();

    public static RetryRegistry retries = RetryRegistry
            .custom()
            .addRegistryEventConsumer(new RegistryEventConsumer<Retry>() {
                @Override
                public void onEntryAddedEvent(EntryAddedEvent<Retry> entryAddedEvent) {
                    var publisher = entryAddedEvent.getAddedEntry().getEventPublisher();

                    publisher.onRetry(event -> {
                        logger.warn("Hamster {} refused treat, trying again", entryAddedEvent.getAddedEntry().getName());
                    });
                }

                @Override
                public void onEntryRemovedEvent(EntryRemovedEvent<Retry> entryRemoveEvent) {
                }

                @Override
                public void onEntryReplacedEvent(EntryReplacedEvent<Retry> entryReplacedEvent) {
                }
            })
            .build();

    @GetMapping("/hamster")
    public List<HamsterDTO> list(
            @RequestParam(required = false) String name
    ) throws HamsterNameTooLongException, HamsterNotFoundException, HamsterEndOfDirectoryException {
        HamsterIterator iterator = new HamsterIterator();

        return hamsterService.list(iterator, null, name);
    }
    @GetMapping("/hamster/{owner}")
    public List<HamsterDTO> listOwner(
            @PathVariable String owner,
            @RequestParam(required = false) String name
    ) throws HamsterNameTooLongException, HamsterNotFoundException, HamsterEndOfDirectoryException {
        HamsterIterator iterator = new HamsterIterator();

        return hamsterService.list(iterator, owner, name);
    }
    @PostMapping("/hamster")
    public @ResponseBody Map<String, String> add(
            @RequestBody HamsterDTO hamsterDTO
    ) throws HamsterNameTooLongException, HamsterStorageException, HamsterAlreadyExistsException, HamsterDatabaseCorruptException {
        HashMap<String, String> json = new HashMap<>();

        int ret = hamsterService.add(hamsterDTO.getOwner(), hamsterDTO.getName(), hamsterDTO.getTreats());

        json.put("state", "http://" + env.getProperty("server.address") + ":" + env.getProperty("server.port") + "/hamster/" + hamsterDTO.getOwner() + "/" + hamsterDTO.getName());
        return json;
    }
    @PostMapping("/hamster/{owner}/{name}")
    public @ResponseBody Map<String, Integer> feed(
            @PathVariable String owner,
            @PathVariable String name,
            @RequestParam Integer treats
    ) throws HamsterNameTooLongException, HamsterNotFoundException, HamsterStorageException, HamsterRefusedTreatException {
        HashMap<String, Integer> json = new HashMap<>();

        int ret = hamsterService.feed(owner, name, treats.shortValue());

        json.put("treats", ret);
        return json;
    }
    @GetMapping("/hamster/{owner}/{name}")
    public StateDTO howsdoing(
            @PathVariable String owner,
            @PathVariable String name
    ) throws HamsterNameTooLongException, HamsterNotFoundException, HamsterStorageException {
        HamsterState state = new HamsterState();
        int ret = hamsterService.state(hamsterService.lookup(owner, name), state); // return code

        StateDTO stateDTO = new StateDTO();

        stateDTO.setName(name);
        stateDTO.setOwner(owner);
        stateDTO.setTreats(state.getTreatsLeft());
        stateDTO.setTurns(state.getRounds());
        stateDTO.setCost(state.getCost());

        return (ret == 0) ? stateDTO : new StateDTO();
    }
    @DeleteMapping("/hamster/{owner}")
    public Map<String, Integer> collect(
            @PathVariable String owner
    ) throws HamsterNameTooLongException, HamsterNotFoundException, HamsterStorageException {
        HashMap<String, Integer> json = new HashMap<>();

        int ret = hamsterService.collect(owner);

        json.put("price", ret);
        return json;
    }
}
