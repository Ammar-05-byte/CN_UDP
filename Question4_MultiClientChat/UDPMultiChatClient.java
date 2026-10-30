import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class UDPMultiChatClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 6000;
    private static final int BUFFER_SIZE = 2048;
    private static volatile boolean isRunning = true;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("         UDP MULTI-CLIENT CHAT ROOM");
        System.out.println("==================================================");

        try (DatagramSocket socket = new DatagramSocket();
             Scanner scanner = new Scanner(System.in)) {

            InetAddress serverAddress = InetAddress.getByName(SERVER_HOST);

            // 1. Determine username
            String username = null;
            if (args.length > 0) {
                username = args[0].trim();
            } else {
                System.out.print("Enter your chat username: ");
                if (scanner.hasNextLine()) {
                    username = scanner.nextLine().trim();
                }
            }

            if (username == null || username.isEmpty()) {
                username = "User_" + socket.getLocalPort();
            }

            System.out.println("Welcome, [" + username + "]! Connecting to server at " + SERVER_HOST + ":" + SERVER_PORT);
            System.out.println("Type your messages and press ENTER. Type 'exit' to leave the chat room.\n");

            // 2. Send JOIN packet to server
            String joinMsg = "JOIN:" + username;
            byte[] joinBytes = joinMsg.getBytes();
            DatagramPacket joinPacket = new DatagramPacket(joinBytes, joinBytes.length, serverAddress, SERVER_PORT);
            socket.send(joinPacket);

            final String clientUser = username;

            // 3. Background Thread for receiving broadcast messages
            Thread receiverThread = new Thread(() -> {
                byte[] receiveBuffer = new byte[BUFFER_SIZE];
                while (isRunning && !socket.isClosed()) {
                    try {
                        DatagramPacket packet = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                        socket.receive(packet);

                        String msg = new String(packet.getData(), 0, packet.getLength()).trim();
                        System.out.println("\n" + msg);
                        System.out.print("[" + clientUser + "]: ");
                    } catch (IOException e) {
                        if (!socket.isClosed()) {
                            System.err.println("[Receiver Error] " + e.getMessage());
                        }
                        break;
                    }
                }
            });
            receiverThread.setDaemon(true);
            receiverThread.start();

            // 4. Main Thread for reading console input and sending chat messages
            while (isRunning) {
                System.out.print("[" + username + "]: ");
                if (!scanner.hasNextLine()) {
                    break;
                }
                String text = scanner.nextLine().trim();

                if (text.isEmpty()) {
                    continue;
                }

                if (text.equalsIgnoreCase("exit") || text.equalsIgnoreCase("quit")) {
                    // Send LEAVE message to server
                    String leaveMsg = "LEAVE:" + username;
                    byte[] leaveBytes = leaveMsg.getBytes();
                    DatagramPacket leavePacket = new DatagramPacket(leaveBytes, leaveBytes.length, serverAddress, SERVER_PORT);
                    socket.send(leavePacket);

                    System.out.println("\n[Client] You have left the chat room. Goodbye!");
                    isRunning = false;
                    socket.close();
                    break;
                }

                // Send regular chat message
                String chatMsg = "MSG:" + username + ":" + text;
                byte[] chatBytes = chatMsg.getBytes();
                DatagramPacket chatPacket = new DatagramPacket(chatBytes, chatBytes.length, serverAddress, SERVER_PORT);
                socket.send(chatPacket);
            }

        } catch (IOException e) {
            System.err.println("[Client Error] " + e.getMessage());
        }
    }
}
