package projet_info0502.DBManager;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import projet_info0502.Users.Status;
import org.json.JSONObject;
import org.junit.jupiter.api.Test; 

public class DBManagerTest {

    @Test
    public void testRegisterNewUser() throws IOException {
        DBManager.registerNewUser("alice", "pass123", "alice@mail.com", Status.STUDENT);

        JSONObject userJson = DBManager.getUserJson("alice");
        assertEquals("pass123", userJson.getString("password"));
        assertEquals("alice@mail.com", userJson.getString("email"));
        assertEquals("STUDENT", userJson.getString("status"));
    }

    @Test
    public void testAuthenticate() throws IOException {

    DBManager.registerNewUser("bob", "secret123", "bob@mail.com", Status.TEACHER);
    boolean resultSuccess = DBManager.authenticate("bob", "secret123");
    assertTrue(resultSuccess, "L'authentification va réussir avec le bon mot de passe");

    DBManager.registerNewUser("Thomas", "good123", "Thomas@mail.com", Status.STUDENT);
    boolean resultFail = DBManager.authenticate("Thomas", "bad123");
    assertFalse(resultFail, "L'authentification va échouer avec un mauvais mot de passe");
}


    @Test
    public void testGenerateSessionIdUnique() {
        String id1 = DBManager.generateSessionId();
        String id2 = DBManager.generateSessionId();
        assertNotEquals(id1, id2);
    }
}

    

