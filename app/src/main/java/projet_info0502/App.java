package projet_info0502;

import java.util.Scanner;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

public class App implements MqttCallback{
    private static MqttClient client;
    public static final String HOST = "tcp://10.11.33.106:1883";
    public static final String TOPIC_MCQMANAGER = "MCQManager";
    public static final String TOPIC_CLIENT = "ClientStudent1";
    public static final String CLIENT_ID = "student1";

    private static String sessionId = "";
    private static boolean waitAnswerAuth = false;

    public static void authenticate(){
        Scanner scan = new Scanner(System.in);

        try {
            System.out.print("Authentification :\nEntrer nickname : ");
            String nick = scan.nextLine();
            System.out.print("Entrer password : ");
            String password = scan.nextLine();

            JSONObject request = new JSONObject().put("service", "Authentification").put("params", new JSONObject().put("topic", TOPIC_CLIENT).put("nick", nick).put("password", password));
            
            String messageText = request.toString();
            MqttMessage message = new MqttMessage(messageText.getBytes());
            client.publish(TOPIC_MCQMANAGER, message);
            waitAnswerAuth = true;
        } catch (MqttException e) {
            e.printStackTrace();
        }

        scan.close();
    }

    public static void main(String[] args) {
        try{
            client = new MqttClient(HOST, CLIENT_ID);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            client.setCallback(new App());
            client.connect(options);
            client.subscribe(TOPIC_CLIENT);

            Scanner scan = new Scanner(System.in);
            while(true){
                if(!waitAnswerAuth){
                    if(sessionId.equals(""))
                        System.out.println("Que faire ? 1: Authentification");
                    else
                        System.out.println("Que faire ? 1: Deconnexion");

                    int choice;
                    do{
                        choice = Integer.parseInt(scan.nextLine());
                    }while(choice < 0 && choice > 1);

                    switch(choice){
                        case 1:
                            if(sessionId.equals(""))
                                authenticate();
                            else{}
                            break;
                    }
                }
            }
            // scan.close();
        } catch(MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        JSONObject answer = new JSONObject(message.toString());

        if(waitAnswerAuth){
            String status = answer.getString("status");
            if(status.equals("OK")){
                JSONObject params = answer.getJSONObject("params");
                sessionId = params.getString("sessionId");
                System.out.println("Authentification réussie. ID de session : " + sessionId);
                waitAnswerAuth = false;
            }else{
                System.out.println("Authentification échoué.");
                waitAnswerAuth = false;
            }
        }
    }
    @Override
    public void connectionLost(Throwable cause){}
    @Override
    public void deliveryComplete(IMqttDeliveryToken token){}
}
