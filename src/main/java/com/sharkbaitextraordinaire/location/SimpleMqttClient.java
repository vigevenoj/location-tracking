package com.sharkbaitextraordinaire.location;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;

/**
 * Hello world!
 *
 */
public class SimpleMqttClient implements MqttCallback {
	MqttClient client;
    MqttConnectOptions connOpts;
    
    private static Properties configProperties = new Properties();

	public void connectionLost(Throwable arg0) {
		System.out.println("Connection lost!");
	}

	public void deliveryComplete(IMqttDeliveryToken arg0) {
		// no-op because we don't publish
		
	}

	public void messageArrived(String topicString, MqttMessage message) throws Exception {
		System.out.println("-------------------------------------------------");
		System.out.println("| Message: " + new String(message.getPayload()));
		System.out.println("-------------------------------------------------");
	}
	
	public static void main(String[] args) {
		loadProperties(args[0]);
		SimpleMqttClient smc = new SimpleMqttClient();
		smc.runClient();
	}

	public void runClient() {

		String clientID = configProperties.getProperty("app.client.client_id");
		String brokerUrl = configProperties.getProperty("app.broker.url");
		
		connOpts = new MqttConnectOptions();
		connOpts.setKeepAliveInterval(30);
		connOpts.setUserName(configProperties.getProperty("app.client.username"));
		connOpts.setPassword(configProperties.getProperty("app.client.password").toCharArray());
		
		Properties sslProps = new Properties();
		sslProps.setProperty("com.ibm.ssl.protocol", configProperties.getProperty("com.ibm.ssl.protocol"));
		sslProps.setProperty("com.ibm.ssl.trustStore", configProperties.getProperty("com.ibm.ssl.trustStore"));
		sslProps.setProperty("com.ibm.ssl.trustStorePassword", configProperties.getProperty("com.ibm.ssl.trustStorePassword"));
		
		connOpts.setSSLProperties(sslProps);
		
		try {
			client = new MqttClient(brokerUrl, clientID);
			client.setCallback(this);
			client.connect(connOpts);
		} catch (MqttException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		System.out.println("Connected to " + brokerUrl);
		
		String myTopic = configProperties.getProperty("app.broker.topic");
		MqttTopic topic = client.getTopic(myTopic);
		System.out.println("Topic is " + topic.getName());
		
		if (true) {
			try {
				int subQoS = 0;
				client.subscribe(myTopic, subQoS);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void loadDefaultProperties() {
		String DEFAULT_PROPERTIES_FILE_NAME = "com/sharkbaitextraordinaire/location/default.properties";
		InputStream in = SimpleMqttClient.class.getClassLoader().getResourceAsStream(DEFAULT_PROPERTIES_FILE_NAME);
		try {
			configProperties.load(in);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void loadProperties(String path) {
		System.out.println("Using " + path + " as path");
		try {
			FileInputStream fis = new FileInputStream(new File(path));
			configProperties.load(fis);
		} catch (FileNotFoundException e) {
			System.err.println("Failed to load specified properties, loading default.properties");
			loadDefaultProperties();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
