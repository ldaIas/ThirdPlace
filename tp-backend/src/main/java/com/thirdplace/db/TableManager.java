package com.thirdplace.db;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface TableManager<T extends TableSchema> {
    void createTable() throws SQLException;
    String insert(T entity) throws SQLException;
    Optional<T> findById(String id) throws SQLException;
    List<T> findAll() throws SQLException;
    boolean update(T entity) throws SQLException;
    boolean delete(String id) throws SQLException;
}