
package chat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ChatServerProcessThread extends Thread{
    private String nickname = null;
    List<SocketChannel> scList = null;
    private int BUF_SIZE = 1024;
    final SocketChannel socketChannel;
    final SelectionKey selectionKey;
    ByteBuffer input = ByteBuffer.allocate(BUF_SIZE);
    static final int READING = 0, SENDING = 1;
    int state = READING;
    String clientName = "";
    Charset charset = Charset.defaultCharset();
    Selector selector = null;
    public ChatServerProcessThread(
            Selector selector,
            SocketChannel c,
            List<SocketChannel> scList)
            throws IOException {
        this.selector = selector;
        this.socketChannel = c;
        this.scList = scList;
        c.configureBlocking(false);
        selectionKey = socketChannel.register(selector, SelectionKey.OP_READ);
        selectionKey.attach(this);
        selectionKey.interestOps(SelectionKey.OP_READ);
        selector.wakeup();
    }

    @Override
    public void run() {
        System.out.println("run");
        try {
            while(true) {
                selector.select();
                if(!selectionKey.isReadable())
                    continue;
                input = ByteBuffer.wrap(new byte[BUF_SIZE]);
                input.limit(BUF_SIZE);
                int readCount = socketChannel.read(input);
                input.flip();
                if( readCount == 0) {
                    consoleLog("클라이언트로부터 연결 끊김");
                    doQuit(socketChannel);
                    break;
                }
                String request = new String(input.array() , StandardCharsets.UTF_8);
                String[] tokens = request.split(":");
                if("join".equals(tokens[0])) {
                    doJoin(tokens[1], socketChannel);
                }
                else if("message".equals(tokens[0])) {
                    doMessage(tokens[1]);
                }
                else if("quit".equals(tokens[0])) {
                    doQuit(socketChannel);
                }else{
                    System.out.println("ERROR:" + tokens[0]);
                }
            }
        }
        catch(IOException e) {
            consoleLog(this.nickname + "님이 채팅방을 나갔습니다.");
        }
    }

    private void doQuit(SocketChannel sc) throws IOException {
        removeSocketChannel(sc);

        String str = nickname + new String("님이 퇴장했습니다.");
        ByteBuffer data = ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8));
        broadcast(data);
        data.clear();
    }

    private void removeSocketChannel(SocketChannel sc) {
        synchronized (scList) {
            scList.remove(sc);
        }
    }

    private void doMessage(String data) throws IOException {
        ByteBuffer byteData = ByteBuffer.wrap(data.getBytes(StandardCharsets.UTF_8));
        broadcast(byteData);
        byteData.clear();
    }

    private void doJoin(String nickname, SocketChannel sc) throws IOException {
        this.nickname = nickname;
        String str = nickname + new String("님이 입장하였습니다.");
        ByteBuffer data = ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8));
        broadcast(data);
        data.clear();
        // writer pool에 저장
        addSocketChannel(sc);
    }

    private void addSocketChannel(SocketChannel sc) {
        synchronized (scList) {
            scList.add(sc);
        }
    }

    private void broadcast(ByteBuffer data) throws IOException {
        synchronized (scList) {
            for(SocketChannel sc : scList) {
                sc.write(data);
            }
        }
    }
    private void consoleLog(String log) {
        System.out.println(log);
    }
}