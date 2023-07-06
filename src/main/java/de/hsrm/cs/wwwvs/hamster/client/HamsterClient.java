package de.hsrm.cs.wwwvs.hamster.client;

import de.hsrm.cs.wwwvs.hamster.server.DTO.HamsterDTO;
import de.hsrm.cs.wwwvs.hamster.server.DTO.StateDTO;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.text.DecimalFormat;
import java.util.*;

public class HamsterClient {
    private RestTemplate restTemplate;
    private String hostname;
    private int port;

    private String baseUrl;

    public HamsterClient(String hostName, int port) {
        this.restTemplate = new RestTemplate();
        this.hostname = hostName;
        this.port = port;
        this.baseUrl = String.format("http://%s:%d", this.hostname, this.port);
    }

    public boolean list(String ownerName, String hamsterName) throws Exception {
        String url = "";
        // URL
        if (ownerName == null && hamsterName == null)
            url = baseUrl + "/hamster";
        if (ownerName != null && hamsterName != null)
            url = baseUrl + "/hamster/" + ownerName + "?name=" + hamsterName;
        if (ownerName != null && hamsterName == null)
            url = baseUrl + "/hamster/" + ownerName;
        if (ownerName == null && hamsterName != null)
            url = baseUrl + "/hamster?name=" + hamsterName;

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<HamsterDTO[]> responseEntity = new RestTemplate().exchange(url, HttpMethod.GET, requestEntity, HamsterDTO[].class, ownerName, hamsterName);

        // TODO: print hamster table, return whether list was successful
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            HamsterDTO[] hamsters = responseEntity.getBody();
            if (hamsters != null && hamsters.length > 0) {
                System.out.printf("%s\t%s\t%s\t%s\n", "Owner", "Name", "Price", "Treats");
                for (HamsterDTO hamster : hamsters) {
                    System.out.printf("%s\t%s\t%s\t%d\n",
                            hamster.getOwner(), hamster.getName(), hamster.getPrice() + " \u20AC", hamster.getTreats());
                }
                return true;
            } else {
                System.out.println("No hamster found.");
            }
        } else {
            System.out.println("Failed to retrieve hamsters.");
        }

        return false;

    }

    public int add(String owner, String hamster, short treats) throws Exception {
        // TODO: add hamster, print ID to stdout (or any integer in case it does not matter), only the number
        String url = baseUrl + "/hamster";
        HamsterDTO newHamster = new HamsterDTO();
        newHamster.setOwner(owner);
        newHamster.setName(hamster);
        newHamster.setTreats((int) treats);
        newHamster.setPrice(17);
        HttpEntity<HamsterDTO> requestEntity = new HttpEntity<>(newHamster);

        ResponseEntity<Map<String,String>> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, new ParameterizedTypeReference<>() {
        });

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            System.out.println(responseEntity.getBody().toString());
        } else {
            System.out.println("Failed to add hamster.");
        }

        return 0;
    }

    public int feed(String owner, String hamster, short treats) throws Exception {
        // TODO: feed hamster, print remaining treats to stdout (only the number)
        Integer feedRequest = (int) treats;
        String url = baseUrl + "/hamster/" + owner + "/" + hamster + "?treats= " + feedRequest;

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<Map<String,Integer>> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, new ParameterizedTypeReference<>() {});

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            Integer remainingTreats = responseEntity.getBody().get("treats");
            System.out.println("Hamster fed. Remaining treats: " + remainingTreats);
            return remainingTreats;
        } else {
            System.out.println("Failed to feed hamster.");
            return -1;
        }
    }

    public void state(String owner, String hamster) throws Exception {
        // TODO: query hamster state, print result to stdout in the format
        String url = baseUrl + "/hamster/" + owner + "/" + hamster;
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<StateDTO> responseEntity = restTemplate.exchange(url, HttpMethod.GET, requestEntity, StateDTO.class);

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            StateDTO state = responseEntity.getBody();
            System.out.printf("%s's hamster %s has done %d hamster wheel turns and has %d treats left in store. Current price is %s%n",
                    owner, hamster, state.getTurns(), state.getTreats(), state.getCost() + " \u20AC");
        } else {
            System.out.println("Failed to retrieve hamster state.");
        }
    }

    public int bill(String owner) throws Exception {
        // TODO: collect hamsters from given owner, print amount to pay to stdout (only the number)
        String url = baseUrl + "/hamster/" + owner;
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<Map<String,Integer>> responseEntity = restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, new ParameterizedTypeReference<>() {});

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            Integer amountToPay = responseEntity.getBody().get("price");
            System.out.println("Billed. Amount to pay: " + amountToPay + " \u20AC");
            return amountToPay;
        } else {
            System.out.println("Failed to retrieve bill.");
            return 0;
        }
    }

}
