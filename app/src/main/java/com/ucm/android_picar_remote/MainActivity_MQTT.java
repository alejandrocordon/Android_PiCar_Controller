package com.ucm.android_picar_remote;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MainActivity_MQTT extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private MqttAndroidClient mqttAndroidClient;

    //final String serverUri = "tcp://iot.eclipse.org:1883";
    final String serverUri = "tcp://hairdresser.cloudmqtt.com:18849";

    String clientId = "AndroidAPP";
    String user = "ibnyofaw";
    String passowrd = "UDbgKs77-wUN";
    final String subscriptionTopic = "masteriot";
    final String publishTopic = "masteriot";
    final String publishMessage = "Hello World!";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_mqtt);

        clientId = clientId + System.currentTimeMillis();

        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), serverUri, clientId);
            mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

                if (reconnect) {
                    Log.w(TAG,"Reconnected to : " + serverURI);
                    // Because Clean Session is true, we need to re-subscribe
                    subscribeToTopic();
                } else {
                    Log.w(TAG,"Connected to: " + serverURI);
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                Log.w(TAG,"The Connection was lost.");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.w(TAG,"Incoming message: " + new String(message.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }



        });

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setUserName(user);
            mqttConnectOptions.setPassword(passowrd.toCharArray());
            mqttConnectOptions.setAutomaticReconnect(true);
            mqttConnectOptions.setCleanSession(false);

            try {
            //addToHistory("Connecting to " + serverUri);
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.w(TAG,"CONNECT onSuccess: " + serverUri);
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    subscribeToTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w(TAG,"Failed to connect to: " + serverUri + " " + exception.getLocalizedMessage());
                }
            });

        } catch (MqttException ex){
            ex.printStackTrace();
        }


        Button boton = findViewById(R.id.button);
            boton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    publishMessage();
                }
            });

    }

    public void subscribeToTopic(){
        try {
            mqttAndroidClient.subscribe(subscriptionTopic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.w(TAG,"Subscribed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w(TAG,"Failed to subscribe");
                }
            });

            // THIS DOES NOT WORK!
            mqttAndroidClient.subscribe(subscriptionTopic, 0, new IMqttMessageListener() {
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    // message Arrived!
                    System.out.println("Message: " + topic + " : " + new String(message.getPayload()));
                }
            });

        } catch (MqttException ex){
            System.err.println("Exception whilst subscribing");
            ex.printStackTrace();
        }
    }

    public void publishMessage(){

        try {
            MqttMessage message = new MqttMessage();
            message.setPayload(publishMessage.getBytes());
            mqttAndroidClient.publish(publishTopic, message);
            Log.w(TAG,"Message Published");
            if(!mqttAndroidClient.isConnected()){
                Log.w(TAG,mqttAndroidClient.getBufferedMessageCount() + " messages in buffer.");
            }
        } catch (MqttException e) {
            System.err.println("Error Publishing: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

