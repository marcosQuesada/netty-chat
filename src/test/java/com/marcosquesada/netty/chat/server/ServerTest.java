package com.marcosquesada.netty.chat.server;

import com.marcosquesada.netty.chat.Main;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;


public class ServerTest {
    private static Server server = new Server(5555);

    @BeforeClass
    public static void setup() {
        Main.configureLogger();

        server.start();
    }

    @Test
    public void EndToEndTest() {

        try {
            Thread.sleep(500L);
            // Client A Open Session
            Client clientA = new Client();
            clientA.write("/login foo pass\n");
            String resA = clientA.readLine();
            Assert.assertTrue(resA.equals("Welcome foo"));

            // Client A Joins topic
            clientA.write("/join fakeTopic\n");
            resA = clientA.readLine();
            Assert.assertTrue(resA.equals("Joined room fakeTopic"));

            // Client B Open Session
            Client clientB = new Client();
            clientB.write("/login bar pass\n");
            String resB = clientB.readLine();
            Assert.assertTrue(resB.equals("Welcome bar"));

            // Client B Joins topic
            clientB.write("/join fakeTopic\n");
            resB = clientB.readLine();
            Assert.assertTrue(resB.equals("Joined room fakeTopic"));

            // Client A publish on topic
            clientA.write("Hi There\n");

            // Client B must receive published message
            String pub = clientB.readLine();
            Assert.assertTrue(pub.equals("Hi There"));

            // Check users on room
            clientB.write("/users\n");
            resB = clientB.readLine();
            Assert.assertTrue(resB.equals("Users on topic fakeTopic"));

            // As Order Result is not guaranteed (Iterates over users Collection), we store it in a list
            List<String> users = new ArrayList<>();
            resB = clientB.readLine();
            users.add(resB.trim().replace("-", ""));

            resB = clientB.readLine();
            users.add(resB.trim().replace("-", ""));

            // Assert foo & bar exists as users
            Assert.assertTrue(users.contains("foo"));
            Assert.assertTrue(users.contains("bar"));

            // Client A leaves room
            clientA.write("/leave\n");
            resA = clientA.readLine();
            Assert.assertTrue(resA.equals("Bye Bye!"));

            // Users show just ClientB user
            clientB.write("/users\n");
            clientB.readLine();

            users = new ArrayList<>();
            resB = clientB.readLine();
            users.add(resB.trim().replace("-", ""));

            Assert.assertFalse(users.contains("foo"));
            Assert.assertTrue(users.contains("bar"));

            // disconnect
            clientA.close();
            clientB.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @AfterClass
    public static void tearDown() {
        server.terminate();
    }

    class Client {

        private BufferedWriter bufferedWriter;
        private BufferedReader bufferedReader;

        Client() throws IOException {
            InetAddress inteAddress = InetAddress.getByName("localhost");
            SocketAddress socketAddress = new InetSocketAddress(inteAddress, 5555);

            // create a socket
            Socket socket = new Socket();

            // this method will block no more than timeout ms.
            int timeoutInMs = 10 * 1000;   // 10 seconds
            socket.connect(socketAddress, timeoutInMs);

            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        }

        public void write(String msg) throws IOException {
            bufferedWriter.write(msg);
            bufferedWriter.flush();

        }

        public String readLine() throws IOException {
            return bufferedReader.readLine();
        }

        public void close() throws IOException{
            bufferedReader.close();
        }
    }

}
