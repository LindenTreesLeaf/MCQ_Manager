package projet_info0502;

import java.io.IOException;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONObject;

import projet_info0502.Exceptions.MCQManagerException;
import projet_info0502.Threads.AuthentificationThrd;
import projet_info0502.Threads.DisconnectionThrd;
import projet_info0502.Threads.RegisterThrd;
import projet_info0502.Users.ServerManager;
import projet_info0502.Threads.MCQThrd;
import projet_info0502.Threads.ScoreThrd;

public class App implements MqttCallback{
    private static MqttClient client;
    public static String HOST;
    public static final String TOPIC = "MCQManager";
    public static final String CLIENT_ID = "MCQServer";

    private static ServerManager sm = new ServerManager();

    public static void main(String[] args) throws IOException{
        if(args.length == 1)
            HOST = args[0];
        else
            throw new MCQManagerException("Incorrect number of argument passed.");

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
            case "Register":
                Thread regThrd = new Thread(new RegisterThrd(client, request, sm));
                regThrd.start();
                break;
            case "Disconnection":
                Thread discThrd = new Thread(new DisconnectionThrd(client, request, sm));
                discThrd.start();
                break;
            case "MCQ":
                Thread mcqThrd = new Thread(new MCQThrd(client, request, sm));
                mcqThrd.start();
                break;
            case "Scores":
                Thread scoreThrd = new Thread(new ScoreThrd(client, request, sm));
                scoreThrd.start();
                break;
            default:
                JSONObject answer = new JSONObject().put("status", "KO").put("error", "Service requested not recognised. Service requested: " + request.getString("service") + "; allowed services: \"Authentification\", \"Register\", \"Disconnection\", \"MCQ\", \"Scores\"");
                String messageText = answer.toString();
                MqttMessage m = new MqttMessage(messageText.getBytes());
                client.publish(TOPIC, m);
                break;
        }
    }
    @Override
    public void connectionLost(Throwable cause){}
    @Override
    public void deliveryComplete(IMqttDeliveryToken token){}
}
