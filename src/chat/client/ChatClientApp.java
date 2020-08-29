//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package chat.client;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ChatClientApp {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 5000;

    public ChatClientApp() {
    }

    public static void main(String[] args) {
        String name = null;
        Scanner scanner = new Scanner(System.in);

        while(true) {
            System.out.println("대화명을 입력하세요.");
            System.out.print(">>> ");
            name = scanner.nextLine();
            if (!name.isEmpty()) {
                scanner.close();
                Socket socket = new Socket();
                try {
                    socket.connect(new InetSocketAddress(SERVER_IP, 5000));
                    consoleLog("채팅방에 입장하였습니다.");
                    (new ChatWindow(name, socket)).show();
                    OutputStream os = socket.getOutputStream();
                    String request = new String("join:") + name + new String("\r\n");
                    byte[] byteData = request.getBytes();
                    os.write(byteData);
                    os.flush();
                } catch (UnknownHostException e) {
                    System.err.println("Unknown host: " + SERVER_IP);
                    System.exit(1);
                }  catch (IOException var6) {
                    var6.printStackTrace();
                }

                return;
            }

            System.out.println("대화명은 한글자 이상 입력해야 합니다.\n");
        }
    }

    private static void consoleLog(String log) {
        System.out.println(log);
    }
}
