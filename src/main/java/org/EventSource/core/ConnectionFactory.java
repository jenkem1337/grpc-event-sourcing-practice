package org.EventSource.core;

import java.sql.Connection;

public interface ConnectionFactory {
    Connection getConnection();
}
