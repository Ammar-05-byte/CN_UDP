import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UDPMultiChatServer {
    private static final int PORT = 6000;
    private static final int BUFFER_SIZE = 2048;

    // Concurrent map storing active client socket addresses and their usernames
    private static final Map<SocketAddress, String> activeClients = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   UDP MULTI-CLIENT BROADCAST SERVER (PORT " + PORT + ")");
        System.out.println("==================================================");
        System.out.println("[Server] Broadcast server initialized. Waiting for clients...\n");

        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            byte[] receiveBuffer = new byte[BUFFER_SIZE];

            while (true) {
                DatagramPacket packet = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                socket.receive(packet);

                SocketAddress clientAddress = packet.getSocketAddress();
                String receivedText = new String(packet.getData(), 0, packet.getLength()).trim();

                if (receivedText.startsWith("JOIN:")) {
                    String username = receivedText.substring(5).trim();
                    activeClients.put(clientAddress, username);

                    System.out.println("[Server] New Client Registered: \"" + username + "\" from " + clientAddress);
                    System.out.println("[Server] Active Members Count: " + activeClients.size());

                    String joinBroadcast = "*** [SYSTEM] User '" + username + "' entered the chat! (Online: " + activeClients.size() + ") ***";
                    broadcast(socket, joinBroadcast, null);

                } else if (receivedText.startsWith("LEAVE:")) {
                    String username = receivedText.substring(6).trim();
                    activeClients.remove(clientAddress);

                    System.out.println("[Server] Client Disconnected: \"" + username + "\" (" + clientAddress + ")");
                    String leaveBroadcast = "*** [SYSTEM] User '" + username + "' left the chat. (Online: " + activeClients.size() + ") ***";
                    broadcast(socket, leaveBroadcast, null);

                } else if (receivedText.startsWith("MSG:")) {
                    // Format: MSG:username:message
                    int firstColon = receivedText.indexOf(':', 4);
                    if (firstColon != -1) {
                        String sender = receivedText.substring(4, firstColon).trim();
                        String message = receivedText.substring(firstColon + 1).trim();

                        System.out.println("[Server] Broadcasting message from [" + sender + "]: " + message);
                        String formattedMsg = "[" + sender + "]: " + message;

                        // Broadcast to all clients except the sender
                        broadcast(socket, formattedMsg, clientAddress);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("[Server Error] " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Broadcasts a datagram message to all active clients.
     * @param socket the DatagramSocket used to send
     * @param message the string payload
     * @param excludeAddress optional client endpoint to exclude (e.g. sender)
     */
    private static void broadcast(DatagramSocket socket, String message, SocketAddress excludeAddress) {
        byte[] data = message.getBytes();
        for (SocketAddress client : activeClients.keySet()) {
            if (excludeAddress != null && client.equals(excludeAddress)) {
                continue; // Do not echo back to the sender
            }
            try {
                DatagramPacket packet = new DatagramPacket(data, data.length, client);
                socket.send(packet);
            } catch (IOException e) {
                System.err.println("[Server] Failed to send packet to " + client + ": " + e.getMessage());
            }
        }
    }
}
