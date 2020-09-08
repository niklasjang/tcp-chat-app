
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
import java.util.Scanner;

public class ChatServerProcessThread extends Thread{
    final Selector selector;
    final SocketChannel socketChannel;
    final List<SocketChannel> scList;
    final SelectionKey selectionKey;

    public List<SocketChannel> getScList() {
        return scList;
    }

    static final int STATE_READING = 0;
    static final int STATE_WRITING = 1;
    static final int STATE_TERMINATING = -1;
    static int state = STATE_TERMINATING;
    private String nickname = null;
    private int BUF_SIZE = 1024;
    ByteBuffer input = ByteBuffer.allocate(BUF_SIZE);
    Scanner scanner;
    public ChatServerProcessThread(
            SocketChannel c,
            List<SocketChannel> scList)
            throws IOException {
        this.selector = Selector.open();
        this.socketChannel = c;
        this.scList = scList;
        this.state = STATE_READING;
        c.configureBlocking(false);
        selectionKey = socketChannel.register(selector, SelectionKey.OP_READ);
        selectionKey.attach(this);
        selectionKey.interestOps(SelectionKey.OP_READ);
        selector.wakeup();


    }

    @Override
    public void run() {
        System.out.println("run :" + Thread.currentThread().getId());
        try {
            while(state != STATE_TERMINATING ) {
                selector.select();
                if(!selectionKey.isReadable())
                    continue;
                scanner = new Scanner(socketChannel, "utf-8");
                scanner.useDelimiter("\r\n");
                state = STATE_READING;
                String request = scanner.nextLine();
                String[] tokens = request.split(":");
                state = STATE_WRITING;
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
            consoleLog("terminate thread in thread");
        }
        catch(IOException e) {
            e.printStackTrace();
            consoleLog(this.nickname + "님이 채팅방을 나갔습니다.");
        }
    }

    private void doJoin(String nickname, SocketChannel sc) throws IOException {
        this.nickname = nickname;
        addSocketChannel(sc);
        String str = nickname + new String("님이 입장하였습니다.");
        ByteBuffer data = ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8));
        broadcast(data);
    }

    private void doMessage(String data) throws IOException {
        String nickWithData = nickname + ":" + data;
        ByteBuffer byteData = ByteBuffer.wrap(nickWithData.getBytes(StandardCharsets.UTF_8));
        broadcast(byteData);
    }

    private void doQuit(SocketChannel sc) throws IOException {
        removeSocketChannel(sc);

        String str = "exit";
        ByteBuffer data = ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8));
        while(data.hasRemaining()){
            int writeSize = sc.write(data);
        }
        data.clear();

        str = nickname + new String("님이 퇴장했습니다.");
        data = ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8));
        broadcast(data);
        data.clear();
        //de-register from selector
        selectionKey.cancel();
    }

    private void broadcast(ByteBuffer data) throws IOException {
        synchronized (scList) {
            for(SocketChannel sc : scList) {
                System.out.println("=======");
                while(data.hasRemaining()){
                    int writeSize = sc.write(data);
                    System.out.println(writeSize);
                }
                data.clear();
            }
        }
    }

    private void addSocketChannel(SocketChannel sc) {
        synchronized (scList) {
            scList.add(sc);
        }
    }

    private void removeSocketChannel(SocketChannel sc) {
        synchronized (scList) {
            scList.remove(sc);
            if(scList.size() == 0){
                state= STATE_TERMINATING;
            }
        }
    }

    private void consoleLog(String log) {
        System.out.println(log);
    }
}