import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;


public class UDPFileClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8888;
    private static final int CHUNK_SIZE = 1024;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("        UDP FILE TRANSFER CLIENT");
        System.out.println("==================================================");

        String filePath = null;
        if (args.length > 0) {
            filePath = args[0];
        } else {
            System.out.print("[Client] Enter the path of the file to send (e.g., sample_input.txt): ");
            try (Scanner scanner = new Scanner(System.in)) {
                if (scanner.hasNextLine()) {
                    filePath = scanner.nextLine().trim();
                }
            }
        }

        if (filePath == null || filePath.isEmpty()) {
            filePath = "sample_input.txt";
            System.out.println("[Client] Defaulting to: " + filePath);
        }

        File fileToSend = new File(filePath);
        if (!fileToSend.exists() || !fileToSend.isFile()) {
            System.err.println("[Client Error] File not found or is not a regular file: " + filePath);
            return;
        }

        long fileLength = fileToSend.length();
        System.out.println("[Client] Target File : " + fileToSend.getName());
        System.out.println("[Client] File Size   : " + fileLength + " bytes");
        System.out.println("[Client] Server      : " + SERVER_HOST + ":" + SERVER_PORT);

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(5000); // 5-second socket timeout for responses
            InetAddress serverAddress = InetAddress.getByName(SERVER_HOST);

            // Step 1: Send START handshake packet with filename and size
            String startMessage = "START:" + fileToSend.getName() + ":" + fileLength;
            byte[] startBytes = startMessage.getBytes();
            DatagramPacket startPacket = new DatagramPacket(startBytes, startBytes.length, serverAddress, SERVER_PORT);
            socket.send(startPacket);
            System.out.println("\n[Client] Sent START handshake: \"" + startMessage + "\"");

            // Wait for server READY response
            byte[] respBuffer = new byte[1024];
            DatagramPacket readyPacket = new DatagramPacket(respBuffer, respBuffer.length);
            socket.receive(readyPacket);
            String readyResponse = new String(readyPacket.getData(), 0, readyPacket.getLength()).trim();

            if (!readyResponse.equals("READY")) {
                System.err.println("[Client Error] Server did not acknowledge readiness: " + readyResponse);
                return;
            }
            System.out.println("[Client] Received server READY signal. Beginning file transmission...");

            // Step 2: Read file and transmit chunks
            long startTime = System.currentTimeMillis();
            int packetsSent = 0;
            long totalBytesSent = 0;

            try (FileInputStream fis = new FileInputStream(fileToSend)) {
                byte[] chunkBuffer = new byte[CHUNK_SIZE];
                int bytesRead;

                while ((bytesRead = fis.read(chunkBuffer)) != -1) {
                    DatagramPacket dataPacket = new DatagramPacket(
                            chunkBuffer,
                            bytesRead,
                            serverAddress,
                            SERVER_PORT
                    );
                    socket.send(dataPacket);
                    totalBytesSent += bytesRead;
                    packetsSent++;

                    // Tiny delay to prevent overwhelming UDP socket receive buffer
                    Thread.sleep(2);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Step 3: Send EOF packet
            byte[] eofBytes = "EOF".getBytes();
            DatagramPacket eofPacket = new DatagramPacket(eofBytes, eofBytes.length, serverAddress, SERVER_PORT);
            socket.send(eofPacket);
            System.out.println("[Client] Sent EOF marker packet.");

            // Step 4: Receive final confirmation ACK from server
            DatagramPacket ackPacket = new DatagramPacket(respBuffer, respBuffer.length);
            socket.receive(ackPacket);
            String ackResponse = new String(ackPacket.getData(), 0, ackPacket.getLength()).trim();

            long elapsedTime = System.currentTimeMillis() - startTime;
            System.out.println("\n[Client] === Transmission Summary ===");
            System.out.println("[Client] Total Datagrams Sent : " + packetsSent);
            System.out.println("[Client] Total Bytes Sent     : " + totalBytesSent + " bytes");
            System.out.println("[Client] Transmission Time    : " + elapsedTime + " ms");
            System.out.println("[Client] Server Final Response: " + ackResponse);
            System.out.println("[Client] File transfer complete!");

        } catch (IOException e) {
            System.err.println("[Client Error] File transfer failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
