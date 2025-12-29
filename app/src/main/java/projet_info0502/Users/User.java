package projet_info0502.Users;

public class User {
    private String nick;
    private String sessionId;
    private Status status;

    public User(String sId, String n, Status s){
        this.sessionId = sId;
        this.nick = new String(n);
        this.status = s;
    }

    public String getSessionId(){ return new String(this.sessionId); }
    public void setSessionId(String id){
        this.sessionId = new String(id);
    }
    public String getNick(){ return new String(this.nick); }
    public Status getStatus() { return this.status; }

    @Override
    public String toString(){
        return "User -- sessionId: " + this.sessionId + ", status: " + this.status;
    }
}
