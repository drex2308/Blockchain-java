# Customer Blockchain Java Program

Welcome to the Customer Blockchain Java program! This project is an exploration of various advanced concepts in blockchain technology, TCP server-client communication, JSON serialization/deserialization, and digital signature functionality. Dive in to see what I've learned and implemented.

## 🚀 Project Overview

The Customer Blockchain project comprises several key components:
- **Blockchain Implementation**: A straightforward blockchain system to add, verify, and view transactions.
- **TCP Server and Client**: A robust server-client architecture for network interactions.
- **JSON Serialization/Deserialization**: Efficient data interchange using JSON.
- **Digital Signatures**: Ensuring transaction authenticity and integrity.

## 📂 Project Structure

### Blockchain Implementation

#### Block.java

This class is the backbone of our blockchain, representing each block with:
- **index**: The block's position in the chain.
- **timestamp**: The creation time of the block.
- **data**: The transaction information.
- **previousHash**: The hash of the preceding block.
- **nonce**: A unique number ensuring the block's hash uniqueness.
- **difficulty**: The complexity level for the proof of work.

**Key Methods**:
- `calculateHash()`: Computes the SHA-256 hash of the block.
- `proofOfWork()`: Executes proof of work by finding a suitable hash.
- `toString()`: Converts the block to a JSON format string.

#### BlockChain.java

This class manages the blockchain, providing functionalities such as:
- **Adding Blocks**: Incorporating new transactions into the blockchain.
- **Hash Calculation and Verification**: Maintaining the blockchain's integrity.
- **Integrity Checks**: Verifying the entire blockchain for any discrepancies.

**Key Methods**:
- `addBlock(Block newBlock)`: Adds a new block to the chain.
- `isChainValid()`: Validates the entire blockchain.
- `repairChain()`: Repairs the blockchain by recomputing hashes from the first corrupted block.
- `toString()`: Outputs the current blockchain state in a readable format.

### TCP Server and Client

Implementing a TCP server and client to interact with the blockchain over a network adds a practical edge to this project.

#### ServerTCP.java

The server handles client requests to interact with the blockchain. It:
- **Processes Requests**: Handles various client requests like adding blocks, verifying the chain, and fetching blockchain status.
- **Sends Responses**: Provides appropriate responses to client queries.

#### ClientTCP.java

The client application communicates with the server to perform blockchain operations. It:
- **Sends Requests**: Requests operations like adding transactions and viewing the blockchain.
- **Receives Responses**: Processes responses from the server.

### JSON Serialization/Deserialization

Utilizing JSON for data interchange ensures smooth communication between the client and server.

**Functionalities**:
- **Serialization**: Converts blockchain objects to JSON for network transmission.
- **Deserialization**: Converts received JSON data back into blockchain objects.

### Digital Signatures

Incorporating digital signatures ensures the security and integrity of transactions.

**Functionalities**:
- **Signing Transactions**: Uses private keys to sign transaction data.
- **Verifying Signatures**: Uses public keys to verify transaction authenticity.

## 🛠️ Getting Started

### Prerequisites

- Java Development Kit (JDK) 21.0.2 or later
- IntelliJ IDEA or any Java IDE

### Installation

1. Clone this repository to your local machine.
2. Open the project in your Java IDE.
3. Build and run the project.

### Usage

#### Running the Server

To start the TCP server:
1. Navigate to `ServerTCP.java`.
2. Run the `main` method to start the server.

#### Running the Client

To start the client application:
1. Navigate to `ClientTCP.java`.
2. Run the `main` method to start the client.
3. Follow the on-screen prompts to interact with the blockchain.

---

This project has been an enriching journey into blockchain technology, server-client architectures, and data security. It showcases the practical application of theoretical concepts, making it a valuable learning experience.
