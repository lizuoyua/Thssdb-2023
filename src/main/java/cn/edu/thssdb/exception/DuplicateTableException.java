package cn.edu.thssdb.exception;

public class DuplicateTableException extends RuntimeException {
    @Override
    public String getMessage() {
        return "Exception: creation caused duplicated table!";
    }
}
