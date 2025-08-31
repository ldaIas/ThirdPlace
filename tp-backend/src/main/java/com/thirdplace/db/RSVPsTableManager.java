package com.thirdplace.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.thirdplace.db.DatabaseConfig.DataSourceCacheKey;
import com.thirdplace.db.schemas.RSVP;
import com.thirdplace.db.schemas.SchemaFieldReference;

public class RSVPsTableManager implements TableManager<RSVP> {

    private static final List<SchemaFieldReference> RSVP_FIELD_REFS = List.of(RSVP.RSVPFieldReference.values());

    private DataSourceCacheKey datasourceKey;

    public RSVPsTableManager(final DataSourceCacheKey datasourceKey) {
        // Private constructor to enforce singleton pattern
        this.datasourceKey = datasourceKey;
    }

    @Override
    public void createTable() throws SQLException {
        String sql = AppDbInterpreter.generateTableDdl(RSVP.TABLE_NAME, RSVP_FIELD_REFS);

        try (Connection conn = DatabaseManager.getConnection(datasourceKey);
                Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    @Override
    public String insert(RSVP rsvp) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection(datasourceKey)) {
            final PreparedStatement stmt = AppDbInterpreter.prepareInsertStatement(rsvp, conn);
            stmt.executeUpdate();
            return rsvp.id();
        }
    }

    @Override
    public List<RSVP> fetchByFilter(List<WhereFilter> filters) throws SQLException {
        List<RSVP> results = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection(datasourceKey);
                PreparedStatement stmt = AppDbInterpreter.prepareSelectStatement(RSVP.TABLE_NAME, filters, conn);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                results.add(AppDbInterpreter.mapResultSetToSchema(RSVP.class, rs));
            }
        }
        return results;
    }

    @Override
    public Optional<RSVP> fetchById(String id) throws SQLException {
        List<WhereFilter> filters = List.of(
                new WhereFilter(RSVP.RSVPFieldReference.ID, WhereFilter.FilterOperator.EQUALS, id));
        List<RSVP> results = fetchByFilter(filters);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public List<RSVP> fetchAll() throws SQLException {
        String sql = "SELECT * FROM rsvps ORDER BY createdat DESC";
        List<RSVP> rsvps = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection(datasourceKey);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                rsvps.add(AppDbInterpreter.mapResultSetToSchema(RSVP.class, rs));
            }
        }
        return rsvps;
    }

    @Override
    public boolean update(RSVP rsvp) throws SQLException {
        List<WhereFilter> whereClause = List.of(
            new WhereFilter(RSVP.RSVPFieldReference.ID, WhereFilter.FilterOperator.EQUALS, rsvp.id())
        );
        
        try (Connection conn = DatabaseManager.getConnection(datasourceKey);
             PreparedStatement stmt = AppDbInterpreter.prepareUpdateStatement(rsvp, whereClause, conn)) {
            
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(String id) throws SQLException {
        List<WhereFilter> whereClause = List.of(
            new WhereFilter(RSVP.RSVPFieldReference.ID, WhereFilter.FilterOperator.EQUALS, id)
        );
        
        try (Connection conn = DatabaseManager.getConnection(datasourceKey);
             PreparedStatement stmt = AppDbInterpreter.prepareDeleteStatement(RSVP.TABLE_NAME, whereClause, conn)) {
            
            return stmt.executeUpdate() > 0;
        }
    }
}