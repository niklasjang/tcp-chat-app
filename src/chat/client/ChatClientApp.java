//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package chat.client;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ChatClientApp {
    private static final String SERVER_IP = "192.168.43.46";
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
                    socket.connect(new InetSocketAddress("192.168.43.46", 5000));
                    consoleLog("채팅방에 입장하였습니다.");
                    (new ChatWindow(name, socket)).show();
                    PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
                    String request = "join:" + name + "\r\n";
                    pw.println(request);
                } catch (IOException var6) {
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
