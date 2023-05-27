package cn.edu.thssdb.exception;

public class DatabaseOccupiedException extends RuntimeException {

    @Override
    public String getMessage() {
        return "Exception: Database is under use!";
    }
}
