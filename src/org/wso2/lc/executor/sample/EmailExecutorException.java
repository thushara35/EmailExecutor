package org.wso2.lc.executor.sample;

/**
 * This class handles API Gateway related exceptions.
 */
public class EmailExecutorException extends Exception {

    /**
     * Constructor method to handle API Gateway related exceptions without message and throwable course.
     */
    public EmailExecutorException() {
        super();
    }

    /**
     * Constructor method to handle API Gateway related exceptions only with message.
     *
     * @param message error message.
     */
    public EmailExecutorException(String message) {
        super(message);
    }

    /**
     * Constructor method to handle API Gateway related exceptions only with throwable course.
     *
     * @param cause throwable cause.
     */
    public EmailExecutorException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor method to handle API Gateway related exceptions with message and throwable course.
     *
     * @param message error message.
     * @param cause   throwable cause.
     */
    public EmailExecutorException(String message, Throwable cause) {
        super(message, cause);
    }

}
