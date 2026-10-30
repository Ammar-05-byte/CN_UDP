# Computer Networks Lab (BITE305P) — Assessment UDP


---

## Assessment Overview & Structure

This repository contains fully verified, standard-compliant Java implementations for all 5 questions in the BITE305P UDP Lab Assessment.

```text
CN code/
├── Question1_EchoServer/
│   ├── UDPEchoServer.java              # Echo server (Port 9876)
│   └── UDPEchoClient.java              # Interactive client
│
├── Question2_FileTransfer/
│   ├── UDPFileServer.java              # File transfer server (Port 8888)
│   ├── UDPFileClient.java              # File transfer client
│   └── sample_input.txt                # Sample payload file
│
├── Question3_OneToOneChat/
│   ├── UDPOneToOneChatServer.java      # Full-duplex real-time server (Port 5000)
│   └── UDPOneToOneChatClient.java      # Full-duplex real-time client
│
├── Question4_MultiClientChat/
│   ├── UDPMultiChatServer.java         # Central broadcast server (Port 6000)
│   └── UDPMultiChatClient.java         # Multi-client chat participant
│
├── Question5_DNS/
│   ├── UDPDNSServer.java               # DNS resolver server (Port 8053)
│   └── UDPDNSClient.java               # DNS query client
│
├── run_assessment_tests.sh             # Master automated verification script
├── build_submission_documents.py       # PDF & DOCX generation script
├── ASSESSMENT_UDP_SUBMISSION.pdf       # Ready-to-submit formatted PDF (19 pages)
├── ASSESSMENT_UDP_SUBMISSION.docx      # Editable Word document
└── README.md                           # Documentation & execution guide
```

---

## Quick Start: Run All Automated Tests

To compile and verify all 5 questions in one command:
```bash
./run_assessment_tests.sh
```

---

## Interactive Manual Execution (Step-by-Step)

### Question 1: UDP Echo Server
Open two terminal windows:
- **Terminal 1 (Server):**
  ```bash
  cd Question1_EchoServer
  javac UDPEchoServer.java
  java UDPEchoServer
  ```
- **Terminal 2 (Client):**
  ```bash
  cd Question1_EchoServer
  javac UDPEchoClient.java
  java UDPEchoClient
  ```

---

### Question 2: UDP File Transfer
- **Terminal 1 (Server):**
  ```bash
  cd Question2_FileTransfer
  javac UDPFileServer.java
  java UDPFileServer
  ```
- **Terminal 2 (Client):**
  ```bash
  cd Question2_FileTransfer
  javac UDPFileClient.java
  java UDPFileClient sample_input.txt
  ```
*Output:* File `received_sample_input.txt` will be created and verified identical to `sample_input.txt`.

---

### Question 3: Real-Time One-to-One Chat
- **Terminal 1 (Server):**
  ```bash
  cd Question3_OneToOneChat
  javac UDPOneToOneChatServer.java
  java UDPOneToOneChatServer
  ```
- **Terminal 2 (Client):**
  ```bash
  cd Question3_OneToOneChat
  javac UDPOneToOneChatClient.java
  java UDPOneToOneChatClient
  ```
*Both sides can type and receive messages simultaneously via independent background receiver threads.*

---

### Question 4: Multi-Client Chat with Broadcasting
- **Terminal 1 (Broadcast Server):**
  ```bash
  cd Question4_MultiClientChat
  javac UDPMultiChatServer.java
  java UDPMultiChatServer
  ```
- **Terminal 2 (Client 1 - Alice):**
  ```bash
  cd Question4_MultiClientChat
  javac UDPMultiChatClient.java
  java UDPMultiChatClient Alice
  ```
- **Terminal 3 (Client 2 - Bob):**
  ```bash
  cd Question4_MultiClientChat
  java UDPMultiChatClient Bob
  ```

---

### Question 5: UDP DNS Resolver
- **Terminal 1 (DNS Server):**
  ```bash
  cd Question5_DNS
  javac UDPDNSServer.java
  java UDPDNSServer
  ```
- **Terminal 2 (DNS Client):**
  ```bash
  cd Question5_DNS
  javac UDPDNSClient.java
  java UDPDNSClient
  ```
---


