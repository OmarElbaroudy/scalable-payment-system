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

public class SignalingServer {
    private static final String QUEUE_NAME = "SIGNALING_SERVER";
    private static final String exchangeName = "BLOCKCHAIN";
    private static Server server;
    private static Channel channel;
    private static Connection connection;
    private static RocksHandler handler;
    private static SegmentationServices segServer;

    public static void segment() throws Exception {
        System.out.println("started segmenting...");
        segServer = new SegmentationServices(handler, channel);

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
                            segServer.exec(task, body);
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
    }

    public static void initRabbit() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        connection = factory.newConnection();
        channel = connection.createChannel();

        channel.exchangeDeclare(exchangeName, BuiltinExchangeType.DIRECT, false, true, null);
        channel.queueDeclare(QUEUE_NAME, false, false, true, null);
        channel.queueBind(QUEUE_NAME, exchangeName, QUEUE_NAME);
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

        channel.basicPublish(exchangeName, "Logger", props, json.toString().getBytes());
    }

    public static void main(String[] args) throws Exception {
        start();
    }
}
