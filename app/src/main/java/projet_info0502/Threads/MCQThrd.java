package projet_info0502.Threads;

import java.io.IOException;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import projet_info0502.DBManager.DBManager; 
import projet_info0502.Exceptions.MCQManagerException;
import org.json.JSONObject;


public class MCQThrd implements Runnable{

    private MqttClient client;
    private JSONObject request;

    public MCQThrd(MqttClient client, JSONObject request){
        this.client = client;
        this.request = request;
    }

    @Override
    public void run(){
        try{
            String action = request.getString("action");
            String sessionId = request.getString("sessionId");
            String qcmId = request.getString("qcmId");

            switch (action){
                case "START":
                    handleStart(sessionId, qcmId);
                    break;
                
                case "ANSWER_ALL":
                    handleAnswerAll(sessionId, qcmId);
                    break;

                default:
                    throw new MCQManagerException("Action MCQ inconnue : " + action, 400);
                
            }


        } catch(MCQManagerException e){
            sendError(e.getMessage(), e.getCode());
        } catch(Exception e){
            sendError("Erreur interne dans MCQThrd : "+ e.getMessage(), 500);
            e.printStackTrace();
        }  
    }

    private void handleStart(String sessionId, String qcmId) throws Exception{
        if(!DBManager.MCQExists(qcmId)){
            throw new MCQManagerException("QCM introuvable : "+ qcmId, 404);
        }

        JSONObject qcm = DBManager.getMCQ(qcmId);
        JSONObject response = new JSONObject().put("status", "MCQ_DATA").put("qcmId", qcmId).put("questions", qcm);

        client.publish("Client" + sessionId, response.toString().getBytes(), 0, false);
    }

    private void handleAnswerAll(String sessionId, String qcmId) throws Exception{
        JSONObject answers = request.getJSONObject("answers");
        JSONObject scoreRequest = new JSONObject().put("service", "SCORE").put("action", "SAVE").put("sessionId", sessionId).put("qcmId", qcmId).put("answers", answers);

        Thread scoreThrd = new Thread(new ScoreThrd(client, scoreRequest));
        scoreThrd.start();
    }
}

