package application;

import persistence.RocksHandler;
import services.RegistrationServices;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class Deregister extends HttpServlet {
    private RocksHandler handler;

    public Deregister() {
        try {
            this.handler = new RocksHandler();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            final String nodeId = req.getParameter("nodeId");
            RegistrationServices.deregister(nodeId, handler);
            resp.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

    }
}
