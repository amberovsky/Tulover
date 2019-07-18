package com.revolut.router;

/**
 * API Response
 */
public class Response {
    /**
     * Possible response codes
     */
    public enum ResponseCode {
        NO_ERROR(0), // no error
        INTERNAL_ERROR(1), // internal errors like unhandled exceptions
        MISSING_PARAMETER(2), // a parameter is missing in the request
        WRONG_PARAMETER(3), // the format of a parameter is wrong
        INVALID_VALUE(4); // the value of a parameter is invalid

        /**
         * DEC code
         */
        private int code;

        /**
         * @param value code
         */
        ResponseCode(int value) { this.code = value; }

        /**
         * @return DEC code
         */
        public int code() { return this.code; }
    }

    /**
     * Response code
     */
    private ResponseCode responseCode;

    /**
     * Optional message to the response code
     */
    private String msg;

    /**
     * Optional any other additional data
     */
    private Object data;

    /**
     * @param responseCode response code
     * @param msg optional message to the response code
     */
    public Response(ResponseCode responseCode, String msg) {
        this.responseCode = responseCode;
        this.msg = msg;
    }

    /**
     * @param data optional any other additional data
     * @return Response
     */
    public Response setData(Object data) {
        this.data = data;
        return this;
    }
}
