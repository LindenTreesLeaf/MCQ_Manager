package projet_info0502.DBManager;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.json.JSONObject;

import projet_info0502.Exceptions.MCQManagerException;

public class DBManager {
    public JSONObject getQCM(int id) throws IOException{
        if(id > 0){
            InputStream is = getClass().getClassLoader().getResourceAsStream("bdd/mcq.txt");
            if (is == null) {
                throw new FileNotFoundException("mcq.txt not found");
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String line = "";
            for(int i = 0; i < id; i++) {
                if((line = reader.readLine()) == null){
                    throw new MCQManagerException("Multiple-choice questionnaire not found.", -2);
                }
            }
            
            reader.close();
            is.close();
            return new JSONObject(line);
        }
        else
            throw new MCQManagerException("Invalid ID to fetch QCM.", -1);
    }
}
