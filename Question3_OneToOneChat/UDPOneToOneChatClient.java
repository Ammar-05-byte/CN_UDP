import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class UDPOneToOneChatClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 5000;
    private static final int BUFFER_SIZE = 1024;
    private static volatile boolean isRunning = true;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("          UDP ONE-TO-ONE CHAT CLIENT");
        System.out.println("==================================================");
        System.out.println("Connecting to Chat Server at " + SERVER_HOST + ":" + SERVER_PORT);
        System.out.println("Type your messages anytime (type 'bye' to quit):\n");

        try (DatagramSocket socket = new DatagramSocket();
             Scanner scanner = new Scanner(System.in)) {

            InetAddress serverAddress = InetAddress.getByName(SERVER_HOST);

            // Send initial connection greeting so server captures our port
            byte[] initBytes = "Hello Server, Client connected!".getBytes();
            DatagramPacket initPacket = new DatagramPacket(initBytes, initBytes.length, serverAddress, SERVER_PORT);
            socket.send(initPacket);

            // Background Thread for receiving messages continuously
            Thread receiveThread = new Thread(() -> {
                byte[] receiveBuffer = new byte[BUFFER_SIZE];
                while (isRunning && !socket.isClosed()) {
                    try {
                        DatagramPacket packet = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                        socket.receive(packet);

                        String msg = new String(packet.getData(), 0, packet.getLength()).trim();
                        System.out.println("\n[Server]: " + msg);
                        System.out.print("[Client]: ");

                        if (msg.equalsIgnoreCase("bye") || msg.equalsIgnoreCase("exit")) {
                            System.out.println("\n[Client] Server ended the chat session. Exiting.");
                            isRunning = false;
                            socket.close();
                            break;
                        }
                    } catch (IOException e) {
                        if (!socket.isClosed()) {
                            System.err.println("[Client Receive Error] " + e.getMessage());
                        }
                        break;
                    }
                }
            });
            receiveThread.setDaemon(true);
            receiveThread.start();

            // Main Thread handles keyboard input and sends messages
            while (isRunning) {
                System.out.print("[Client]: ");
                if (scanner.hasNextLine()) {
                    String outgoingMsg = scanner.nextLine();

                    if (outgoingMsg == null || outgoingMsg.trim().isEmpty()) {
                        continue;
                    }

                    byte[] sendBuffer = outgoingMsg.getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(
                            sendBuffer,
                            sendBuffer.length,
                            serverAddress,
                            SERVER_PORT
                    );
                    socket.send(sendPacket);

                    if (outgoingMsg.equalsIgnoreCase("bye") || outgoingMsg.equalsIgnoreCase("exit")) {
                        System.out.println("[Client] Closing chat session.");
                        isRunning = false;
                        socket.close();
                        break;
                    }
                } else {
                    break;
                }
            }

        } catch (IOException e) {
            System.err.println("[Client Error] " + e.getMessage());
        }
        System.out.println("[Client] Chat client stopped.");
    }
}
