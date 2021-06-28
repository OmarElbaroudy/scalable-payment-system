package application;

import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import persistence.RocksHandler;
import services.RegistrationServices;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

public class Register extends HttpServlet {
    private static RocksHandler handler;

    public Register() {
    }

    public static void setHandler(RocksHandler Rockshandler) {
        handler = Rockshandler;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try (PrintWriter out = resp.getWriter()) {
            JsonObject json = new JsonObject();
            String nodeId = RegistrationServices.generateNodeId();
            String primaryQueue = RegistrationServices.getPrimaryQueue();
            String committeeQueue = RegistrationServices.getCommitteeQueue(nodeId, handler);

            json.addProperty("nodeId", nodeId);
            json.addProperty("primaryQueue", primaryQueue);
            json.addProperty("committeeQueue", committeeQueue);

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("server", "signaling");
            jsonObject.addProperty("id", nodeId);
            jsonObject.addProperty("task", "register");
            jsonObject.addProperty("committee", committeeQueue);

            SignalingServer.log(jsonObject);

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("application/json");
            out.print(json);
        } catch (Exception e) {
            System.err.println(Arrays.toString(e.getStackTrace()));
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

}
