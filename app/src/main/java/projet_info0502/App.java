package projet_info0502;

import java.util.Scanner;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;
import org.json.JSONArray;

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
    private static volatile RequestedService rs = new RequestedService("", false);
    private static volatile boolean connected = true;

    private static JSONObject currentMCQ = null;
    private static String currentMCQId = null;

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
            rs.service = "Authenticate"; rs.waiting = true;
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
            rs.service = "Register"; rs.waiting = true;
        } catch (MqttException e){
            e.printStackTrace();
        }
    }

    public static void disconnection(){
        try{
            System.out.println("Disconnection");

            JSONObject request = new JSONObject().put("service", "Disconnection").put("params", new JSONObject().put("topic", TOPIC_CLIENT).put("sessionId", sessionId));

            String messageText = request.toString();
            MqttMessage message = new MqttMessage(messageText.getBytes());
            client.publish(TOPIC_MCQMANAGER, message);
            rs.service = "Disconnection"; rs.waiting = true;
        }catch (MqttException e){
            e.printStackTrace();
        }
    }

    public static void startMCQ(){
        try{
            System.out.print("Entrer l'ID du QCM (ex: mcq1) : ");
            String qcmId = scan.nextLine();
            currentMCQId = qcmId;

            JSONObject request = new JSONObject().put("service", "MCQ").put("params", new JSONObject().put("action", "START").put("MCQId", qcmId).put("topic", TOPIC_CLIENT).put("sessionId", sessionId));
            MqttMessage message = new MqttMessage(request.toString().getBytes());
            client.publish(TOPIC_MCQMANAGER, message); 
            
            rs.service = "MCQ_START"; rs.waiting = true;
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void manageQuestions(JSONObject questions) {
        JSONObject answers = new JSONObject();
        
        try {
            for(int i = 1; i <= 20; i++){
                System.out.println("Question " + i + " : " + questions.getString(Integer.toString(i)) + "\n0. " + questions.getJSONArray("propositions").get(0) + " ; 1. " + questions.getJSONArray("propositions").get(1) + " ; 2. " + questions.getJSONArray("propositions").get(2));
                int choice;
                do{
                    System.out.print("Choix : ");
                    choice = Integer.parseInt(scan.nextLine());
                } while(choice < 0 || choice > 2);
                answers.put(Integer.toString(i), choice);
            }

            JSONObject request = new JSONObject().put("service", "MCQ").put("params", new JSONObject().put("action", "ANSWER_ALL").put("MCQId", currentMCQId).put("topic", TOPIC_CLIENT).put("sessionId", sessionId).put("answers", answers));
            MqttMessage message = new MqttMessage(request.toString().getBytes());
            client.publish(TOPIC_MCQMANAGER, message);
            rs.service = "MCQ_RESULT"; rs.waiting = true;
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public static void requestScores(){
        try{
            JSONObject request = new JSONObject().put("service", "Scores").put("params", new JSONObject().put("topic", TOPIC_CLIENT).put("sessionId", sessionId));
            MqttMessage message = new MqttMessage(request.toString().getBytes());
            client.publish(TOPIC_MCQMANAGER, message);
            rs.service = "Scores"; rs.waiting = true;
        } catch(MqttException e){
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
            while(connected){
                if(!rs.waiting){
                    if(sessionId.equals(""))
                        System.out.println("Que faire ? 1: Authentification, 2: Register");
                    else
                        System.out.println("Que faire ? 1: Deconnexion, 2: Faire un QCM, 3: Demander les scores");

                    do{
                        choice = Integer.parseInt(scan.nextLine());
                    }while(((sessionId.equals("")) && (choice < 0 || choice > 2)) || (!sessionId.equals("") && (choice < 0 || choice > 3)));

                    switch(choice){
                        case 1:
                            if(sessionId.equals(""))
                                authenticate();
                            else
                                disconnection();
                            break;
                        case 2:
                            if(sessionId.equals(""))
                                register();
                            else 
                                startMCQ();
                            break;
                        case 3:
                            break;
                    }
                }
            }
            client.disconnect();
            client.close();
        } catch(MqttException e) {
            e.printStackTrace();
        }

        scan.close();
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        JSONObject answer = new JSONObject(message.toString());

        if((rs.service.equals("Authenticate") || rs.service.equals("Register")) && rs.waiting == true){
            String status = answer.getString("status");
            if(status.equals("OK")){
                JSONObject params = answer.getJSONObject("params");
                sessionId = params.getString("sessionId");
                System.out.println("Authentification et/ou Registeration réussie. ID de session : " + sessionId);
                rs.service = ""; rs.waiting = false;
            }else{
                System.out.println("Authentification et/ou Registration échoué.");
                rs.service = ""; rs.waiting = false;
            }
        }

        if(rs.service.equals("Disconnection") && rs.waiting == true){
            String status = answer.getString("status");
            if(status.equals("OK")){
                System.out.println("Déconnexion réussie.");
                connected = false;
            }else{
                System.out.println("Problème de déconnexion côté serveur. Déconnexion forcée.");
                connected = false;
            }
        }

        if(rs.service.equals("MCQ_START") && rs.waiting == true){
            currentMCQ = answer.getJSONObject("params").getJSONObject("MCQ");
            manageQuestions(currentMCQ);
        }

        if(rs.service.equals("MCQ_RESULT") && rs.waiting == true){
            System.out.println("Score : " + answer.getInt("score") + "/20");
            rs.waiting = false;
        }

        if(rs.service.equals("Scores") && rs.waiting == true){
            JSONArray scores = answer.getJSONObject("params").getJSONArray("scores");
            for(int i = 0; i < scores.length(); i++){
                JSONArray data = (JSONArray)(scores.get(i));
                System.out.println("QCM " + data.getString(0) + " : " + data.getInt(1) + "/20");
            }
            rs.waiting = false;
        }
    }
    @Override
    public void connectionLost(Throwable cause){}
    @Override
    public void deliveryComplete(IMqttDeliveryToken token){}
}
