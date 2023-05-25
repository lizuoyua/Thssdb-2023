package cn.edu.thssdb.exception;

public class MetaFileNotFoundException extends RuntimeException {
    @Override
    public String getMessage() {
        return "Exception: Meta file is not found!";
    }
}
