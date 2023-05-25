package cn.edu.thssdb.exception;

public class WrongDataException extends RuntimeException {
    @Override
    public String getMessage() {
        return "Exception: Internal error when recovering data!";
    }
}
