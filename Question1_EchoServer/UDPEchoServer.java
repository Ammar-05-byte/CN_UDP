import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class UDPEchoServer {
    private static final int PORT = 9876;
    private static final int BUFFER_SIZE = 1024;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("          UDP ECHO SERVER (PORT " + PORT + ")");
        System.out.println("==================================================");

        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            byte[] receiveBuffer = new byte[BUFFER_SIZE];
            System.out.println("[Server] Server is running and waiting for client datagrams...");

            while (true) {
                // Prepare packet to receive incoming data
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                socket.receive(receivePacket);

                String clientMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
                InetAddress clientAddress = receivePacket.getAddress();
                int clientPort = receivePacket.getPort();

                System.out.println("\n[Server] Received packet from: " + clientAddress.getHostAddress() + ":" + clientPort);
                System.out.println("[Server] Message content: \"" + clientMessage + "\"");

                // Echo the message back to the client
                byte[] echoBuffer = clientMessage.getBytes();
                DatagramPacket echoPacket = new DatagramPacket(
                        echoBuffer,
                        echoBuffer.length,
                        clientAddress,
                        clientPort
                );
                socket.send(echoPacket);
                System.out.println("[Server] Successfully echoed back " + echoBuffer.length + " bytes to client.");

                // If client sends 'exit' or 'quit', server logs client disconnect
                if (clientMessage.trim().equalsIgnoreCase("exit") || clientMessage.trim().equalsIgnoreCase("quit")) {
                    System.out.println("[Server] Client requested termination: " + clientAddress.getHostAddress() + ":" + clientPort);
                }
            }
        } catch (IOException e) {
            System.err.println("[Server Error] Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
