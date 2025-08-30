package com.thirdplace.db;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.thirdplace.db.schemas.TableSchema;

public interface TableManager<T extends TableSchema> {
    void createTable() throws SQLException;

    String insert(T entity) throws SQLException;

    Optional<T> fetchById(String id) throws SQLException;

    List<T> fetchAll() throws SQLException;

    List<T> fetchByFilter(List<WhereFilter> filters) throws SQLException;

    boolean update(T entity) throws SQLException;

    boolean delete(String id) throws SQLException;
}