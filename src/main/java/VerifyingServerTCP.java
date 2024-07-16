/**
 * Author: Dhanush Venkataramu
 * Last Modified: 03/17/2024
 *
 * This program serves as a TCP server for a blockchain application.
 * It receives requests from clients, processes them, and sends back responses accordingly.
 * The server maintains a blockchain and performs various operations such as adding blocks,
 * verifying the chain, viewing the blockchain, corrupting the chain, and repairing it.
 * It also verifies signatures and client IDs to ensure the integrity of requests.
 */
// Imports for necessary libraries and classes
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.ArrayList;

public class VerifyingServerTCP {
    public static void main(String[] args) {
        // Initialize the client socket variable
        Socket clientSocket = null;
        // Create a scanner object for user input
        Scanner scanner = new Scanner(System.in).useDelimiter("\\n");
        // Create a new blockchain instance
        BlockChain myChain = new BlockChain();
        // Create the genesis block
        Block genesis = new Block(0, myChain.getTime(), "Genesis", 2);
        // Set the previous hash of the genesis block
        genesis.setPreviousHash("");
        // Add the genesis block to the blockchain
        myChain.addBlock(genesis);
        // Compute the hashes per second for the blockchain
        myChain.computeHashesPerSecond();
        try {
            // Define the server port
            int serverPort = 7777;
            // Flag to indicate if there's no client connected
            boolean noClient = true;
            // Create a server socket
            ServerSocket listenSocket = new ServerSocket(serverPort);
            // Display server status
            System.out.println("Blockchain server running..");
            while (true) {
                // Check if there's no client connected
                if (noClient) {
                    // Accept incoming client connection
                    clientSocket = listenSocket.accept();
                    // Update the flag to indicate client connection
                    noClient = false;
                }
                // Receive request from the client
                RequestMessage request = receive(clientSocket);
                // Create a response message object
                ResponseMessage response = new ResponseMessage();
                // Display visitor's public key
                System.out.println("Visitor Public key: ");
                // Display exponent of the public key
                System.out.println("E: " + request.getE());
                // Display modulus of the public key
                System.out.println("N: " + request.getN());
                // Check if the request signature is valid
                if (!checkSign(request)) {
                    // Display verification failure message
                    System.out.println("Verification Failed !");
                    // Set response type to error
                    response.setResponseType("Error");
                    // Set error response
                    response.setResponse("Error in request, Verification Failed");
                    // Send error response to client
                    send(clientSocket, response);
                    // Continue to the next iteration of the loop
                    continue;
                }
                // Display signature verification success
                System.out.println("Signature Verified !");
                // Initialize a string for response
                String resStr = "";
                // Check the type of request
                switch (request.getRequestType()) {
                    // If request is to get basic view
                    case "getBasicView":
                        // Construct the response string with chain information
                        resStr += "Current size of chain: " + myChain.getChainSize();
                        resStr += "\nDifficulty of most recent block: " + myChain.getLatestBlock().getDifficulty();
                        resStr += "\nTotal difficulty for all blocks: " + myChain.getTotalDifficulty();
                        resStr += "\nExperimented with 2,000,000 hashes.";
                        resStr += "\nApproximate hashes per second on this machine: " + myChain.getHashesPerSecond();
                        resStr += "\nExpected total hashes required for the whole chain: " + myChain.getTotalExpectedHashes();
                        resStr += "\nNonce for most recent block: " + myChain.getLatestBlock().getNonce();
                        resStr += "\nChain hash: " + myChain.getChainHash();
                        // Set the response type in the response message
                        response.setResponseType(request.getRequestType());
                        // Set the response message
                        response.setResponse(resStr);
                        // Send the response to the client
                        send(clientSocket, response);
                        // Break out of the switch statement
                        break;
                    case "addBlock":
                        // Extract transaction and difficulty from the request
                        String tx = request.getVar2();
                        int diff = Integer.parseInt(request.getVar1());
                        // Create a new block with the provided transaction and difficulty
                        Block newBlock = new Block(myChain.getLatestBlock().getIndex() + 1, myChain.getTime(), tx, diff);
                        // Set the previous hash of the new block to the chain hash
                        newBlock.setPreviousHash(myChain.getChainHash());
                        // Add the new block to the blockchain
                        myChain.addBlock(newBlock);
                        // Set the response type in the response message
                        response.setResponseType(request.getRequestType());
                        // Set the response message
                        response.setResponse("Successfully added block");
                        // Send the response to the client
                        send(clientSocket, response);
                        // Break out of the switch statement
                        break;
                    case "verifyChain":
                        // Verify the integrity of the blockchain
                        String res = myChain.isChainValid();
                        // Set the response type in the response message
                        response.setResponseType(request.getRequestType());
                        // Set the response message
                        response.setResponse(res);
                        // Send the response to the client
                        send(clientSocket, response);
                        // Break out of the switch statement
                        break;
                    case "getFullView":
                        // Get the full view of the blockchain
                        response.setResponseType(request.getRequestType());
                        // Set the response message to the string representation of the blockchain
                        response.setResponse(myChain.toString());
                        // Send the response to the client
                        send(clientSocket, response);
                        // Break out of the switch statement
                        break;
                    case "corruptChain":
                        // Extract new transaction and block ID from the request
                        String newTx = request.getVar2();
                        int id = Integer.parseInt(request.getVar1());
                        // Set the response type in the response message
                        response.setResponseType(request.getRequestType());
                        // If the provided block ID is invalid
                        if (id > myChain.getChainSize() - 1) {
                            // Set an error response
                            response.setResponse("Please enter valid ID");
                            // Send the response to the client
                            send(clientSocket, response);
                            // Break out of the switch statement
                            break;
                        }
                        // Modify the data of the specified block
                        myChain.getBlock(id).setData(newTx);
                        // Set the response message
                        response.setResponse("Block " + id + " now holds " + myChain.getBlock(id).getData());
                        // Send the response to the client
                        send(clientSocket, response);
                        // Break out of the switch statement
                        break;
                    case "repairChain":
                        // Repair the blockchain
                        myChain.repairChain();
                        // Set the response type in the response message
                        response.setResponseType(request.getRequestType());
                        // Set the response message
                        response.setResponse("Repaired Successfully");
                        // Send the response to the client
                        send(clientSocket, response);
                        // Break out of the switch statement
                        break;
                    case "clientExit":
                        // Set the flag to indicate no client connection
                        noClient = true;
                        // Set the response type in the response message
                        response.setResponseType(request.getRequestType());
                        // Set the response message
                        response.setResponse("Ack. Server awaiting new Client :)");
                        // Send the response to the client
                        send(clientSocket, response);
                        // Break out of the switch statement
                        break;
                }
                // Display the number of blocks on the chain
                System.out.println("Number of Blocks on Chain == " + myChain.getChainSize());
            }
        } catch (IOException e) {
            // Handle IO Exception
            System.out.println("IO Exception:" + e.getMessage());
        }
    }

    /**
     * Receives a request message from the client socket.
     * @param clientSocket The client socket from which to receive the message.
     * @return The received request message.
     */
    private static RequestMessage receive(Socket clientSocket) {
        // Print a blank line
        System.out.println();
        Scanner in;
        try {
            // Create a scanner to read from the client socket input stream
            in = new Scanner(clientSocket.getInputStream());
        } catch (IOException e) {
            // Throw a runtime exception if an IO exception occurs
            throw new RuntimeException(e);
        }
        // Read the request message from the client
        String request = in.nextLine();
        // Print a message indicating a visitor
        System.out.println("We have a visitor");
        // Print the received request
        System.out.print("Request Received: ");
        System.out.println(request);
        // Deserialize the request message from JSON to RequestMessage object
        return new Gson().fromJson(request, RequestMessage.class);
    }

    /**
     * Sends a response message to the client socket.
     * @param clientSocket The client socket to which the response message will be sent.
     * @param res          The response message to send.
     */
    private static void send(Socket clientSocket, ResponseMessage res) {
        PrintWriter out = null;
        try {
            // Create a PrintWriter to write to the client socket output stream
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())));
        } catch (IOException e) {
            // Print an IO exception message if an exception occurs
            System.out.println("IO Exception:" + e.getMessage());
        }
        // Convert the response message to JSON format
        String response = new Gson().toJson(res);
        // Print the response sent message
        System.out.print("Response Sent: ");
        System.out.println(response);
        // Send the response message to the client
        out.println(response);
        out.flush();
    }

    /**
     * Checks the signature of the request message.
     * @param request The request message to check.
     * @return True if the signature is valid, false otherwise.
     */
    private static boolean checkSign(RequestMessage request) {
        // Check if the client ID is valid
        if (!checkClientID(request.getE(), request.getN(), request.getClientID())) {
            return false;
        }
        // Verify the signature of the request
        return verifySign(request);
    }

    /**
     * Checks if the provided client ID matches the computed client ID based on public key components.
     *
     * @param e        The public exponent.
     * @param n        The modulus.
     * @param clientID The client ID to compare against.
     * @return True if the client ID matches, false otherwise.
     */
    private static boolean checkClientID(BigInteger e, BigInteger n, String clientID) {
        // Initialize a message digest for SHA-256
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
        // Concatenate e and n, and compute their hash
        md.update((e.toString() + n.toString()).getBytes());
        String compID = Block.bytesToHex(md.digest());
        // Extract the last 20 characters from the computed ID
        compID = compID.substring(compID.length() - 20);
        // Compare the computed ID with the provided client ID
        return compID.equals(clientID);
    }

    /**
     * Verifies the signature of the request message.
     * @param request The request message containing the signature to verify.
     * @return True if the signature is valid, false otherwise.
     * Code taken from project github, url: https://github.com/CMU-Heinz-95702/Project3
     */
    private static boolean verifySign(RequestMessage request) {
        // Convert the signature from String to BigInteger
        BigInteger encryptedSign = new BigInteger(request.getSignature());
        // Decrypt the signature using the public key components
        BigInteger decryptedSign = encryptedSign.modPow(request.getE(), request.getN());
        // Concatenate message components for hashing
        String message = request.getClientID() + request.getE() +
                request.getN() + request.getRequestType() +
                request.getVar1() + request.getVar2();
        try {
            // Compute the SHA-256 hash of the message
            byte[] bytesOfMessage = message.getBytes("UTF-8");
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] messageDigest = md.digest(bytesOfMessage);
            // Prepare the hash bytes for comparison
            byte[] extraByte = new byte[3];
            extraByte[0] = 0;
            extraByte[1] = messageDigest[0];
            extraByte[2] = messageDigest[1];
            // Convert the prepared bytes to BigInteger for comparison
            BigInteger bigIntegerToCheck = new BigInteger(extraByte);
            // Check if the computed hash matches the decrypted signature
            if (bigIntegerToCheck.compareTo(decryptedSign) == 0) {
                return true;
            }
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            System.out.println(e);
        }
        // If any exception occurs or verification fails, return false
        return false;
    }
}
class Block {
    // Instance variables
    private int index; // Index of the block
    private Timestamp timestamp; // Timestamp of when the block was created
    private String data; // Data stored in the block
    private String previousHash; // Hash of the previous block
    private BigInteger nonce; // Nonce used in proof of work
    private int difficulty; // Difficulty level for proof of work
    /**
     * Constructor for creating a new Block object.
     *
     * @param index      The index of the block.
     * @param timestamp  The timestamp of when the block was created.
     * @param data       The data to be stored in the block.
     * @param difficulty The difficulty level for proof of work.
     */
    public Block(int index, Timestamp timestamp, String data, int difficulty) {
        this.index = index;
        this.timestamp = timestamp;
        this.data = data;
        this.difficulty = difficulty;
    }
    /**
     * Getter method for retrieving the nonce of the block.
     * @return The nonce of the block.
     */
    public BigInteger getNonce() {
        return nonce;
    }
    /**
     * Getter method for retrieving the difficulty of the block.
     * @return The difficulty of the block.
     */
    public int getDifficulty() {
        return difficulty;
    }
    /**
     * Getter method for retrieving the data of the block.
     * @return The data of the block.
     */
    public String getData() {
        return data;
    }
    /**
     * Getter method for retrieving the previous hash of the block.
     * @return The previous hash of the block.
     */
    public String getPreviousHash() {
        return previousHash;
    }
    /**
     * Getter method for retrieving the index of the block.
     * @return The index of the block.
     */
    public int getIndex() {
        return index;
    }
    /**
     * Getter method for retrieving the timestamp of the block.
     * @return The timestamp of the block.
     */
    public Timestamp getTimestamp() {
        return timestamp;
    }
    /**
     * Setter method for updating the data of the block.
     * @param data The new data to be stored in the block.
     */
    public void setData(String data) {
        this.data = data;
    }
    /**
     * Setter method for updating the difficulty of the block.
     * @param difficulty The new difficulty level for proof of work.
     */
    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }
    /**
     * Setter method for updating the index of the block.
     * @param index The new index of the block.
     */
    public void setIndex(int index) {
        this.index = index;
    }
    /**
     * Setter method for updating the previous hash of the block.
     * @param previousHash The new previous hash of the block.
     */
    public void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }
    /**
     * Setter method for updating the timestamp of the block.
     * @param timestamp The new timestamp of the block.
     */
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
    /**
     * Method to calculate the hash of the block using SHA-256.
     * @return The hash of the block.
     */
    public String calculateHash() {
        // Initialize MessageDigest object
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            // Handle exception
            throw new RuntimeException(e);
        }
        // Concatenate block data for hashing
        String hashData = new StringBuilder().append(index)
                .append(timestamp)
                .append(data)
                .append(previousHash)
                .append(nonce.toString())
                .append(difficulty)
                .toString();
        // Update MessageDigest with hashData bytes
        md.update(hashData.getBytes());
        // Convert MessageDigest digest to hexadecimal string
        return bytesToHex(md.digest());
    }
    /**
     * Method to perform proof of work by finding a hash that meets the target difficulty.
     * @return The hash of the block after proof of work.
     */
    public String proofOfWork() {
        // Initialize nonce to 0
        nonce = new BigInteger("0");
        // Calculate initial hash
        String hashData = calculateHash();
        // Define target string based on block difficulty
        String target = getTargetString(this.getDifficulty());
        // Loop until hash meets target difficulty
        while (!hashData.startsWith(target)) {
            // Increment nonce
            nonce = nonce.add(BigInteger.ONE);
            // Recalculate hash
            hashData = calculateHash();
        }
        // Return hash after proof of work
        return hashData;
    }
    /**
     * Method to convert the block to a JSON formatted string.
     * @return JSON representation of the block.
     */
    @Override
    public String toString() {
        // Create a GsonBuilder object to configure JSON serialization.
        // part of GsonBuilder code taken from Stackoverflow, url:https://stackoverflow.com/questions/14939395/does-gson-mess-up-timestamp-variables
        return new GsonBuilder().setDateFormat("yyyy-MM-dd hh:mm:ss.S").create().toJson(this);
    }
    /**
     * Method to generate a target string based on the block difficulty.
     * @param diff The difficulty level.
     * @return The target string.
     */
    private static String getTargetString(int diff) {
        // Create a StringBuilder object to build the target string.
        StringBuilder targetBuild = new StringBuilder();
        // Loop until the number of leading zeros is equal to the specified difficulty.
        while (diff != 0) {
            // Append '0' to the target string.
            targetBuild.append("0");
            // Decrement the difficulty.
            diff--;
        }
        // Return the target string.
        return targetBuild.toString();
    }
    // Array representing hexadecimal characters
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    /**
     * Method to convert an array of bytes to a hexadecimal string.
     * @param bytes The array of bytes to be converted.
     * @return The hexadecimal representation of the input byte array.
     * Code taken from stackOverflow, url:https://stackoverflow.com/questions/9655181/java-convert-a-byte-array-to-a-hex-string
     */
    static String bytesToHex(byte[] bytes) {
        // Create a character array to store hexadecimal characters.
        char[] hexChars = new char[bytes.length * 2];
        // Iterate over each byte in the input array.
        for (int j = 0; j < bytes.length; j++) {
            // Convert the byte to an integer and mask it to ensure only the last 8 bits are considered.
            int v = bytes[j] & 0xFF;
            // Retrieve the hexadecimal representation of the upper 4 bits of the byte.
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            // Retrieve the hexadecimal representation of the lower 4 bits of the byte.
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        // Create and return a new string from the character array.
        return new String(hexChars);
    }
}

class BlockChain {
    // ArrayList holding all the blocks in the blockchain.
    private ArrayList<Block> blocks;
    // String storing the current hash of the blockchain.
    private String chainHash;
    // Integer tracking the system's hash rate (hashes per second).
    private int hashesPerSecond;
    /**
     * Constructs a new blockchain instance.
     */
    public BlockChain() {
        // Initialize the ArrayList to hold the blockchain's blocks.
        blocks = new ArrayList<>(0);
        // Initialize the hash rate (hashes per second) to zero.
        hashesPerSecond = 0;
        // Initialize the blockchain's overall hash to an empty string.
        chainHash = "";
    }
    /**
     * Retrieves the current hash of the blockchain.
     * @return A string representing the current blockchain hash.
     */
    public String getChainHash() {
        // Return the current blockchain hash.
        return chainHash;
    }

    /**
     * Retrieves the current rate of hash calculations (hashes per second).
     * @return The current hash rate as an integer.
     */
    public int getHashesPerSecond() {
        // Return the current hash rate.
        return hashesPerSecond;
    }

    /**
     * Retrieves the current system time.
     * @return A Timestamp object representing the current system time.
     */
    public Timestamp getTime() {
        // Return the current system time as a Timestamp object.
        return new Timestamp(System.currentTimeMillis());
    }
    /**
     * Retrieves the latest block in the blockchain.
     * @return The most recently added Block object.
     */
    public Block getLatestBlock() {
        // Return the last Block in the ArrayList.
        return blocks.get(blocks.size() - 1);
    }
    /**
     * Retrieves a specific block from the blockchain.
     * @param i The index of the block in the blockchain.
     * @return The Block object at the specified index.
     */
    public Block getBlock(int i) {
        // Return the Block at the specified index.
        return blocks.get(i);
    }

    /**
     * Counts the total number of blocks in the blockchain.
     * @return The total number of blocks in the blockchain as an integer.
     */
    public int getChainSize() {
        // Return the size of the blockchain (number of blocks).
        return blocks.size();
    }
    /**
     * Calculates the total difficulty of all blocks in the blockchain.
     * @return The total difficulty as an integer.
     */
    public int getTotalDifficulty() {
        // Initialize the total difficulty counter.
        int totalDiff = 0;
        // Add the difficulty of each block to the total difficulty.
        for (Block block: blocks) {
            totalDiff += block.getDifficulty();
        }
        // Return the total difficulty.
        return totalDiff;
    }
    /**
     * Calculates the total expected number of hashes based on the difficulty of each block.
     * @return The total expected hashes as a double.
     */
    public double getTotalExpectedHashes() {
        // Initialize the total expected hashes.
        double totalHash = 0.0000;
        // Add the expected hashes for each block based on its difficulty.
        for (Block block: blocks) {
            totalHash += Math.pow(16, block.getDifficulty());
        }
        // Return the total expected hashes.
        return totalHash;
    }
    /**
     * Computes and updates the system's hash rate (hashes per second).
     */
    public void computeHashesPerSecond() {
        // Declare a variable for the MessageDigest.
        MessageDigest md = null;
        try {
            // Attempt to get an instance of the SHA-256 hash function.
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            // If SHA-256 is not available, throw a runtime exception.
            throw new RuntimeException(e);
        }
        // Data to be used in calculating the hash rate.
        String hashData = "00000000";
        // Set the number of hashes to compute for the benchmark.
        int counter = 2000000;
        // Record the start time of the benchmark.
        long startTime = System.currentTimeMillis();
        // Prepare the hash data for computation.
        md.update(hashData.getBytes());
        // Compute the specified number of hashes.
        while(counter != 0) {
            // Perform a single hash computation.
            md.digest();
            // Decrement the counter after each hash computation.
            counter--;
        }
        // Record the end time of the benchmark.
        long endTime = System.currentTimeMillis();
        // Calculate and update the hash rate based on the benchmark results.
        hashesPerSecond = (int) (2000000000 / (endTime - startTime));
    }
    /**
     * Adds a new block to the blockchain.
     * @param newBlock The block to be added.
     *
     * The runtime complexity of this method depends on the number
     * the difficulty level supplied by the user. As the difficulty
     * level increases, the systems takes longer time.
     * With difficulty level above 5, we observed an overall increase in
     * the time required by the system to find the "true hash" via nonce, 10 seconds
     * for difficulty 6 amd 7, if difficult yis 8 and above the system takes minutes to
     * complete this operation.
     */
    public void addBlock(Block newBlock) {
        // Check if the new block is not the first block in the blockchain.
        if(newBlock.getIndex() != 0) {
            // Set the previous hash of the new block to the hash of the latest block in the blockchain.
            newBlock.setPreviousHash(blocks.get(blocks.size() - 1).calculateHash());
        }
        // Perform the proof of work for the new block and update the chain hash.
        chainHash = newBlock.proofOfWork();
        // Add the new block to the blockchain.
        blocks.add(newBlock);
    }
    /**
     * Validates the integrity of the blockchain.
     * @return A string indicating whether the blockchain is valid.
     *
     * The runtime complexity of this method primarily depends on the
     * number of blocks in the blockchain. The complexity remains relatively
     * constant regardless of the difficulty level of the proof-of-work algorithm.
     * This is one of the key functions of blockchain, easy to verify.
     * As the number of blocks in the blockchain increases, the time taken
     * to validate entire blockchain also increases. However, the difficulty
     * level of the proof-of-work algorithm does not impact the overall
     * execution time of this method.
     */
    public String isChainValid() {
        // Special case: check if the blockchain only contains the genesis block.
        if (blocks.size() == 1) {
            // Retrieve the genesis block.
            Block genesis = blocks.get(0);
            // Calculate the target string based on the difficulty of the genesis block.
            String target = getTargetString(genesis);
            // Calculate the hash of the genesis block.
            String hashData = genesis.calculateHash();
            // Check if the genesis block's hash starts with the target string and matches the chain hash.
            if (hashData.startsWith(target) && chainHash.equals(hashData)) {
                // The blockchain is valid.
                return "Chain verification: TRUE";
            } else {
                // The genesis block is corrupted.
                return "Chain verification: FALSE\nGenesis Node corrupted, Improper hash on node 0 Does not begin with " + target;
            }
        }
        // Validate all other blocks in the blockchain.
        for (int i = 1; i < blocks.size(); i++) {
            // Retrieve the current block and its previous block.
            Block currentBlock = blocks.get(i);
            Block previousBlock = blocks.get(i - 1);
            // Check if the current block's previous hash matches the hash of the previous block.
            if (!previousBlock.calculateHash().equals(currentBlock.getPreviousHash())) {
                // The link between the current block and its predecessor is invalid.
                return "Chain verification: FALSE\nImproper previousHash on node " + i + " Does not match with previous node hash";
            }
            // Check if the hash of the current block starts with the target string based on its difficulty.
            if (!currentBlock.calculateHash().startsWith(getTargetString(currentBlock))) {
                // The current block's hash does not meet the required difficulty.
                return "Chain verification: FALSE\nNode corrupted, Improper hash on node " + i + " Does not begin with: " + getTargetString(currentBlock);
            }
        }
        // Finally, check if the stored chain hash matches the hash of the last block.
        if (!chainHash.equals(getLatestBlock().calculateHash())) {
            // The stored chain hash is incorrect.
            return "Chain verification: FALSE\nImproper chainHash stored in BlockChain: " + chainHash;
        }
        // The blockchain is valid.
        return "Chain verification: TRUE";
    }
    /**
     * Attempts to repair the blockchain by re-computing hashes starting from the first corrupted block.
     *
     * The runtime complexity of this method depends on the number
     * of blocks in the blockchain and the difficulty level of the
     * proof-of-work algorithm. As the difficulty level increases,
     * the time taken to repair increases. With difficulty level above 5
     * we observed an overall increase in the time required by
     * the system to find the "true hash" via nonce. 10 seconds
     *  for difficulty 6 amd 7, if difficult yis 8 and above the
     *  system takes minutes to complete this operation.Blocks after the
     * corrupted block needs to be rehashed and proof of work re-computed
     * resulting in longer execution times for this method. Additionally,
     * the number of corrupted blocks in the blockchain does not affect the
     * overall execution time, as once a corrupted block is found, all the
     * blocks after it in the chain needs to re-compute proof-of-work.
     */
    public void repairChain() {
        // Flag to indicate whether a corruption has been found.
        boolean corruptFlag = false;
        // Iterate through all blocks in the blockchain.
        for (int i = 0; i < blocks.size(); i++) {
            // Retrieve the current block.
            Block currentBlock = blocks.get(i);
            // If a corruption has been detected and we're not at the first block, update the previous hash.
            if(i != 0 && corruptFlag) {
                // Set the previous hash of the current block to the hash of the previous block.
                currentBlock.setPreviousHash(blocks.get(i - 1).calculateHash());
                // Recompute the proof of work for the current block.
                currentBlock.proofOfWork();
            }
            // Check if the block is corrupted (its hash does not start with the target string).
            if (!currentBlock.calculateHash().startsWith(getTargetString(currentBlock)) && !corruptFlag) {
                // Perform proof of work to repair the corrupted block.
                currentBlock.proofOfWork();
                // Set the corruption flag to true as we've encountered a corruption.
                corruptFlag = true;
            }
        }
        // Update the chain hash to the hash of the latest block after repair.
        chainHash = this.getLatestBlock().calculateHash();
    }
    /**
     * Converts the current state of the blockchain into a human-readable string format.
     * @return A string representation of the blockchain.
     */
    public String toString() {
        // Create a new Display object to format the blockchain data.
        Display display = new Display();
        // Set the current chain hash in the display object.
        display.setChainHash(this.getChainHash());
        // Set the current list of blocks in the display object.
        display.setDsChain(blocks);
        // Convert the display object to a formatted JSON string.
        return new GsonBuilder().setDateFormat("yyyy-MM-dd hh:mm:ss.S")
                .setPrettyPrinting().create().toJson(display);
    }
    /**
     * Builds a target string of zeroes for mining based on difficulty level.
     * @param blk The block for which to build the target string.
     * @return A string consisting of '0' repeated 'difficulty' times.
     */
    private static String getTargetString(Block blk) {
        // Create a new StringBuilder for building the target string.
        StringBuilder targetBuild = new StringBuilder();
        // Get the difficulty level of the block.
        int diff = blk.getDifficulty();
        // Append '0' to the target string 'difficulty' times.
        while(diff != 0) {
            targetBuild.append("0");
            diff--;
        }
        // Return the completed target string.
        return targetBuild.toString();
    }
    /**
     * The Display inner class for formatting the blockchain information.
     */
    class Display {
        // List holding all the blocks for display.
        private ArrayList<Block> ds_chain;
        // String representing the hash of the entire displayed chain.
        private String chainHash;
        /**
         * Sets the current chain hash for display.
         * @param chainHash The hash of the current chain.
         */
        public void setChainHash(String chainHash) {
            // Set the chain hash for the display.
            this.chainHash = chainHash;
        }
        /**
         * Sets the list of blocks for display.
         * @param ds_chain The list of blocks to display.
         */
        public void setDsChain(ArrayList<Block> ds_chain) {
            // Set the list of blocks for the display.
            this.ds_chain = ds_chain;
        }
        /**
         * Retrieves the current chain hash for display.
         * @return The hash of the displayed chain.
         */
        public String getChainHash() {
            // Return the chain hash for the display.
            return chainHash;
        }
        /**
         * Retrieves the list of displayed blocks.
         * @return The list of blocks in the display.
         */
        public ArrayList<Block> getDs_chain() {
            // Return the list of displayed blocks.
            return ds_chain;
        }
    }
}