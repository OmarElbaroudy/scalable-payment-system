package application;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import persistence.MongoHandler;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.stream.Collectors;

public class Login extends HttpServlet {
    private static MongoHandler handler;

    public Login() {
    }

    public static void setHandler(MongoHandler mongoHandler) {
        handler = mongoHandler;
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String body = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            JsonObject json = JsonParser.parseString(body).getAsJsonObject();

            String userName = json.get("userName").getAsString();
            String password = json.get("password").getAsString();

            String userId = handler.findUser(userName, password).getUserId();
            JsonObject userIdJson = new JsonObject();
            userIdJson.addProperty("userId", userId);
            String jsonString = userIdJson.toString();

            resp.setContentType("application/json");
            resp.setStatus(HttpServletResponse.SC_OK);

            PrintWriter out = resp.getWriter();
            out.print("login successful!");

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
