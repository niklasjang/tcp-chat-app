package echo;

import echo.client.EchoClient;
import echo.server.EchoServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class EchoTest {

    Process server;
    EchoClient client;

    @Before
    public void setup() throws IOException, InterruptedException {
        server = EchoServer.start();
        client = EchoClient.start();
    }

    @Test
    public void givenServerClient_whenServerEchosMessage_thenCorrect() {
        String resp1 = client.sendMessage("hello");
        String resp2 = client.sendMessage("world");
        assertEquals("hello", resp1);
        assertEquals("world", resp2);
    }

    private void assertEquals(String hello, String resp1) {

    }

    @After
    public void teardown() throws IOException {
        server.destroy();
        EchoClient.stop();
    }
}
