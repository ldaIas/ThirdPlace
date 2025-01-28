package com.thirdplace.ThirdPlaceDatabaseService;

import java.sql.SQLException;
import java.util.Map;


public record DatabaseServiceResults<T extends ResultType>(
    String statement,
    QueryOperation operation,
    SQLException exception,
    boolean successful,
    T result
) { 

    /**
     * Query result that contains the map of column names to their values
     */
    public record QueryResult(Map<String, Object> resultMap) implements ResultType { }

    public record UpdateResult(int rowsUpdated) implements ResultType { }

    public record InsertResult(int rowsInserted) implements ResultType { }

    public record DeleteResult(int rowsDeleted) implements ResultType { }
}