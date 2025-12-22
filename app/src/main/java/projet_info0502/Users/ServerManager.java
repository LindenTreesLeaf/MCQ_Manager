package projet_info0502.Users;

import java.util.Vector;

public class ServerManager {
    private Vector<User> users;

    public ServerManager(){
        this.users = new Vector<User>();
    }

    public void addUser(User u){
        this.users.add(u);
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
