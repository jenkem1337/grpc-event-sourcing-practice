package org.EventSource.core;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class PostgresConnectionFactory implements ConnectionFactory {
    private final Properties properties;

    public PostgresConnectionFactory(Properties properties){
        this.properties = properties;
    }
    @Override
    public Connection getConnection() {
        try {
            return DriverManager.getConnection("jdbc:postgresql://"+ properties.get("host")+":"+properties.get("port")+"/EventStore", properties);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
