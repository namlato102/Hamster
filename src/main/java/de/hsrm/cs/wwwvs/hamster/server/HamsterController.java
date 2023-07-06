package de.hsrm.cs.wwwvs.hamster.server;

import de.hsrm.cs.wwwvs.hamster.lib.*;
import org.apache.catalina.core.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
public class HamsterController {

    private final static HamsterLib lib = new HamsterLib();
    private static String preferred_username = "";

    @GetMapping("/hamster")
    public List<Hamster> getAllHamsters(@RequestParam(value = "name", required = false) String name, @AuthenticationPrincipal Jwt jwt) throws Exception {
        List<Hamster> list = new ArrayList<>();
        HamsterIterator iterator = new HamsterIterator();
        preferred_username = jwt.getClaim("preferred_username").toString();

        if (Objects.equals(name, ""))
            name = null;

        try {
            while (iterator.hasNext()) {
                int ret = lib.directory(iterator, preferred_username, name);

                HamsterLib.OutString ownerName = lib.new OutString();
                HamsterLib.OutString hamsterName = lib.new OutString();
                HamsterLib.OutShort price = lib.new OutShort();

                int treats = lib.readentry(ret, ownerName, hamsterName, price);

                Hamster hamster = new Hamster(ownerName.getValue(), hamsterName.getValue(), treats, price.getValue());

                list.add(hamster);
            }
        } catch (HamsterNameTooLongException | HamsterNotFoundException e) {
            System.out.println(e.getMessage());
        } catch (HamsterEndOfDirectoryException ignored) {
        }

        return list;
    }

    @PostMapping("/hamster")
    public ResponseEntity<String> addHamster(@RequestBody HamsterAddRequest request, @AuthenticationPrincipal Jwt jwt) throws Exception {
        preferred_username = jwt.getClaim("preferred_username").toString();

        try {
            lib.new_(preferred_username, request.name(), (short) request.treats());
        } catch (HamsterNameTooLongException | HamsterDatabaseCorruptException | HamsterStorageException |
                 HamsterAlreadyExistsException e) {
            System.out.println(e.getMessage());
        }
        System.out.println("Added Hamster");
        return ResponseEntity.created(new URI("http://localhost:4200/hamster")).build();
    }

    @PostMapping("/hamster/{name}")
    public ResponseEntity<TreatsInfo> feed(@PathVariable String name, @RequestBody TreatsInfo treats, @AuthenticationPrincipal Jwt jwt) throws Exception {
        short treatsLeft = -1;
        preferred_username = jwt.getClaim("preferred_username").toString();

        try {
            treatsLeft = lib.givetreats(lib.lookup(preferred_username, name), treats.treats());
        } catch (HamsterNameTooLongException | HamsterNotFoundException | HamsterRefusedTreatException |
                 HamsterStorageException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println("Unknown error");
            System.out.println(e.getMessage());
        }
        System.out.println("Fed Hamster");
        return ResponseEntity.ok(new TreatsInfo(treatsLeft));
    }

    @DeleteMapping("/hamster")
    public PriceInfo collect(@AuthenticationPrincipal Jwt jwt) throws Exception {
        short price = -1;
        preferred_username = jwt.getClaim("preferred_username").toString();

        try {
            price = lib.collect(preferred_username);
        } catch (HamsterNameTooLongException | HamsterNotFoundException | HamsterStorageException e) {
            System.out.println(e.getMessage());
        }
        System.out.println("Collected Hamsters");
        return new PriceInfo(price);
    }

    public record Hamster(String owner, String name, int treatsLeft, int price) {}

    public record HamsterAddRequest(String name, String owner, short treats) {}

    public record TreatsInfo(short treats) {}

    public record PriceInfo(int price) {}
}
