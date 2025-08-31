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
import com.thirdplace.db.schemas.Post;
import com.thirdplace.db.schemas.SchemaFieldReference;

public class PostsTableManager implements TableManager<Post> {

    private static final List<SchemaFieldReference> POST_FIELD_REFS = List.of(Post.PostFieldReference.values());

    private DataSourceCacheKey datasourceKey;

    public PostsTableManager(final DataSourceCacheKey datasourceKey) {
        this.datasourceKey = datasourceKey;
    }

    @Override
    public void createTable() throws SQLException {

        String sql = AppDbInterpreter.generateTableDdl(Post.TABLE_NAME, POST_FIELD_REFS);

        try (Connection conn = DatabaseManager.getConnection(datasourceKey);
                Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    @Override
    public String insert(Post post) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection(datasourceKey)) {

            final PreparedStatement stmt = AppDbInterpreter.prepareInsertStatement(post, conn);
            stmt.executeUpdate();
            return post.id();
        }
    }

    @Override
    public List<Post> fetchByFilter(List<WhereFilter> filters) throws SQLException {
        List<Post> results = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection(datasourceKey);
                PreparedStatement stmt = AppDbInterpreter.prepareSelectStatement(Post.TABLE_NAME, filters, conn);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                results.add(AppDbInterpreter.mapResultSetToSchema(Post.class, rs));
            }
        }
        return results;
    }

    @Override
    public Optional<Post> fetchById(String id) throws SQLException {
        List<WhereFilter> filters = List.of(
                new WhereFilter(Post.PostFieldReference.ID, WhereFilter.FilterOperator.EQUALS, id));
        List<Post> results = fetchByFilter(filters);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public List<Post> fetchAll() throws SQLException {
        String sql = "SELECT * FROM posts ORDER BY createdat DESC";
        List<Post> posts = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection(datasourceKey);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                posts.add(AppDbInterpreter.mapResultSetToSchema(Post.class, rs));
            }
        }
        return posts;
    }

    @Override
    public boolean update(Post post) throws SQLException {
        List<WhereFilter> whereClause = List.of(
                new WhereFilter(Post.PostFieldReference.ID, WhereFilter.FilterOperator.EQUALS, post.id()));

        try (Connection conn = DatabaseManager.getConnection(datasourceKey);
                PreparedStatement stmt = AppDbInterpreter.prepareUpdateStatement(post, whereClause, conn)) {

            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(String id) throws SQLException {
        List<WhereFilter> whereClause = List.of(
                new WhereFilter(Post.PostFieldReference.ID, WhereFilter.FilterOperator.EQUALS, id));

        try (Connection conn = DatabaseManager.getConnection(datasourceKey);
                PreparedStatement stmt = AppDbInterpreter.prepareDeleteStatement(Post.TABLE_NAME, whereClause, conn)) {

            return stmt.executeUpdate() > 0;
        }
    }

}