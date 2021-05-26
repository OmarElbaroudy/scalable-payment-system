package application;

import com.rabbitmq.client.*;
import io.github.cdimascio.dotenv.Dotenv;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SignalingServer {
    private final String QUEUE_NAME = "SIGNALING_SERVER";
    private Server server;
    private String EXCHANGE_NAME;

    public void start() throws Exception {
        server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(5000);
        server.setConnectors(new Connector[]{connector});

        ServletContextHandler context = new ServletContextHandler();


        context.addServlet(Register.class, "/register");
        context.addServlet(Deregister.class, "/deregister");

        server.setHandler(context);
        server.start();
    }

    public void initRabbit() throws Exception {
        setEXCHANGE_NAME();

        ConnectionFactory factory = new ConnectionFactory();
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT, false, true, null);
        channel.queueDeclare(QUEUE_NAME, false, false, true, null);
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, QUEUE_NAME);

        Map<String, Object> mp = new HashMap<>();
        mp.put("task", "validateTransaction");

        AMQP.BasicProperties props =
                new AMQP.BasicProperties().
                        builder().
                        headers(mp).
                        contentType("application/json").
                        build();


        channel.basicPublish(EXCHANGE_NAME, QUEUE_NAME, props, "test".getBytes());

        channel.basicConsume(QUEUE_NAME, false, "SIGNALING_SERVER",
                new DefaultConsumer(channel) {
                    @Override
                    public void handleDelivery(String consumerTag,
                                               Envelope envelope,
                                               AMQP.BasicProperties properties,
                                               byte[] body)
                            throws IOException {

                        String routingKey = envelope.getRoutingKey();
                        String contentType = properties.getContentType();
                        long deliveryTag = envelope.getDeliveryTag();

                        LongString ls = (LongString) properties.getHeaders().get("task");
                        String task = new String(ls.getBytes());

                        // (process the message components here ...)
                        //validateBlock => Block
                        //MineBlock by signaling
                        //CreateTransaction by signaling
                        //ValidateTransaction => transaction
                        //send blockchain by signaling

                        channel.basicAck(deliveryTag, false);
                    }
                });
    }

    private void setEXCHANGE_NAME() {
        String path = "/home/baroudy/Projects/Bachelor/payment-system/.env";
        Dotenv dotenv = Dotenv.configure().directory(path).load();
        this.EXCHANGE_NAME = dotenv.get("EXCHANGE_NAME");
    }

    public void stop() throws Exception {
        server.stop();
    }
}
