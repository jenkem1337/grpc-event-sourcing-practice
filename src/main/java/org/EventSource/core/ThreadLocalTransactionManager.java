package org.EventSource.core;

import java.sql.Connection;
import java.sql.SQLException;

public class ThreadLocalTransactionManager implements TransactionManager {
    private final ConnectionFactory connectionFactory;
    private static final ThreadLocal<Connection> connectionThreadLocal = new ThreadLocal<>();
    public ThreadLocalTransactionManager(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }
    @Override
    public void beginTransaction() {
        if(connectionThreadLocal.get() == null) {
            Connection connection = connectionFactory.getConnection();
            try {
                connection.setAutoCommit(false);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            connectionThreadLocal.set(connection);
        }
    }

    @Override
    public void commit()  {
        Connection conn = connectionThreadLocal.get();
        if (conn != null) {
            try {
                conn.commit();
                conn.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            finally {
                connectionThreadLocal.remove();
            }
        }

    }

    @Override
    public void rollback()  {
        Connection conn = connectionThreadLocal.get();
        if (conn != null) {
            try {
                conn.rollback();
                conn.close();
            }catch (SQLException e) {
                throw new RuntimeException(e);
            }
            finally {
                connectionThreadLocal.remove();
            }
        }

    }

    @Override
    public Connection getConnection() {
        Connection conn = connectionThreadLocal.get();
        if(conn == null) {

                return connectionFactory.getConnection();

        }
        return conn;
    }
    @Override
    public void closeConnection(Connection connection) {
        if(connectionThreadLocal.get() != null){
            return;
        }
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
