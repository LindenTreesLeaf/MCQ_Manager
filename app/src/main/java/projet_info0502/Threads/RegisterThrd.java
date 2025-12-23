package projet_info0502.Threads;

import java.io.IOException;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import projet_info0502.DBManager.DBManager;
import projet_info0502.Users.ServerManager;
import projet_info0502.Users.User;
import projet_info0502.Users.Status;

public class RegisterThrd implements Runnable{
    private JSONObject request;
    private String sessionId = null;
    private ServerManager sm;
    private MqttClient client;

    private String TOPIC;

    public RegisterThrd(MqttClient c, JSONObject r, ServerManager sm){
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

            String nick = params.getString("nick");
            String password = params.getString("password");
            String email = params.getString("email");
            String statusStr = params.getString("status");

            try{
                try {
                    DBManager.getUserJson(nick);
                    answer.put("status", "KO").put("error", "Nickname est déjà utuilisé.");
                } catch (IOException e){

                    DBManager.registerNewUser(nick, password, email, statusStr.equals("TEACHER") ? Status.TEACHER : Status.STUDENT);

                    String sId = DBManager.generateSessionId(); this.sessionId = sId;
                    User u = new User(this.sessionId, Status.getStatus(statusStr));
                    sm.addUser(u);

                    answer.put("status", "OK");
                    answer.put("params", new JSONObject().put("sessionId", sId).put("nick", nick).put("role", statusStr));
                }
            } catch (IOException e){
                answer.put("status", "KO").put("error", "Erreur serveur : " + e.getMessage());
            }

            String messageText = answer.toString();
            MqttMessage message = new MqttMessage(messageText.getBytes());
            this.client.publish(TOPIC, message);

        } catch (MqttException e){
            e.printStackTrace();
        }
    }
}