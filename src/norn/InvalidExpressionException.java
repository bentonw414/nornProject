package norn;


/**
 * Exception class for invalid expressions. 
 */
public class InvalidExpressionException extends Exception {
    /**
     * Create an invalid expression exception with the given error message
     * @param errorMessage the error message to include
     */
    public InvalidExpressionException(String errorMessage) {
        super(errorMessage);
    }
}
