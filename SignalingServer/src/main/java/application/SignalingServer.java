package application;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rabbitmq.client.*;
import io.github.cdimascio.dotenv.Dotenv;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import persistence.RocksHandler;
import services.CoordinationServices;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SignalingServer {
    private static final String QUEUE_NAME = "SIGNALING_SERVER";
    private static Server server;
    private static Channel channel;
    private static String EXCHANGE_NAME;
    private static Connection connection;
    private static RocksHandler handler;
    private static CoordinationServices services;
    private static boolean mining = false;
    private static int transactionNumber = 0, curCommittee = 0;

    public static void segment() throws Exception {
        System.out.println("started segmenting...");
        int numberOfCommittees = handler.getNumberOfCommittees();

        channel.basicConsume(QUEUE_NAME, false, "SIGNALING_SERVER",
                new DefaultConsumer(channel) {
                    @Override
                    public void handleDelivery(String consumerTag,
                                               Envelope envelope,
                                               AMQP.BasicProperties properties,
                                               byte[] body)
                            throws IOException {

                        long deliveryTag = envelope.getDeliveryTag();

                        LongString ls = (LongString) properties.getHeaders().get("task");
                        String task = new String(ls.getBytes());

                        if (task.equals("createTransaction")) {
                            transactionNumber++;
                            curCommittee = curCommittee % numberOfCommittees + 1;
                            String nodeId = services.getRandomNodeId(String.valueOf(curCommittee));

                            System.out.println(nodeId + " assigned to create transaction");

                            Map<String, Object> mp = new HashMap<>();
                            mp.put("task", "createTransaction");

                            AMQP.BasicProperties props =
                                    new AMQP.BasicProperties().
                                            builder().
                                            headers(mp).
                                            contentType("application/json").
                                            build();

                            channel.basicPublish(EXCHANGE_NAME, nodeId, props, body);
                        }

                        if (task.equals("getBalance")) {
                            curCommittee = curCommittee % numberOfCommittees + 1;
                            String nodeId = services.getRandomNodeId(String.valueOf(curCommittee));

                            Map<String, Object> mp = new HashMap<>();
                            mp.put("task", "getBalance");

                            AMQP.BasicProperties props =
                                    new AMQP.BasicProperties().
                                            builder().
                                            headers(mp).
                                            contentType("application/json").
                                            build();

                            channel.basicPublish(EXCHANGE_NAME, nodeId, props, body);
                        }


                        if (task.equals("routeBalance")) {
                            Map<String, Object> mp = new HashMap<>();
                            mp.put("task", "balance");

                            AMQP.BasicProperties props =
                                    new AMQP.BasicProperties().
                                            builder().
                                            headers(mp).
                                            contentType("application/json").
                                            build();

                            channel.basicPublish(EXCHANGE_NAME, "API", props, body);
                        }

                        if (task.equals("blockValidated")) {
                            String s = new String(body);
                            JsonObject json = JsonParser.parseString(s).getAsJsonObject();
                            String nodeId = json.get("nodeId").getAsString();
                            mining = !services.endBlockValidationPhase(nodeId);
                        }

                        if (!mining && transactionNumber >= 1000) {
                            for (int i = 1; i <= numberOfCommittees; i++) {
                                String nodeId = services.getRandomNodeId(String.valueOf(i));

                                Map<String, Object> mp = new HashMap<>();
                                mp.put("task", "mine");

                                AMQP.BasicProperties props =
                                        new AMQP.BasicProperties().
                                                builder().
                                                headers(mp).
                                                contentType("application/json").
                                                build();

                                channel.basicPublish(EXCHANGE_NAME, nodeId, props, null);
                            }
                        }

                        channel.basicAck(deliveryTag, false);
                    }
                });
    }

    public static void start() throws Exception {
        server = new Server();
        ServerConnector connector = new ServerConnector(server);

        connector.setPort(5000);

        server.setConnectors(new Connector[]{connector});
        ServletContextHandler context = new ServletContextHandler();

        handler = new RocksHandler();
        Register.setHandler(handler);
        Deregister.setHandler(handler);
        services = new CoordinationServices(handler);

        context.addServlet(Register.class, "/register");
        context.addServlet(Deregister.class, "/deregister");

        server.setHandler(context);

        initRabbit();
        server.start();
    }

    public static void initRabbit() throws Exception {
        setEXCHANGE_NAME();

        ConnectionFactory factory = new ConnectionFactory();
        connection = factory.newConnection();
        channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT, false, true, null);
        channel.queueDeclare(QUEUE_NAME, false, false, true, null);
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, QUEUE_NAME);
    }

    private static void setEXCHANGE_NAME() {
        String path = "/home/baroudy/Projects/Bachelor/payment-system/.env";
        Dotenv dotenv = Dotenv.configure().directory(path).load();
        EXCHANGE_NAME = dotenv.get("EXCHANGE_NAME");
    }

    public static void stop() throws Exception {
        channel.close();
        connection.close();
        server.stop();
    }

    public static void main(String[] args) throws Exception {
        start();
    }
}
