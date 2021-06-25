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
            String[] parent = RegistrationServices.getParentNodeId(nodeId, handler);

            json.addProperty("nodeId", nodeId);
            json.addProperty("primaryQueue", primaryQueue);
            json.addProperty("committeeQueue", committeeQueue);

            //parent not found e.g first node in system
            if (parent[0].equals("nil")) {
                json.addProperty("parentType", "nil");
                json.addProperty("parentId", "");

            } else if (parent[1].equals("SAME_COMMITTEE")) {
                json.addProperty("parentType", "SAME_COMMITTEE");
                json.addProperty("parentId", parent[0]);

            } else if (parent[1].equals("DIFFERENT_COMMITTEE")) {
                json.addProperty("parentType", "DIFFERENT_COMMITTEE");
                json.addProperty("parentId", parent[0]);
            }

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("server", "signaling");
            jsonObject.addProperty("id", nodeId);
            jsonObject.addProperty("task", "register");
            jsonObject.addProperty("committee", committeeQueue);

            SignalingServer.log(jsonObject);

            int cnt = handler.getNumberOfNodes();
            if (cnt == Integer.parseInt(System.getenv("TOTAL_NUMBER_OF_NODES"))) {
                SignalingServer.segment();
            }

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("application/json");
            out.print(json);
        } catch (Exception e) {
            System.err.println(Arrays.toString(e.getStackTrace()));
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

}
