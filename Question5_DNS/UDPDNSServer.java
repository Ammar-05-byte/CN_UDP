import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;


public class UDPDNSServer {
    private static final int PORT = 8053;
    private static final int BUFFER_SIZE = 1024;

    // Local authoritative DNS lookup table (Domain -> IP Mapping)
    private static final Map<String, String> dnsTable = new HashMap<>();

    static {
        dnsTable.put("google.com", "142.250.190.46");
        dnsTable.put("www.google.com", "142.250.190.46");
        dnsTable.put("vit.ac.in", "115.242.228.10");
        dnsTable.put("www.vit.ac.in", "115.242.228.10");
        dnsTable.put("github.com", "140.82.121.3");
        dnsTable.put("www.github.com", "140.82.121.3");
        dnsTable.put("yahoo.com", "74.6.143.25");
        dnsTable.put("www.yahoo.com", "74.6.143.25");
        dnsTable.put("wikipedia.org", "208.80.154.224");
        dnsTable.put("www.wikipedia.org", "208.80.154.224");
        dnsTable.put("localhost", "127.0.0.1");
    }

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("          UDP DNS SERVER (PORT " + PORT + ")");
        System.out.println("==================================================");
        System.out.println("[DNS Server] Initialized with " + dnsTable.size() + " pre-configured zone records.");
        System.out.println("[DNS Server] Waiting for DNS query datagrams...\n");

        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            byte[] receiveBuffer = new byte[BUFFER_SIZE];

            while (true) {
                DatagramPacket queryPacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                socket.receive(queryPacket);

                String rawQuery = new String(queryPacket.getData(), 0, queryPacket.getLength()).trim();
                InetAddress clientAddress = queryPacket.getAddress();
                int clientPort = queryPacket.getPort();

                System.out.println("[DNS Server] Query received from " + clientAddress.getHostAddress() + ":" + clientPort);
                System.out.println("[DNS Server] Requested Target : \"" + rawQuery + "\"");

                // Clean and extract host name from raw URL
                String hostName = extractHost(rawQuery);
                String resolvedIp;
                String resolutionSource;

                // 1. Check local DNS table
                if (dnsTable.containsKey(hostName.toLowerCase())) {
                    resolvedIp = dnsTable.get(hostName.toLowerCase());
                    resolutionSource = "Local DNS Zone Table (Authoritative)";
                } else {
                    // 2. Fallback to upstream dynamic resolution
                    try {
                        InetAddress dynamicAddress = InetAddress.getByName(hostName);
                        resolvedIp = dynamicAddress.getHostAddress();
                        resolutionSource = "Upstream Recursive DNS Resolver";
                    } catch (UnknownHostException e) {
                        resolvedIp = "ERROR: Host Not Found / Unresolved Domain";
                        resolutionSource = "Resolution Failed";
                    }
                }

                String response = "IP: " + resolvedIp + " [" + resolutionSource + "]";
                System.out.println("[DNS Server] Resolved To     : " + resolvedIp);
                System.out.println("[DNS Server] Resolution Type : " + resolutionSource);

                // Send resolved IP back to client
                byte[] responseBytes = response.getBytes();
                DatagramPacket responsePacket = new DatagramPacket(
                        responseBytes,
                        responseBytes.length,
                        clientAddress,
                        clientPort
                );
                socket.send(responsePacket);
                System.out.println("[DNS Server] Dispatched response datagram back to client.\n");
            }

        } catch (IOException e) {
            System.err.println("[DNS Server Error] " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Sanitizes a URL string down to its pure domain/hostname.
     * E.g., "https://www.google.com/search?q=test" -> "www.google.com"
     */
    private static String extractHost(String raw) {
        String clean = raw.trim();
        if (clean.startsWith("http://")) {
            clean = clean.substring(7);
        } else if (clean.startsWith("https://")) {
            clean = clean.substring(8);
        }
        int slashIdx = clean.indexOf('/');
        if (slashIdx != -1) {
            clean = clean.substring(0, slashIdx);
        }
        int colonIdx = clean.indexOf(':');
        if (colonIdx != -1) {
            clean = clean.substring(0, colonIdx);
        }
        return clean;
    }
}
