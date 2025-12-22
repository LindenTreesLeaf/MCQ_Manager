package projet_info0502;

import java.io.IOException;

import projet_info0502.DBManager.DBManager;

public class App {
    public static void main(String[] args) throws IOException{
        DBManager m = new DBManager();
        System.out.println(m.getQCM(1).getJSONObject("1"));
    }
}
