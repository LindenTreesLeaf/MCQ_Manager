package projet_info0502.Threads;

import java.io.IOException;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import projet_info0502.DBManager.DBManager;
import projet_info0502.Users.ServerManager;
import projet_info0502.Users.User;

public class AuthentificationThrd implements Runnable{
    private JSONObject request;
    private ServerManager sm;
    private MqttClient client;

    private String TOPIC;

    public AuthentificationThrd(MqttClient c, JSONObject r, ServerManager sm){
        this.client = c;
        this.request = r;
        this.TOPIC = this.request.getJSONObject("params").getString("topic");
        this.sm = sm;
    }

    public ServerManager getServerManager(){ return this.sm; }

    @Override
    public void run() {
        try{            
            JSONObject params = this.request.getJSONObject("params");
            JSONObject answer = new JSONObject();
            try{
                if(DBManager.authenticate(params.getString("nick"), params.getString("password"))){
                    answer.put("status", "OK");
                    String sessionId = DBManager.generateSessionId();
                    answer.put("params", new JSONObject().put("sessionId", sessionId));

                    String userStatus = DBManager.getUserJson(params.getString("nick")).getString("status");
                    User u = new User(sessionId, params.getString("nick"), projet_info0502.Users.Status.getStatus(userStatus));
                    sm.addUser(u);
                } else{
                    answer.put("status", "KO").put("error", "Authentification IDs do not exist.");
                }
            } catch(IOException e){}

            String messageText = answer.toString();
            MqttMessage message = new MqttMessage(messageText.getBytes());
            this.client.publish(TOPIC, message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
