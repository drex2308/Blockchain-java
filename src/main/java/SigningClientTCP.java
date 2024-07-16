/**
 * Author: Dhanush Venkataramu
 * Last Modified: 03/17/2024
 *
 * Represents a TCP client for signed blockchain transactions.
 * This client interacts with a blockchain server to add transactions, verify the chain,
 * and perform other actions. It utilizes RSA encryption for secure communication
 * and signing of transactions. The program allows users to view blockchain status,
 * add transactions, verify the integrity of the blockchain, view the entire blockchain,
 * corrupt the chain for testing purposes, repair the chain to hide the corruption, and exit.
 */

// Importing necessary libraries and classes.
import com.google.gson.Gson;
import java.io.*;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.security.MessageDigest;
import java.util.Scanner;
import java.math.BigInteger;

public class SigningClientTCP {
    // Array of hexadecimal characters
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    // Generate RSA key pair
    private static BigInteger[] rsaVars =  getRSA();
    // Get unique client ID
    private static String clientID = getClientID(rsaVars);
    public static void main(String[] args) {
        // Socket for client-server communication
        Socket clientSocket = null;
        try {
            // Port number for server connection
            int serverPort = 7777;
            // Establish connection with the server
            clientSocket = new Socket("localhost", serverPort);
            // Scanner for user input
            Scanner scanner = new Scanner(System.in).useDelimiter("\\n");
            // Variable to control program execution
            boolean runVar = true;
            // Variables to measure execution time
            long startTime;
            long endTime;
            // Display client status and generated keys
            System.out.println("Client running...");
            System.out.println("Keys Generated.. ");
            System.out.println("Private Key: ");
            System.out.println("D: " + rsaVars[1].toString());
            System.out.println("N: " + rsaVars[2].toString());
            System.out.println("Public Key: ");
            System.out.println("E: " + rsaVars[0].toString());
            System.out.println("N: " + rsaVars[2].toString());
            System.out.println();
            // Main program loop
            while (runVar) {
                // Display menu and get user selection
                int userSelection = menu();
                // Create request and response objects
                RequestMessage request = new RequestMessage();
                ResponseMessage response;
                // Handle user selection
                switch (userSelection) {
                    case 0:
                        // Request to get basic view of blockchain
                        request.setRequestType("getBasicView");
                        // Transmit request and receive response
                        response = transmit(clientSocket, request);
                        // Display response
                        System.out.println(response.getResponse());
                        break;
                    case 1:
                        // Request to add a block to the blockchain
                        request.setRequestType("addBlock");
                        // Prompt user to enter difficulty
                        System.out.println("Enter Difficulty > 1");
                        // Set difficulty input
                        request.setVar1(scanner.next());
                        // Prompt user to enter transaction
                        System.out.println("Enter transaction");
                        // Set transaction input
                        request.setVar2(scanner.next());
                        // Record start time
                        startTime = System.currentTimeMillis();
                        // Transmit request and receive response
                        response = transmit(clientSocket, request);
                        // Record end time
                        endTime = System.currentTimeMillis();
                        // Display response and execution time
                        System.out.println(response.getResponse());
                        System.out.println("Total execution time to add this block was " + (endTime - startTime) + " milliseconds");
                        break;
                    case 2:
                        // Request to verify entire blockchain
                        System.out.println("Verifying entire chain");
                        // Set request type
                        request.setRequestType("verifyChain");
                        // Record start time
                        startTime = System.currentTimeMillis();
                        // Transmit request and receive response
                        response = transmit(clientSocket, request);
                        // Display response and execution time
                        System.out.println(response.getResponse());
                        endTime = System.currentTimeMillis();
                        System.out.println("Total execution time required to verify the chain was " + (endTime - startTime) + " milliseconds");
                        break;
                    case 3:
                        // Request to view the entire blockchain
                        System.out.println("View the Blockchain");
                        // Set request type
                        request.setRequestType("getFullView");
                        // Transmit request and receive response
                        response = transmit(clientSocket, request);
                        // Display response
                        System.out.println(response.getResponse());
                        break;
                    case 4:
                        // Request to corrupt a block in the blockchain
                        System.out.println("Corrupt the Blockchain");
                        // Set request type
                        request.setRequestType("corruptChain");
                        // Prompt user to enter block ID
                        System.out.println("Enter block ID of block to corrupt");
                        // Set block ID input
                        int id = scanner.nextInt();
                        request.setVar1(String.valueOf(id));
                        // Prompt user to enter new data for block
                        System.out.println("Enter new data for block " + id);
                        // Set new data input
                        request.setVar2(scanner.next());
                        // Transmit request and receive response
                        response = transmit(clientSocket, request);
                        // Display response
                        System.out.println(response.getResponse());
                        break;
                    case 5:
                        // Request to repair the entire blockchain
                        System.out.println("Repairing the entire chain");
                        // Set request type
                        request.setRequestType("repairChain");
                        // Record start time
                        startTime = System.currentTimeMillis();
                        // Transmit request and receive response
                        response = transmit(clientSocket, request);
                        // Record end time
                        endTime = System.currentTimeMillis();
                        // Display response and execution time
                        System.out.println(response.getResponse());
                        System.out.println("Total execution time required to repair the chain was " + (endTime - startTime) + " milliseconds");
                        break;
                    case 6:
                        // Request to exit the client program
                        request.setRequestType("clientExit");
                        // Transmit request and receive response
                        response = transmit(clientSocket, request);
                        // Display response and exit message
                        System.out.println(response.getResponse());
                        System.out.println("Exiting!");
                        // Set loop control variable to end loop
                        runVar = false;
                        break;
                }
                System.out.println();
            }
        } catch (IOException e) {
            // Handle IO Exception
            throw new RuntimeException(e);
        } finally {
            try {
                // Close client socket
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                // Ignore exception on close
            }
        }
    }
    /**
     * Transmits a request message to the server and receives a response message.
     *
     * @param clientSocket The socket used for communication with the server.
     * @param request The request message to be transmitted.
     * @return The response message received from the server.
     */
    private static ResponseMessage transmit(Socket clientSocket, RequestMessage request) {
        try {
            // Initialize input and output streams for communication
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())));
            // Set RSA variables and client ID in the request message
            request.setE(rsaVars[0]);
            request.setN(rsaVars[2]);
            request.setClientID(clientID);
            // Generate and set digital signature for the request message
            String mySign = sign(rsaVars[1],rsaVars[2], request.getClientID() + request.getE() +
                    request.getN() + request.getRequestType() +
                    request.getVar1() + request.getVar2());
            request.setSignature(mySign);
            // Convert request message to JSON format
            Gson gson = new Gson();
            String req = gson.toJson(request);
            // Send the JSON-formatted request message to the server
            out.println(req);
            out.flush();
            // Receive the response message from the server and convert it from JSON format
            String res = in.readLine();
            return gson.fromJson(res,ResponseMessage.class);
        } catch (Exception e) {
            // Handle any exceptions that occur during transmission
            throw new RuntimeException(e);
        }
    }
    /**
     * Generates RSA public and private keys.
     * @return An array containing the RSA public and private keys.
     * Code taken from project github, url: https://github.com/CMU-Heinz-95702/Project3
     */
    public static BigInteger[] getRSA() {
        // Each public and private key consists of an exponent and a modulus
        BigInteger n; // n is the modulus for both the private and public keys
        BigInteger e; // e is the exponent of the public key
        BigInteger d; // d is the exponent of the private key
        Random rnd = new Random();
        // Step 1: Generate two large random primes.
        // We use 400 bits here, but best practice for security is 2048 bits.
        // Change 400 to 2048, recompile, and run the program again and you will
        // notice it takes much longer to do the math with that many bits.
        BigInteger p = new BigInteger(2048, 100, rnd);
        BigInteger q = new BigInteger(2048, 100, rnd);
        // Step 2: Compute n by the equation n = p * q.
        n = p.multiply(q);
        // Step 3: Compute phi(n) = (p-1) * (q-1)
        BigInteger phi = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));
        // Step 4: Select a small odd integer e that is relatively prime to phi(n).
        // By convention the prime 65537 is used as the public exponent.
        e = new BigInteger("65537");
        // Step 5: Compute d as the multiplicative inverse of e modulo phi(n).
        d = e.modInverse(phi);
        BigInteger[] rsaVars = new BigInteger[3];
        rsaVars[0] = e; // storing n in array
        rsaVars[1] = d; // storing e in array
        rsaVars[2] = n; // storing d in array
        return rsaVars; // return array of generated keys
    }
    /**
     * Generates a unique client ID based on RSA keys.
     * @param rsa The array containing RSA public and private keys.
     * @return A unique client ID.
     */
    private static String getClientID(BigInteger[] rsa) {
        // Variable to hold the MessageDigest instance.
        MessageDigest md = null;
        try {
            // Initializing the MessageDigest with SHA-256 algorithm.
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            // Throw a RuntimeException if the algorithm is not found.
            throw new RuntimeException(e);
        }
        // Convert the first element of the RSA array (public key) to string.
        String e = rsa[0].toString();
        // Convert the third element of the RSA array (modulus) to string.
        String n = rsa[2].toString();
        // Update the MessageDigest with the concatenation of public key and modulus bytes.
        md.update((e+n).getBytes());
        // Get the hexadecimal representation of the digest.
        String hashData = bytesToHex(md.digest());
        // Return the last 20 characters of the hexadecimal representation.
        return hashData.substring(hashData.length()-20);
    }
    /**
     * Signs a message using RSA encryption.
     * @param d The private exponent.
     * @param n The modulus.
     * @param message The message to be signed.
     * @return The signed message.
     * @throws Exception If an error occurs during signing.
     * part of code taken from project github, url:https://github.com/CMU-Heinz-95702/Project3
     */
    public static String sign(BigInteger d, BigInteger n, String message) throws Exception {
        // Compute the digest of the message using SHA-256 algorithm.
        byte[] bytesOfMessage = message.getBytes("UTF-8");
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] bigDigest = md.digest(bytesOfMessage);
        // Prepare the message digest for signing.
        byte[] messageDigest = new byte[3];
        messageDigest[0] = 0;   // most significant set to 0
        messageDigest[1] = bigDigest[0]; // take a byte from SHA-256
        messageDigest[2] = bigDigest[1]; // take a byte from SHA-256
        // Create a BigInteger from the message digest.
        BigInteger m = new BigInteger(messageDigest);
        // Encrypt the digest with the private key.
        BigInteger c = m.modPow(d, n);
        // Return the signed message as a big integer string.
        return c.toString();
    }
    /**
     * Displays the menu options for the blockchain application and reads the user's selection.
     * @return The user's selected menu option.
     */
    public static int menu() {
        // Create a new Scanner object to read the user's menu selection.
        Scanner scanner = new Scanner(System.in);
        // Display menu options.
        System.out.println("0. View basic blockchain status.");
        System.out.println("1. Add a transaction to the blockchain.");
        System.out.println("2. Verify the blockchain.");
        System.out.println("3. View the blockchain.");
        System.out.println("4. Corrupt the chain.");
        System.out.println("5. Hide the corruption by recomputing hashes.");
        System.out.println("6. Exit");
        // Read and return the user's selection.
        return scanner.nextInt();
    }
    /**
     * Method to convert an array of bytes to a hexadecimal string.
     * @param bytes The array of bytes to be converted.
     * @return The hexadecimal representation of the input byte array.
     * Code taken from stackOverflow, url:https://stackoverflow.com/questions/9655181/java-convert-a-byte-array-to-a-hex-string
     */
    private static String bytesToHex(byte[] bytes) {
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
