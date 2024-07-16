/**
 * Author: Dhanush Venkataramu
 * Last Modified: 03/17/2024
 *
 * Represents a response message with a type and content.
 */
public class ResponseMessage {
    private String responseType; // Type of response
    private String response; // Content of the response
    /**
     * Gets the type of response.
     * @return The type of response.
     */
    public String getResponseType() {
        return responseType;
    }
    /**
     * Gets the content of the response.
     * @return The content of the response.
     */
    public String getResponse() {
        return response;
    }
    /**
     * Sets the type of response.
     * @param responseType The type of response to set.
     */
    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }
    /**
     * Sets the content of the response.
     * @param response The content of the response to set.
     */
    public void setResponse(String response) {
        this.response = response;
    }
}
