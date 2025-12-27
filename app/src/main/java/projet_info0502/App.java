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
    private static String currentQcmId = null;

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
            currentQcmId = qcmId;
            JSONObject req = new JSONObject().put("service", "MCQ").put("action", "START").put("sessionId", sessionId).put("qcmId", qcmId);
            MqttMessage message = new MqttMessage(req.toString().getBytes());
            client.publish(TOPIC_MCQMANAGER, message); 
            
            rs = new RequestedService("MCQ_START", true);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void afficherQuestions(JSONObject questions) {
        for (String key : questions.keySet()) {
            JSONObject q = questions.getJSONObject(key);
            System.out.println("Question " + key + ": " + q.getString("question"));

            JSONArray props = q.getJSONArray("propositions");
            for (int i = 0; i < props.length(); i++) {
                JSONArray p = props.getJSONArray(i);
                System.out.println("  " + (i+1) + ". " + p.getString(0));
            }
            System.out.println();
        }
    }

    public static void sendAllAnswers() {
        try {
            JSONObject answers = new JSONObject();
            for (String key : currentMCQ.keySet()){
                System.out.print("Réponse à la question " + key + " : ");
                int rep = Integer.parseInt(scan.nextLine());
                answers.put(key, rep);
            }
            JSONObject req = new JSONObject().put("service", "MCQ").put("action", "ANSWER_ALL").put("sessionId", sessionId).put("qcmId", currentQcmId).put("answers", answers);
            MqttMessage message =  new MqttMessage(req.toString().getBytes());
            client.publish(TOPIC_MCQMANAGER, message);
            
            rs = new RequestedService("SCORE", true);
        } catch (Exception e){
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
                        System.out.println("Que faire ? 1: Deconnexion, 2: Faire un QCM");

                    do{
                        choice = Integer.parseInt(scan.nextLine());
                    }while(((sessionId.equals("")) && (choice < 0 || choice > 2)) || (!sessionId.equals("") && (choice < 0 || choice > 1)));

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

        if (answer.getString("status").equals("MCQ_DATA")){
            rs.waiting = false;
            currentMCQ = answer.getJSONObject("questions");
            System.out.println("QCM reçu : "+ answer.getString("qcmId"));
            afficherQuestions(currentMCQ);
            sendAllAnswers();
            return;
        }

        if (answer.getString("status").equals("SCORE_RESULT")){
            rs.waiting = false;
            System.out.println("Votre score : " + answer.getInt("score") + "/" +answer.getInt("total"));
            return;
        }
    }
    @Override
    public void connectionLost(Throwable cause){}
    @Override
    public void deliveryComplete(IMqttDeliveryToken token){}
}
