import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class UDPEchoClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 9876;
    private static final int BUFFER_SIZE = 1024;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("          UDP ECHO CLIENT");
        System.out.println("==================================================");
        System.out.println("Connecting to Echo Server at " + SERVER_HOST + ":" + SERVER_PORT);
        System.out.println("Type your message and press ENTER. Type 'exit' to quit.\n");

        try (DatagramSocket socket = new DatagramSocket();
             Scanner scanner = new Scanner(System.in)) {

            InetAddress serverAddress = InetAddress.getByName(SERVER_HOST);
            byte[] receiveBuffer = new byte[BUFFER_SIZE];

            while (true) {
                System.out.print("[Client] Enter message: ");
                if (!scanner.hasNextLine()) {
                    break;
                }
                String message = scanner.nextLine();

                if (message == null || message.trim().isEmpty()) {
                    continue;
                }

                // Send message to Echo Server
                byte[] sendBuffer = message.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(
                        sendBuffer,
                        sendBuffer.length,
                        serverAddress,
                        SERVER_PORT
                );
                socket.send(sendPacket);

                // Receive echoed response from Server
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                socket.receive(receivePacket);

                String echoedMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
                System.out.println("[Client] <-- Server Echoed: \"" + echoedMessage + "\"");

                if (message.trim().equalsIgnoreCase("exit") || message.trim().equalsIgnoreCase("quit")) {
                    System.out.println("\n[Client] Exiting client application. Goodbye!");
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("[Client Error] Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
