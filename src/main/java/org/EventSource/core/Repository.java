package org.EventSource.core;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.UUID;

public interface Repository<A extends AggregateRoot<?>>{
    A findById(UUID uuid) throws Exception;
    void saveChanges(A aggregate) throws OptimisticConcurrencyException, SQLException, JsonProcessingException;
}
