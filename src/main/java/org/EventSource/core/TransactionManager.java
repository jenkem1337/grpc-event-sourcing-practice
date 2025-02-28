package org.EventSource.core;

import java.sql.Connection;

public interface TransactionManager extends ConnectionFactory {
    void beginTransaction() ;
    void commit() ;
    void rollback();
    void closeConnection(Connection connection);
}
