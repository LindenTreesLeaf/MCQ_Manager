package projet_info0502.Users;

import java.io.IOException;

import org.json.JSONObject;

import projet_info0502.DBManager.DBManager;
import projet_info0502.Exceptions.MCQManagerException;

public class User {
    private String sessionId;
    private JSONObject mcq;
    private Status status;

    public User(String sId, Status s){
        this.sessionId = sId;
        this.status = s;
        if(this.status == Status.STUDENT){
            this.mcq = new JSONObject();
        }
    }

    public String getSessionId(){ return new String(this.sessionId); }
    public void setSessionId(String id){
        this.sessionId = new String(id);
    }
    public Status getStatus() { return this.status; }
    public void setStatus(Status s){
        boolean teacher = false;
        if(this.status == Status.TEACHER) teacher = true;
        this.status = s;
        if((this.status == Status.STUDENT || this.status == Status.MIX) && teacher == true) this.mcq = new JSONObject();
    }

    public void setMCQ(String id) throws IOException{
        if(DBManager.MCQExists(id)){
            this.mcq.put("mcqId", id);
        }
        else
            throw new MCQManagerException("Trying to set an non existing MCQ to a User. Id used: " + id);
    }
    public void answerMCQ(int question, int select){
        if(question > 0 && question < 21 && select > 0 && select < 4)
            this.mcq.put(String.valueOf(question), String.valueOf(select));
        else
            throw new MCQManagerException("Trying to answer a MCQ with invalid values. Question answered (min=1, max=20): " + question + ", proposition selected (min=1, max=3): " + select);
    }

    @Override
    public String toString(){
        return "User -- sessionId: " + this.sessionId + ", status: " + this.status;
    }
}
