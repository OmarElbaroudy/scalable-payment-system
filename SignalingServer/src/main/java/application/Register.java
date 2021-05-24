package application;

import com.google.gson.JsonObject;
import persistence.RocksHandler;
import services.RegistrationServices;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class Register extends HttpServlet {
    private RocksHandler handler;

    public Register() {
        try {
            this.handler = new RocksHandler();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
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
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("application/json");
            out.print(json);
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

}
