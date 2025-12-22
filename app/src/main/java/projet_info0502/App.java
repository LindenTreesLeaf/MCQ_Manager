package projet_info0502;

import java.io.IOException;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import projet_info0502.DBManager.DBManager;
import projet_info0502.Threads.AuthentificationThrd;
import projet_info0502.Users.ServerManager;

public class App implements MqttCallback{
    private static MqttClient client;
    public static final String HOST = "tcp://10.11.33.106:1883";
    public static final String TOPIC = "MCQManager";
    public static final String CLIENT_ID = "MCQServer";

    private static ServerManager sm = new ServerManager();

    public static void main(String[] args) throws IOException{
        try {
            client = new MqttClient(HOST, CLIENT_ID);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            client.setCallback(new App());
            client.connect(options);
            client.subscribe(TOPIC);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        JSONObject request = new JSONObject(message.toString());
        switch(request.getString("service")){
            case "Authentification":
                Thread authThrd = new Thread(new AuthentificationThrd(client, request, sm));
                authThrd.start();
                break;
            default:
                JSONObject answer = new JSONObject().put("status", "KO").put("error", "Service requested not recognised. Service requested: " + request.getString("service") + "; allowed services: \"Authentification\"");
                String messageText = answer.toString();
                MqttMessage m = new MqttMessage(messageText.getBytes());
                client.publish(TOPIC, m);
                break;
        }
        //tests -- a supprimer
        Thread.sleep(2500);
        System.out.println(sm);
    }
    @Override
    public void connectionLost(Throwable cause){}
    @Override
    public void deliveryComplete(IMqttDeliveryToken token){}
}
