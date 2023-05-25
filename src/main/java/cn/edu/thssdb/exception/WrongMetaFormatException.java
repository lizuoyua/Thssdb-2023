package cn.edu.thssdb.exception;

public class WrongMetaFormatException extends RuntimeException {
    @Override
    public String getMessage() {
        return "Exception: Meta data format error!";
    }
}
