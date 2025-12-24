package projet_info0502;

public class RequestedService {
    public boolean waiting;
    public String service;

    public RequestedService(String s, boolean b){
        this.waiting = b;
        this.service = new String(s);
    }
}
