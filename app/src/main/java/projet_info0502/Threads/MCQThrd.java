package projet_info0502.Threads;

import java.io.IOException;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import projet_info0502.DBManager.DBManager; 
import projet_info0502.Exceptions.MCQManagerException;
import projet_info0502.Users.ServerManager;
import projet_info0502.Users.User;

import org.json.JSONObject;


public class MCQThrd implements Runnable{
    private ServerManager sm;
    private MqttClient client;
    private JSONObject request;

    private String TOPIC;
    private String MCQId;

    public MCQThrd(MqttClient client, JSONObject request, ServerManager s){
        this.client = client;
        this.request = request;
        this.TOPIC = this.request.getJSONObject("params").getString("topic");
        this.MCQId = this.request.getJSONObject("params").getString("MCQId");
        this.sm = s;
    }

    @Override
    public void run(){
        try{
            JSONObject params = this.request.getJSONObject("params");
            User u = sm.checkAuth(params.getString("sessionId"));

            String action = params.getString("action");

            switch (action){
                case "START":
                    handleStart();
                    break;
                case "ANSWER_ALL":
                    handleAnswerAll();
                    break;
                default:
                    JSONObject answer = new JSONObject();
                    answer.put("status", "KO").put("error", "Action not allowed. Action requested: " + action + ". Actions allowed: \"START\", \"ANSWER_ALL\"");
                    String messageText = answer.toString();
                    MqttMessage message = new MqttMessage(messageText.getBytes());
                    this.client.publish(TOPIC, message);
                    break;
            }
        } catch(MqttException e){
            e.printStackTrace();
        } catch (MCQManagerException e){
            // checkAuth renvoie une erreur
        }
    }

    private void handleStart(){
        try{
            JSONObject answer = new JSONObject();

            if(!DBManager.MCQExists(this.MCQId)){
                answer.put("status", "KO").put("error", "MCQ not found. ID given: " + this.MCQId);
            }
            else{
                JSONObject mcq = DBManager.getMCQ(this.MCQId);
                answer.put("status", "OK").put("params", new JSONObject().put("MCQ", mcq));
            }

            String messageText = answer.toString();
            MqttMessage message = new MqttMessage(messageText.getBytes());
            this.client.publish(TOPIC, message);
        } catch (MqttException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private void handleAnswerAll(){
        try {
            JSONObject answers = request.getJSONObject("params").getJSONObject("answers");
            int res = DBManager.correctMCQ(this.MCQId, answers);

            JSONObject answer = new JSONObject();
            answer.put("status", "OK").put("params", new JSONObject().put("result", res));
            String messageText = answer.toString();
            MqttMessage message = new MqttMessage(messageText.getBytes());
            this.client.publish(TOPIC, message);
        } catch (MqttException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}

