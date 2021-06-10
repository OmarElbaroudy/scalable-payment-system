package application;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;


public class API {
    private final String QUEUE_NAME = "API_SERVER";
    private Server server;
    private String EXCHANGE_NAME;

    public void start() throws Exception {
        server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(3000);
        server.setConnectors(new Connector[]{connector});

        ServletContextHandler context = new ServletContextHandler();

        context.addServlet(Register.class, "/register");
        context.addServlet(Login.class, "/login");
        context.addServlet(Balance.class, "/getBalance");
        context.addServlet(CreateTransaction.class, "/generateTransaction");

        server.setHandler(context);
        server.start();
    }

    /*public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse
            response)throws IOException, ServletException {
        baseRequest.setHandled(true);

        try {
            Authentication authentication = digestAuthenticator.validateRequest(request, response, true);

            if (authentication instanceof Authentication.User) {
                response.setContentType("text/plain");
                Authentication.User user = (Authentication.User) authentication;
                response.getWriter().println(user.getAuthMethod());
            } else if (authentication instanceof Authentication.ResponseSent) {
                Authentication.ResponseSent responseSent = (Authentication.ResponseSent) authentication;
            }
        } catch (ServerAuthException e) {
            e.printStackTrace();
        }
    }*/
    public void stop() throws Exception {
        server.stop();
    }
}
