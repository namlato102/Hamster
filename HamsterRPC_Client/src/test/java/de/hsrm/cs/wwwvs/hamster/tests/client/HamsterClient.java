package de.hsrm.cs.wwwvs.hamster.tests.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class HamsterClient {
    private int port;
    private static String pathToHamsterClient = "java -jar ../build/libs/hamster_client.jar";

    public static String getPathToHamsterClient() { return pathToHamsterClient; }

    public static void setPathToHamsterClient(String path) {
        pathToHamsterClient = path;
    }

    public HamsterClient(int port) {
        this.port = port;
    }

    private List<String> runHamsterAndGetResponse(String...args) throws HamsterClientException {
        try {
            var process = Runtime.getRuntime().exec(pathToHamsterClient + " " + String.join(" ", args) + " -p " + port);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            var lines = new ArrayList<String>();
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            try {
                var exitCode = process.waitFor();
                if (exitCode != 0) {
                    throw new HamsterClientException(lines.size() > 0 ? lines.get(0) : "no error message provided");
                }
            } catch (InterruptedException ex) {
                throw new HamsterClientException(ex.getMessage());
            }
            return lines;
        }
        catch(IOException ex) {
            throw new HamsterClientException(ex.getMessage());
        }
    }

    public int collect(String ownerName) throws HamsterClientException {
        var response = runHamsterAndGetResponse("bill", ownerName);
        return Integer.parseInt(response.get(0));
    }

    public int new_(String ownerName, String hamsterName, short treats) throws HamsterClientException {
        var response = runHamsterAndGetResponse("add", ownerName, hamsterName, Short.toString(treats));
        return Integer.parseInt(response.get(0));
    }

    public int giveTreats(String ownerName, String hamsterName, int treats) throws HamsterClientException {
        var response = runHamsterAndGetResponse("feed", ownerName, hamsterName, Integer.toString(treats));
        return Integer.parseInt(response.get(0));
    }

    public List<String> search(String ownerName, String hamsterName) throws HamsterClientException {
        var args = new ArrayList<String>();
        args.add("list");
        if (ownerName != null && ownerName.length() > 0) {
            args.add("--owner");
            args.add(ownerName);
        }
        if (hamsterName != null && hamsterName.length() > 0) {
            args.add("--hamster");
            args.add(hamsterName);
        }
        String[] argsArray = new String[args.size()];
        return runHamsterAndGetResponse(args.toArray(argsArray));
    }

    public String howsDoing(String ownerName, String hamsterName) throws HamsterClientException {
        return runHamsterAndGetResponse("state", ownerName, hamsterName).get(0);
    }
}
