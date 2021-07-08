package application;

import com.google.gson.JsonObject;
import com.rabbitmq.client.*;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import persistence.RocksHandler;
import services.SegmentationServices;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

public class SignalingServer {
    private static final String QUEUE_NAME = "SIGNALING_SERVER";
    private static final String exchangeName = "BLOCKCHAIN";
    private static Server server;
    private static Channel channel;
    private static Connection connection;
    private static RocksHandler handler;
    private static SegmentationServices segServer;
    private static Queue<byte[]> transactionTasks;

    public static void segment() throws Exception {
        System.out.println("started segmenting...");
        segServer = new SegmentationServices(handler, channel);
        transactionTasks = new LinkedList<>();
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

                        try {
                            if (segServer.isMining() && task.equals("createTransaction")) {
                                transactionTasks.add(body);
                            } else {
                                segServer.exec(task, body);

                                while (!segServer.isMining() && !transactionTasks.isEmpty()) {
                                    segServer.exec("createTransaction", transactionTasks.poll());
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
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

        context.addServlet(Register.class, "/register");
        context.addServlet(Deregister.class, "/deregister");

        server.setHandler(context);

        initRabbit();
        server.start();
        segment();
    }

    public static void initRabbit() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();

        factory.setUri(System.getenv("RABBIT_CONNECTION_URL"));
        connection = factory.newConnection();
        channel = connection.createChannel();

        channel.exchangeDeclare(exchangeName, BuiltinExchangeType.DIRECT, false, true, null);
        channel.queueDeclare(QUEUE_NAME, false, false, true, null);
    }

    public static void stop() throws Exception {
        channel.close();
        connection.close();
        server.stop();
    }

    public static void log(JsonObject json) throws Exception {
        AMQP.BasicProperties props =
                new AMQP.BasicProperties().
                        builder().
                        contentType("application/json").
                        build();

        channel.basicPublish("", "Logger", props, json.toString().getBytes());
    }

    public static void main(String[] args) throws Exception {
        start();
    }
}
