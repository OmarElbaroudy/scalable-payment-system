package application;


import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import persistence.MongoHandler;
import persistence.models.User;

import java.io.IOException;

public class Balance extends HttpServlet {
    private static MongoHandler handler;

    public static void setHandler(MongoHandler mongoHandler) {
        handler = mongoHandler;
    }


    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            User user = handler.getUserById(req.getHeader("userId"));
            String pubKey = user.getPubKey();

            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("server", "API");
            jsonObject.addProperty("id", user.getUserId());
            jsonObject.addProperty("task", "balance");

            API.log(jsonObject);
            API.getBalance(req, resp, pubKey);
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
