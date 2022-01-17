package jm.task.core.jdbc.dao;

public class DaoException extends RuntimeException {
    public DaoException(Throwable throwable) {
        super(throwable);
    }
}
