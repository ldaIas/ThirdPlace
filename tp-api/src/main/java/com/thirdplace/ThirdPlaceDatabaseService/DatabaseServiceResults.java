package com.thirdplace.ThirdPlaceDatabaseService;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;


public record DatabaseServiceResults<T extends ResultType>(
    String preparedStatement,
    QueryOperation operation,
    SQLException exception,
    boolean successful,
    T result
) { 

    /**
     * Query result that contains the map of column names to their values
     */
    public record QueryResult(List<Map<String, Object>> results, int count) implements ResultType { }

    public record UpdateResult(int rowsUpdated) implements ResultType { }

    public record InsertResult(int rowsInserted, int id) implements ResultType { }

    public record DeleteResult(int rowsDeleted) implements ResultType { }
}