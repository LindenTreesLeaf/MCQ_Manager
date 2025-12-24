package projet_info0502.Users;

import java.util.Vector;

import projet_info0502.Exceptions.MCQManagerException;

public class ServerManager {
    private Vector<User> users;

    public ServerManager(){
        this.users = new Vector<User>();
    }

    public void addUser(User u){
        this.users.add(u);
    }
    public User checkAuth(String sessionId){
        if(this.users.size() > 0){
            int i = 0;
            User res = this.users.get(i);
            while(!res.getSessionId().equals(sessionId) && i < this.users.size()){
                i++;
            }
            if(res.getSessionId().equals(sessionId))
                return res;
            else
                return null;
        }
        else
            throw new MCQManagerException("Trying to get a User when none is connected.");
    }
    public void removeUser(User u){
        this.users.remove(u);
    }

    @Override
    public String toString(){
        String res = "ServerManager:\n[";
        for(int i = 0; i < this.users.size(); i++){
            res += this.users.get(i).toString();
            if(i != (this.users.size() - 1)){
                res += ", ";
            }
        }
        res += "]";
        return res;
    }
}
