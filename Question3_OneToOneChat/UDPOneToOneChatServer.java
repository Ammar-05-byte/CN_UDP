import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class UDPOneToOneChatServer {
    private static final int SERVER_PORT = 5000;
    private static final int BUFFER_SIZE = 1024;
    private static volatile boolean isRunning = true;
    private static InetAddress clientAddress = null;
    private static int clientPort = -1;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("     UDP ONE-TO-ONE CHAT SERVER (PORT " + SERVER_PORT + ")");
        System.out.println("==================================================");
        System.out.println("[Server] Waiting for client to initiate chat...\n");

        try (DatagramSocket socket = new DatagramSocket(SERVER_PORT);
             Scanner scanner = new Scanner(System.in)) {

            // Background Thread for receiving messages continuously
            Thread receiveThread = new Thread(() -> {
                byte[] receiveBuffer = new byte[BUFFER_SIZE];
                while (isRunning && !socket.isClosed()) {
                    try {
                        DatagramPacket packet = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                        socket.receive(packet);

                        // Capture client details on first message received
                        if (clientAddress == null) {
                            clientAddress = packet.getAddress();
                            clientPort = packet.getPort();
                            System.out.println("\n[Server] Connected with Chat Client: " +
                                    clientAddress.getHostAddress() + ":" + clientPort);
                            System.out.println("[Server] You can now type messages anytime (type 'bye' to quit):\n");
                        }

                        String msg = new String(packet.getData(), 0, packet.getLength()).trim();
                        System.out.println("\n[Client]: " + msg);
                        System.out.print("[Server]: ");

                        if (msg.equalsIgnoreCase("bye") || msg.equalsIgnoreCase("exit")) {
                            System.out.println("\n[Server] Client disconnected. Closing chat session.");
                            isRunning = false;
                            socket.close();
                            break;
                        }
                    } catch (IOException e) {
                        if (!socket.isClosed()) {
                            System.err.println("[Server Receive Error] " + e.getMessage());
                        }
                        break;
                    }
                }
            });
            receiveThread.setDaemon(true);
            receiveThread.start();

            // Main Thread handles keyboard input and sends messages
            while (isRunning) {
                if (scanner.hasNextLine()) {
                    String outgoingMsg = scanner.nextLine();

                    if (outgoingMsg == null || outgoingMsg.trim().isEmpty()) {
                        continue;
                    }

                    if (clientAddress == null || clientPort == -1) {
                        System.out.println("[Server] Waiting for client connection before sending messages...");
                        continue;
                    }

                    byte[] sendBuffer = outgoingMsg.getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(
                            sendBuffer,
                            sendBuffer.length,
                            clientAddress,
                            clientPort
                    );
                    socket.send(sendPacket);

                    if (outgoingMsg.equalsIgnoreCase("bye") || outgoingMsg.equalsIgnoreCase("exit")) {
                        System.out.println("[Server] Terminating chat session.");
                        isRunning = false;
                        socket.close();
                        break;
                    }
                } else {
                    break;
                }
            }

        } catch (IOException e) {
            System.err.println("[Server Error] " + e.getMessage());
        }
        System.out.println("[Server] Chat server stopped.");
    }
}
