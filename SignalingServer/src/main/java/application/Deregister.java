package application;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import persistence.RocksHandler;
import services.RegistrationServices;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;


public class Deregister extends HttpServlet {
    private static RocksHandler handler;

    public Deregister() {
    }

    public static void setHandler(RocksHandler Rockshandler) {
        handler = Rockshandler;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {

            String body = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            JsonObject json = JsonParser.parseString(body).getAsJsonObject();

            String nodeId = json.get("nodeId").getAsString();
            RegistrationServices.deregister(nodeId, handler);

            if (handler.getNumberOfNodes() == 0) {
                SignalingServer.stop();
            }

            resp.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println(Arrays.toString(e.getStackTrace()));
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

    }
}
