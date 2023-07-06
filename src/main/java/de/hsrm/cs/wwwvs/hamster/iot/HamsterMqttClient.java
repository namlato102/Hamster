package de.hsrm.cs.wwwvs.hamster.iot;

import org.eclipse.paho.client.mqttv3.*;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Properties;


public class HamsterMqttClient {

    private SimulatedHamster _hamster;
    private MqttClient _mqttClient;

    public HamsterMqttClient(SimulatedHamster hamster) {
        _hamster = hamster;
    }

    public void connect(String host, boolean encryptedConnection, boolean authenticateClient) throws Exception {
        // Create MQTT connection options
        MqttConnectOptions options = new MqttConnectOptions();

        // Authenticate the client if enabled
        if (authenticateClient) {
            SSLSocketFactory socketFactory = TlsUtil.getSocketFactory("./certs/ca.crt", "./certs/client.crt", "./certs/client.key", null);
            options.setSocketFactory(socketFactory);
            options.setUserName("hamster");

            // Create MQTT client with client ID
            _mqttClient = new MqttClient("ssl://" + host + ":8883", "ddang001", null);
        }

        else
        // Set SSL/TLS connection if enabled
            if (encryptedConnection) {
                SSLSocketFactory socketFactory = TlsUtil.getSocketFactory("./certs/ca.crt", null, null, null);
                options.setSocketFactory(socketFactory);
                options.setHttpsHostnameVerificationEnabled(false); // Disable hostname verification

                // Create MQTT client with client ID
                _mqttClient = new MqttClient("ssl://" + host + ":8883", _hamster.getHamsterId(), null);
            }
            else
                _mqttClient = new MqttClient("tcp://" + host + ":1883", _hamster.getHamsterId(), null);


        _mqttClient.connect(options);

        this.publishHamster("/pension/livestock", 2, true);

        // Subscribe to fondle/punish topics
        this._mqttClient.subscribe("/pension/hamster/" + _hamster.getHamsterId() + "/punish", 0, (topic, message) -> {
            byte[] payload = message.getPayload();
            int value;
            if (payload.length == 4) {
                value=ByteBuffer.wrap(payload).getInt();
            } else {
                value=Integer.parseInt(new String(payload, StandardCharsets.UTF_8));
            }
            _hamster.punish(value);
        });

        this._mqttClient.subscribe("/pension/hamster/" + _hamster.getHamsterId() + "/fondle", 0, (topic, message) -> {
            byte[] payload = message.getPayload();
            int value;
            if (payload.length == 4) {
                value=ByteBuffer.wrap(payload).getInt();
            } else {
                value=Integer.parseInt(new String(payload, StandardCharsets.UTF_8));
            }
            _hamster.fondle(value);
        });

        // Set callback for wheels publishing
        this._hamster.setRevolutionCallback( (rounds) -> {
            try {
                _mqttClient.publish("/pension/hamster/" + _hamster.getHamsterId() + "/wheels", new MqttMessage(String.valueOf(rounds).getBytes()));
                // benutzt publishMessage(String topic, String message, Integer QoS, boolean retention)
            } catch (MqttException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void eat() {
        _hamster.stopRunning();
        // Prepare the message indicating that the hamster is eating
        String message = "EATING";
        // Publish the message to the "hamster/eating" topic
        publishMessage("/pension/hamster/" + _hamster.getHamsterId() + "/state", message, 1, false);
    }

    public void mate() {
        _hamster.stopRunning();
        // Prepare the message indicating that the hamster is mating
        String message = "MATEING";
        // Publish the message to the "hamster/mating" topic
        publishMessage("/pension/hamster/" + _hamster.getHamsterId() + "/state", message, 1, false);
    }

    public void sleep() {
        _hamster.stopRunning();
        // Prepare the message indicating that the hamster is sleeping
        String message = "SLEEPING";
        // Publish the message to the "hamster/sleeping" topic
        publishMessage("/pension/hamster/" + _hamster.getHamsterId() + "/state", message, 1, false);
    }

    public void run() {
        // Prepare the message indicating that the hamster is running
        String message = "RUNNING";
        _hamster.startRunning();
        // Publish the message to the "hamster/running" topic
        publishMessage("/pension/hamster/" + _hamster.getHamsterId() + "/state", message, 1, false);
    }

    public void move(String position) {
        // Prepare the message indicating that the hamster is moving to a specific position
        String message = position;
        // Publish the message to the "hamster/moving" topic
        publishMessage("/pension/hamster/" + _hamster.getHamsterId() + "/position", message, 1, false);
        publishHamster("/pension/room/" + position, 0, false);
    }

    public void disconnect() throws MqttException {
        if (_mqttClient != null && _mqttClient.isConnected()) {
            _mqttClient.disconnect();
        }
    }

    private void publishMessage(String topic, String message, Integer QoS, boolean retention) {
        try {
            // Convert the message to bytes using UTF-8 encoding and publish it to the specified topic
            _mqttClient.publish(topic, message.getBytes(StandardCharsets.UTF_8), QoS, retention);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void publishHamster(String topic, Integer QoS, boolean retention) {
        try {
            _mqttClient.publish(topic, String.valueOf(_hamster.getHamsterId()).getBytes(), QoS, retention);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
