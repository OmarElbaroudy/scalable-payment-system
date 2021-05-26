package application;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import persistence.RocksHandler;
import services.RegistrationServices;


import java.io.IOException;


public class Deregister extends HttpServlet {
    private static RocksHandler handler;

    public Deregister() {}

    public static void setHandler(RocksHandler Rockshandler){
        handler = Rockshandler;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            final String nodeId = req.getHeader("nodeId");
            RegistrationServices.deregister(nodeId, handler);
            resp.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            System.out.println(e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

    }
}
