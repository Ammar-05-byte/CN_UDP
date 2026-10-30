import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class UDPDNSClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8053;
    private static final int BUFFER_SIZE = 1024;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("            UDP DNS CLIENT APPLICATION");
        System.out.println("==================================================");
        System.out.println("DNS Resolver Server: " + SERVER_HOST + ":" + SERVER_PORT);
        System.out.println("Enter a URL or domain name (e.g. google.com, vit.ac.in, github.com).");
        System.out.println("Type 'exit' to quit.\n");

        try (DatagramSocket socket = new DatagramSocket();
             Scanner scanner = new Scanner(System.in)) {

            socket.setSoTimeout(5000); // 5-second response timeout
            InetAddress serverAddress = InetAddress.getByName(SERVER_HOST);
            byte[] receiveBuffer = new byte[BUFFER_SIZE];

            while (true) {
                System.out.print("[Client] Enter URL / Domain: ");
                if (!scanner.hasNextLine()) {
                    break;
                }
                String queryUrl = scanner.nextLine().trim();

                if (queryUrl.isEmpty()) {
                    continue;
                }

                if (queryUrl.equalsIgnoreCase("exit") || queryUrl.equalsIgnoreCase("quit")) {
                    System.out.println("\n[Client] Exiting DNS Client. Goodbye!");
                    break;
                }

                // Send DNS query packet to server
                byte[] queryBytes = queryUrl.getBytes();
                DatagramPacket queryPacket = new DatagramPacket(
                        queryBytes,
                        queryBytes.length,
                        serverAddress,
                        SERVER_PORT
                );
                socket.send(queryPacket);

                // Receive DNS reply from server
                DatagramPacket replyPacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                try {
                    socket.receive(replyPacket);
                    String reply = new String(replyPacket.getData(), 0, replyPacket.getLength());
                    System.out.println("[Client] <-- DNS Server Reply: " + reply + "\n");
                } catch (IOException e) {
                    System.err.println("[Client Error] Request timed out or server unreachable.\n");
                }
            }

        } catch (IOException e) {
            System.err.println("[Client Error] " + e.getMessage());
            e.printStackTrace();
        }
    }
}
