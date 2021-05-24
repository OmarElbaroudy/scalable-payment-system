import application.SignalingServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;


public class serverTester {
    private static SignalingServer signalingServer;

    @BeforeAll
    static void init() {
        try {
            signalingServer.start();
        } catch (Exception e) {
            System.err.println();
        }
    }
    @AfterAll
    static void close(){
        try {
            signalingServer.stop();
        } catch (Exception e) {
            System.err.println();
        }

    }
}
