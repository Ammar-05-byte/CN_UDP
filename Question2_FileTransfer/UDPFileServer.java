
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPFileServer {
    private static final int PORT = 8888;
    private static final int BUFFER_SIZE = 4096;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("        UDP FILE TRANSFER SERVER (PORT " + PORT + ")");
        System.out.println("==================================================");

        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            System.out.println("[Server] Listening on UDP port " + PORT + " for incoming files...\n");

            while (true) {
                // 1. Wait for START packet containing filename
                DatagramPacket initialPacket = new DatagramPacket(buffer, buffer.length);
                socket.receive(initialPacket);

                String initialMsg = new String(initialPacket.getData(), 0, initialPacket.getLength()).trim();
                if (!initialMsg.startsWith("START:")) {
                    System.out.println("[Server] Unexpected packet received: " + initialMsg);
                    continue;
                }

                String[] parts = initialMsg.split(":", 3);
                String originalFilename = parts[1];
                long expectedSize = parts.length > 2 ? Long.parseLong(parts[2]) : -1;

                InetAddress clientAddress = initialPacket.getAddress();
                int clientPort = initialPacket.getPort();

                String outputFilename = "received_" + new File(originalFilename).getName();
                System.out.println("[Server] Incoming file transfer detected from " + clientAddress.getHostAddress() + ":" + clientPort);
                System.out.println("[Server] File Name     : " + originalFilename);
                System.out.println("[Server] Target Output : " + outputFilename);
                System.out.println("[Server] Expected Size : " + (expectedSize >= 0 ? (expectedSize + " bytes") : "Unknown"));

                // Send READY acknowledgment to client
                byte[] readyBytes = "READY".getBytes();
                DatagramPacket readyPacket = new DatagramPacket(readyBytes, readyBytes.length, clientAddress, clientPort);
                socket.send(readyPacket);
                System.out.println("[Server] Sent READY acknowledgment to client.");

                // 2. Receive file chunks until END marker is received
                File outputFile = new File(outputFilename);
                long totalBytesReceived = 0;
                int packetCount = 0;

                try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                    while (true) {
                        DatagramPacket dataPacket = new DatagramPacket(buffer, buffer.length);
                        socket.receive(dataPacket);

                        int bytesInPacket = dataPacket.getLength();
                        String checkEnd = new String(dataPacket.getData(), 0, bytesInPacket).trim();

                        if (checkEnd.equals("EOF") || checkEnd.equals("END")) {
                            System.out.println("[Server] Received EOF marker from client.");
                            break;
                        }

                        // Write received datagram payload to file
                        fos.write(dataPacket.getData(), 0, bytesInPacket);
                        totalBytesReceived += bytesInPacket;
                        packetCount++;
                    }
                    fos.flush();
                }

                System.out.println("\n[Server] === Transfer Completed Successfully ===");
                System.out.println("[Server] Total Packets Received : " + packetCount);
                System.out.println("[Server] Total Bytes Written    : " + totalBytesReceived + " bytes");
                System.out.println("[Server] Saved File Location    : " + outputFile.getAbsolutePath());

                // Send Final ACK to client
                String ackMessage = "SUCCESS:" + totalBytesReceived;
                byte[] ackBytes = ackMessage.getBytes();
                DatagramPacket ackPacket = new DatagramPacket(ackBytes, ackBytes.length, clientAddress, clientPort);
                socket.send(ackPacket);
                System.out.println("[Server] Sent SUCCESS confirmation to client.\n");
            }
        } catch (IOException e) {
            System.err.println("[Server Error] " + e.getMessage());
            e.printStackTrace();
        }
    }
}
