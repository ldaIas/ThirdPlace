package com.thirdplace.thirdplacedatabaseservice;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public record DatabaseServiceResults<R extends ResultType>(
    String preparedStatement,
    QueryOperation operation,
    SQLException exception,
    boolean successful,
    R result
) { 

    /**
     * Query result that contains the map of column names to their values
     */
    public record QueryResult(List<Map<String, Object>> results, int count) implements ResultType { }

    public record UpdateResult(List<Map<String, Object>> updated, int rowsUpdated) implements ResultType { }

    public record InsertResult(Map<String, Object> inserted, int rowsInserted) implements ResultType { }

    public record DeleteResult(int rowsDeleted) implements ResultType { }
}