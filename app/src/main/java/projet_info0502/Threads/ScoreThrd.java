package projet_info0502.Threads;

import java.io.IOException;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import projet_info0502.DBManager.DBManager;
import projet_info0502.Exceptions.MCQManagerException;
import projet_info0502.Users.ServerManager;
import projet_info0502.Users.User;

public class ScoreThrd implements Runnable{
    private ServerManager sm;
    private MqttClient client;
    private JSONObject request;

    private String TOPIC;

    public ScoreThrd(MqttClient client, JSONObject request, ServerManager s){
        this.client = client;
        this.request = request;
        this.TOPIC = this.request.getJSONObject("params").getString("topic");
        this.sm = s;
    }

    @Override
    public void run(){
        try {
            JSONObject params = this.request.getJSONObject("params");
            User u = sm.checkAuth(params.getString("sessionId"));

            JSONObject user = DBManager.getUserJson(u);
            JSONObject answer = new JSONObject();
            answer.put("status", "OK").put("params", new JSONObject().put("scores", user.getJSONArray("scores")));

            String messageText = answer.toString();
            MqttMessage message = new MqttMessage(messageText.getBytes());
            this.client.publish(TOPIC, message);

        } catch(MqttException e){
            e.printStackTrace(); 
        } catch(IOException e){
            e.printStackTrace(); 
        } catch (MCQManagerException e) {
            // checkAuth renvoie une exception
        }
    }
}