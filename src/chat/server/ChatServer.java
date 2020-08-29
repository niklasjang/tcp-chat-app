package chat.server;

import java.io.Console;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ChatServer {
    public static final int PORT = 5000;
    public static Selector selector;
    public static ServerSocketChannel serverSocketChannel;
    public boolean isWithThreadPool;
    public static List<SocketChannel> scList = new ArrayList<SocketChannel>();

    public static void main(String[] args) throws IOException {

        try {
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress("localhost", PORT));
            serverSocketChannel.configureBlocking(false);
            SelectionKey selectionKey0 = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            selectionKey0.attach(new ChatServer.Acceptor());

            consoleLog("연결 기다림 - "+ serverSocketChannel.getLocalAddress());
            try {
                while (true) {
                    int numKeys = selector.select();
                    if( numKeys > 0){
                        Set selected = selector.selectedKeys();
                        Iterator it = selected.iterator();
                        while (it.hasNext()) {
                            SelectionKey key = (SelectionKey) (it.next());
                            if(key.isAcceptable()){
                                dispatch(key);
                            }
                            it.remove();
                        }
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if( serverSocketChannel != null && serverSocketChannel.isOpen() ) {
                    serverSocketChannel.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void dispatch(SelectionKey k) {
        //k.attachment = new ChatServer.Acceptor()
        Runnable r = (Runnable) (k.attachment());
        if (r != null) {
            r.run();
        }
    }

    private static void consoleLog(String log) {
        System.out.println("[server " + Thread.currentThread().getId() + "] " + log);
    }
    static class Acceptor implements Runnable {
        public void run() {
            try {
                SocketChannel socketChannel = serverSocketChannel.accept();
                if (socketChannel != null) {
//                    if (isWithThreadPool)
//                        new HandlerWithThreadPool(selector, socketChannel);
//                    else
                    new ChatServerProcessThread(selector, socketChannel,scList).start();
                }
                System.out.println("Connection Accepted by Reactor");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
