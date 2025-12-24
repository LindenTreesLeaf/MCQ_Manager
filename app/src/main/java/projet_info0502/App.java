package projet_info0502;

import java.util.Scanner;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import projet_info0502.Exceptions.MCQManagerException;

public class App implements MqttCallback{
    private static MqttClient client;
    public static String HOST;
    public static final String TOPIC_MCQMANAGER = "MCQManager";
    public static final String TOPIC_CLIENT = "ClientStudent1";
    public static final String CLIENT_ID = "student1";

    private static String sessionId = "";
    private static int choice;
    private static Scanner scan;
    private static volatile boolean waitAnswerAuth = false;

    public static void authenticate(){
        try {
            System.out.print("Authentification :\nEntrer nickname : ");
            String nick = scan.nextLine();
            System.out.print("Entrer password : ");
            String password = scan.nextLine();

            JSONObject request = new JSONObject().put("service", "Authentification").put("params", new JSONObject().put("topic", TOPIC_CLIENT).put("nick", nick).put("password", password));
            
            String messageText = request.toString();
            MqttMessage message = new MqttMessage(messageText.getBytes());
            client.publish(TOPIC_MCQMANAGER, message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public static void register(){
        try {
            System.out.print("Registration : \nEntrer nickname: ");
            String nick = scan.nextLine();
            System.out.print("Entrer password: ");
            String password = scan.nextLine();
            System.out.print("Entrer email: ");
            String email = scan.nextLine();
            System.out.print("Entrer status (STUDENT/TEACHER/MIX): ");
            String status = scan.nextLine();

            JSONObject request = new JSONObject().put("service", "Register").put("params", new JSONObject().put("topic", TOPIC_CLIENT).put("nick", nick).put("password", password).put("email", email).put("status", status));

            String messageText = request.toString();
            MqttMessage message = new MqttMessage(messageText.getBytes());
            client.publish(TOPIC_MCQMANAGER, message);

        } catch (MqttException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if(args.length == 1)
            HOST = args[0];
        else
            throw new MCQManagerException("Incorrect number of argument passed. Argument needed: MQTT Server's IP address, with format \"tcp://10.11.33.106:1883\"");

        try{
            client = new MqttClient(HOST, CLIENT_ID);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            client.setCallback(new App());
            client.connect(options);
            client.subscribe(TOPIC_CLIENT);

            scan = new Scanner(System.in);
            while(true){
                if(!waitAnswerAuth){
                    if(sessionId.equals(""))
                        System.out.println("Que faire ? 1: Authentification, 2: Register");
                    else
                        System.out.println("Que faire ? 1: Deconnexion");

                    do{
                        choice = Integer.parseInt(scan.nextLine());
                    }while(choice < 0 || choice > 2);

                    switch(choice){
                        case 1:
                            if(sessionId.equals("")){
                                waitAnswerAuth = true;
                                authenticate();
                            }
                            else{
                                //gestion déconnexion
                            }
                            break;
                        case 2:
                            waitAnswerAuth = true;
                            register();
                            break;
                    }
                }
            }
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
                System.out.println("Authentification et/ou Registeration réussie. ID de session : " + sessionId);
                waitAnswerAuth = false;
            }else{
                System.out.println("Authentification et/ou Registration échoué.");
                waitAnswerAuth = false;
            }
        }
    }
    @Override
    public void connectionLost(Throwable cause){}
    @Override
    public void deliveryComplete(IMqttDeliveryToken token){}
}
