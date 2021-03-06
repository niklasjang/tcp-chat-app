package chat.client;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ChatWindow {
    private int BUF_SIZE = 1024;
    private String name;
    private Frame frame;
    private Panel pannel;
    private Button buttonSend;
    private TextField textField;
    private TextArea textArea;

    private Socket socket;

    public ChatWindow(String name, Socket socket) {
        this.name = name;
        frame = new Frame(name);
        pannel = new Panel();
        buttonSend = new Button("Send");
        textField = new TextField();
        textArea = new TextArea(30, 80);
        this.socket = socket;

        new ChatClientReceiveThread(socket).start();
    }

    public void show() {
        // Button
        buttonSend.setBackground(Color.GRAY);
        buttonSend.setForeground(Color.WHITE);
        buttonSend.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent actionEvent ) {
                sendMessage();
            }
        });


        // Textfield
        textField.setColumns(80);
        textField.addKeyListener( new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                char keyCode = e.getKeyChar();
                if (keyCode == KeyEvent.VK_ENTER) {
                    sendMessage();
                }
            }
        });

        // Pannel
        pannel.setBackground(Color.LIGHT_GRAY);
        pannel.add(textField);
        pannel.add(buttonSend);
        frame.add(BorderLayout.SOUTH, pannel);

        // TextArea
        textArea.setEditable(false);
        frame.add(BorderLayout.CENTER, textArea);

        // Frame
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                OutputStream os;
                try {
                    os = socket.getOutputStream();
                    String request = "quit\r\n";
                    byte[] byteData = request.getBytes();
                    os.write(byteData);
                    os.flush();
                }
                catch (IOException e1) {
                    e1.printStackTrace();
                }


            }
        });
        frame.setVisible(true);
        frame.pack();
    }

    // 쓰레드를 만들어서 대화를 보내기
    private void sendMessage() {
        PrintWriter pw;
        try {
            OutputStream os = socket.getOutputStream();
            String message = textField.getText();
            if(message.length() != 0){
                String request = new String("message:") + message + new String("\r\n");
                byte[] byteData = request.getBytes(StandardCharsets.UTF_8);
                os.write(byteData);
                textField.setText("");
                textField.requestFocus();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ChatClientReceiveThread extends Thread{
        Socket socket = null;

        ChatClientReceiveThread(Socket socket){
            this.socket = socket;
        }

        public void run() {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                InputStream is = socket.getInputStream();
                byte[] byteData;
                while(!Thread.interrupted()) {
                    byteData = new byte[BUF_SIZE];
                    is.read(byteData,0,BUF_SIZE);
                    String msg = new String(byteData,StandardCharsets.UTF_8);
                    if(byteData[0] == 101
                            && byteData[1] == 120
                            && byteData[2] == 105
                            && byteData[3] == 116){
                        Thread.currentThread().interrupt();
                        continue;
                    }
                    textArea.append(msg);
                    textArea.append("\n");
                }
                System.exit(0);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}