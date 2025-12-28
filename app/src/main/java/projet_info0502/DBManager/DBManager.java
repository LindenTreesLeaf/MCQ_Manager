package projet_info0502.DBManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.SecureRandom;
import java.util.Base64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import projet_info0502.Exceptions.MCQManagerException;
import projet_info0502.Users.User;

public class DBManager {
    public static final Path PATH_MCQ = Paths.get("database", "mcq.txt");
    public static final Path PATH_USERS = Paths.get("database", "users.txt");

    public static boolean MCQExists(String id)throws IOException{
        boolean res = true;

        BufferedReader reader = Files.newBufferedReader(PATH_MCQ, StandardCharsets.UTF_8);
        String line = reader.readLine();
        
        JSONObject mcq = new JSONObject(line);
        try{
            mcq.getJSONObject(id);
        } catch (JSONException e){
            res = false;
        }

        reader.close();
        return res;
    }
    public static JSONObject getMCQ(String id) throws IOException{
        BufferedReader reader = Files.newBufferedReader(PATH_MCQ, StandardCharsets.UTF_8);
        String line = reader.readLine();
        JSONObject mcq = new JSONObject(line);

        reader.close();
        return mcq.getJSONObject(id);
    }
    public static JSONObject getQuestions(String id){
        JSONObject questions = new JSONObject();
        try {
            JSONObject mcq = getMCQ(id);
            for(String key: mcq.keySet()){
                JSONArray propositions = new JSONArray();
                for(int i = 0; i < 3; i++){
                    JSONArray p = (JSONArray)(mcq.getJSONObject(key).getJSONArray("propositions").get(i));
                    propositions.put((String)(p.get(0)));
                }
                questions.put(key, new JSONObject().put("question", mcq.getJSONObject(key).getString("question")).put("propositions", propositions));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return questions;
    }
    public static int correctMCQ(User u, String MCQId, JSONObject answers) throws IOException{
        if(MCQExists(MCQId)){
            JSONObject mcq = getMCQ(MCQId);

            int res = 0;
            for(int i = 1; i <= 20; i++){
                JSONArray propositions = mcq.getJSONObject(Integer.toString(i)).getJSONArray("propositions");
                JSONArray choice = (JSONArray)(propositions.get(answers.getInt(Integer.toString(i))));
                if((boolean)(choice.get(1)) == true)
                    res++;
            }

            JSONObject uJSON = getUserJson(u);
            uJSON.getJSONArray("scores").put(new JSONArray().put(MCQId).put(res));

            BufferedReader reader = Files.newBufferedReader(PATH_USERS, StandardCharsets.UTF_8);
            String line = reader.readLine();
            JSONObject users = new JSONObject(line);
            users.put(u.getNick(), uJSON);

            BufferedWriter writer = Files.newBufferedWriter(PATH_USERS, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            writer.write(users.toString());
            writer.newLine();

            writer.close();
            reader.close();

            return res;
        } else
            throw new MCQManagerException("Trying to correct a non existing MCQ. MCQ ID provided: " + MCQId);
    }

    public static String generateSessionId() {
        final SecureRandom secureRandom = new SecureRandom();
        final Base64.Encoder base64Encoder = Base64.getUrlEncoder();

        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);

        return base64Encoder.encodeToString(randomBytes);
    }
    public static boolean authenticate(String nick, String password) throws IOException {
        boolean res = true;

        BufferedReader reader = Files.newBufferedReader(PATH_USERS, StandardCharsets.UTF_8);
        String line = reader.readLine();
        JSONObject users = new JSONObject(line);
        try{
            JSONObject u = users.getJSONObject(nick);
            if(!password.equals(u.getString("password")))
                res = false;
        } catch(JSONException e){
            res = false;
        }

        reader.close();
        return res;
    }
    public static synchronized void registerNewUser(String nick, String password, String email, projet_info0502.Users.Status status) throws IOException{
        JSONObject newUser = new JSONObject().put("password", password).put("email", email).put("scores", new JSONArray()).put("status", status);
        BufferedReader reader = Files.newBufferedReader(PATH_USERS, StandardCharsets.UTF_8);
        String line = reader.readLine();
        JSONObject users = new JSONObject(line);
        users.put(nick, newUser);

        BufferedWriter writer = Files.newBufferedWriter(PATH_USERS, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        writer.write(users.toString());
        writer.newLine();

        writer.close();
        reader.close();
    }
    public static JSONObject getUserJson(String nick) throws IOException{
        BufferedReader reader = Files.newBufferedReader(PATH_USERS, StandardCharsets.UTF_8);
        String line = reader.readLine();
        JSONObject users = new JSONObject(line);

        reader.close();
        return users.getJSONObject(nick);
    }
    public static JSONObject getUserJson(User u) throws IOException{
        return getUserJson(u.getNick());
    }
}
