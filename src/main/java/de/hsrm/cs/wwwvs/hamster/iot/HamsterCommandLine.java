package de.hsrm.cs.wwwvs.hamster.iot;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class HamsterCommandLine {

    private static int printRtfm() {
        System.out.println("Usage: hamster_mqtt.jar <hamster_client_id> {<Options>}");
        System.out.println("Function: Hamster instrumentation device software");
        System.out.println("Options:");
        System.out.println("     -t / --target {<IP address>}   - IP address to run the server on (default: 127.0.0.1");
        System.out.println("     -e / --encrypted               - Connect with Certificate based SSL/TLS Support to the MQTT server");
        System.out.println("     -c / --client-certificate      - Connect with Certificate based client authentification to the MQTT server");
        System.out.println("     -s / --silent                  - supress usage information");
        System.out.println("     -h / --help                    - This help ");

        return 2;
    }

    public static void main(String[] args) {
        String hamsterId = null;
        String target = "127.0.0.1";
        boolean encryptConnection = false;
        boolean authenticateWithCertificate = false;
        boolean printUsage = true;

        if (args.length < 1) {
            System.exit(printRtfm());
        }
        hamsterId = args[0];

        int currentArg = 1;
        while (currentArg < args.length) {
            switch (args[currentArg]) {
                case "-t":
                case "--target":
                    if (args.length < currentArg + 1) {
                        System.exit(printRtfm());
                    }
                    target = args[currentArg + 1];
                    currentArg += 2;
                    break;
                case "-e":
                case "--encrypted":
                    encryptConnection = true;
                    currentArg += 1;
                    break;
                case "-c":
                case "--client-certificate":
                    authenticateWithCertificate = true;
                    encryptConnection = true;
                    currentArg += 1;
                    break;
                case "-s":
                case "--silent":
                    printUsage = false;
                    currentArg += 1;
                    break;
                case "-h":
                case "--help":
                    printRtfm();
                    System.exit(0);
                default:
                    System.exit(printRtfm());
            }
        }

        try {
            var hamster = new SimulatedHamster(hamsterId);
            var client = new HamsterMqttClient(hamster);
            client.connect(target, encryptConnection, authenticateWithCertificate);
            var bufferedReader = new BufferedReader(new InputStreamReader(System.in));

            if (printUsage) {
                printTerminalUsage();
            }

            boolean shouldQuit = false;

            while (!shouldQuit) {
                System.out.print("> ");
                var line = bufferedReader.readLine();
                switch (line.trim()) {
                    case "quit":
                        System.out.println("Goodbye");
                        shouldQuit = true;
                        break;
                    case "eat":
                        client.eat();
                        break;
                    case "run":
                        client.run();
                        break;
                    case "sleep":
                        client.sleep();
                        break;
                    case "mate":
                        client.mate();
                        break;
                    case "move A":
                        client.move("A");
                        break;
                    case "move B":
                        client.move("B");
                        break;
                    case "move C":
                        client.move("C");
                        break;
                    case "move D":
                        client.move("D");
                        break;
                    case "help":
                        printTerminalUsage();
                        break;
                }
            }

            client.disconnect();
        }
        catch (Exception ex) {
            ex.printStackTrace();
            System.err.println(ex.getMessage());
            System.exit(1);
        }
    }

    private static void printTerminalUsage() {
        System.out.println("Usage:");
        System.out.println("  - eat:    sets the hamster into eating state");
        System.out.println("  - mate:   sets the hamster into mateing state");
        System.out.println("  - sleep:  sets the hamster into sleeping state");
        System.out.println("  - run:    sets the hamster into running state");
        System.out.println("  - move:   moves the hamster to the given position");
    }
}
