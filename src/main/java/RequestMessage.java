/**
 * Author: Dhanush Venkataramu
 * Last Modified: 03/17/2024
 *
 * Represents a request message with fields for client identification,
 * encryption parameters, request
 */
// Import for handling large integers.
import java.math.BigInteger;

class RequestMessage {
    private String clientID; // Client ID associated with the request.
    private BigInteger e; // BigInteger representing 'e'.
    private BigInteger n; // BigInteger representing 'n'.
    private String requestType; // Type of request.
    private String var1; // Variable 1.
    private String var2; // Variable 2.
    private String signature; // Signature of the request.
    /**
     * Getter for the request type.
     * @return The request type.
     */
    public String getRequestType() {
        return requestType;
    }
    /**
     * Getter for variable 1.
     * @return Variable 1.
     */
    public String getVar1() {
        return var1;
    }
    /**
     * Getter for variable 2.
     * @return Variable 2.
     */
    public String getVar2() {
        return var2;
    }
    /**
     * Getter for the request signature.
     * @return The request signature.
     */
    public String getSignature() {
        return signature;
    }
    /**
     * Getter for the client ID.
     * @return The client ID.
     */
    public String getClientID() {
        return clientID;
    }
    /**
     * Getter for 'e'.
     * @return The BigInteger 'e'.
     */
    public BigInteger getE() {
        return e;
    }
    /**
     * Getter for 'n'.
     * @return The BigInteger 'n'.
     */
    public BigInteger getN() {
        return n;
    }
    /**
     * Setter for the request type.
     * @param requestType The request type to set.
     */
    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }
    /**
     * Setter for variable 1.
     * @param var1 The value to set for variable 1.
     */
    public void setVar1(String var1) {
        this.var1 = var1;
    }
    /**
     * Setter for variable 2.
     * @param var2 The value to set for variable 2.
     */
    public void setVar2(String var2) {
        this.var2 = var2;
    }
    /**
     * Setter for the client ID.
     * @param clientID The client ID to set.
     */
    public void setClientID(String clientID) {
        this.clientID = clientID;
    }
    /**
     * Setter for the request signature.
     * @param signature The signature to set.
     */
    public void setSignature(String signature) {
        this.signature = signature;
    }
    /**
     * Setter for 'e'.
     * @param e The BigInteger 'e' to set.
     */
    public void setE(BigInteger e) {
        this.e = e;
    }
    /**
     * Setter for 'n'.
     * @param n The BigInteger 'n' to set.
     */
    public void setN(BigInteger n) {
        this.n = n;
    }
}
