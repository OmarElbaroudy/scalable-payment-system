import application.Deregister;
import application.Register;
import application.SignalingServer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.okhttp.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import persistence.RocksHandler;

import static org.junit.jupiter.api.Assertions.fail;


public class SignalingServerTester {
    private final static String BASE_URL = "http://localhost:5000";
    private static OkHttpClient client;
    private static RocksHandler handler;

    @BeforeAll
    static void init() {
        try {
            handler = new RocksHandler();
            Deregister.setHandler(handler);
            Register.setHandler(handler);

            SignalingServer.start();
            client = new OkHttpClient();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @AfterAll
    static void close() {
        try {
            SignalingServer.stop();
            handler.closeHandler();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @DisplayName("registering node correctly")
    @Test
    void whenNodeRegisters_thenCorrectResponseIsReturned() {
        try {
            Request req = new Request.Builder().url(BASE_URL + "/register").build();
            ResponseBody resBody = client.newCall(req).execute().body();
            JsonObject json = JsonParser.parseString(resBody.string()).getAsJsonObject();

            System.out.println(json);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @DisplayName("Unregistering node correctly")
    @Test
    void whenNodeDeRegisters_thenCommitteesAreUpdatedCorrectly() {
        try {
            Request req = new Request.Builder().url(BASE_URL + "/register").build();
            ResponseBody resBody = client.newCall(req).execute().body();
            JsonObject json = JsonParser.parseString(resBody.string()).getAsJsonObject();

            String nodeId = json.get("nodeId").getAsString();

            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(JSON, json.toString());

            req = new Request.Builder().url(BASE_URL + "/deregister").
                    addHeader("nodeId", nodeId).
                    post(body).build();

            client.newCall(req).execute();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

}
