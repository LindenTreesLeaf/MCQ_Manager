package projet_info0502.Threads;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import projet_info0502.Users.ServerManager;
import projet_info0502.Users.User;

public class DisconnectionThrd implements Runnable{
    private JSONObject request;
    private ServerManager sm;
    private MqttClient client;

    private String TOPIC;

    public DisconnectionThrd(MqttClient c, JSONObject r, ServerManager sm){
        this.client = c;
        this.request = r;
        this.TOPIC = this.request.getJSONObject("params").getString("topic");
        this.sm = sm;
    }

    public ServerManager getServerManager(){ return this.sm; }

    @Override
    public void run(){
        try{
            JSONObject params = this.request.getJSONObject("params");
            User u = sm.checkAuth(params.getString("sessionId"));
            JSONObject answer = new JSONObject();

            if(u != null){
                answer.put("status", "OK");
                sm.removeUser(u);
            }else{
                answer.put("status", "KO").put("error", "Session ID not recognised.");
            }

            String messageText = answer.toString();
            MqttMessage message = new MqttMessage(messageText.getBytes());
            this.client.publish(TOPIC, message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
