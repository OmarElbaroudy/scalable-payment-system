package application;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;

public class SignalingServer {
    private Server server;

    public void start() throws Exception {
        server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(5000);
        server.setConnectors(new Connector[]{connector});

        ServletContextHandler context = new ServletContextHandler();
        context.addServlet(Register.class.getName(), "/register");
        server.start();
    }

    public void stop() throws Exception {
        server.stop();
    }

}
