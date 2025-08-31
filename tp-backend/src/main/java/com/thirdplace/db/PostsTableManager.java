package com.thirdplace.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.thirdplace.db.schemas.Post;

public class PostsTableManager implements TableManager<Post> {

    public static final Class<Post> POST_CLASS = Post.class;

    private static PostsTableManager manager;

    private PostsTableManager() {
    }

    public static synchronized PostsTableManager getInstance() {
        if (manager == null) {
            manager = new PostsTableManager();
        }
        return manager;
    }

    @Override
    public void createTable() throws SQLException {

        String sql = AppDbInterpreter.generateTableDdl(POST_CLASS);

        try (Connection conn = DatabaseManager.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    @Override
    public String insert(Post post) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {

            final PreparedStatement stmt = AppDbInterpreter.prepareInsertStatement(post, conn);
            stmt.executeUpdate();
            return post.id();
        }
    }

    @Override
    public List<Post> fetchByFilter(List<WhereFilter> filters) throws SQLException {
        List<Post> results = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
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

        try (Connection conn = DatabaseManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                posts.add(AppDbInterpreter.mapResultSetToSchema(Post.class, rs));
            }
        }
        return posts;
    }

    @Override
    public boolean update(final Post post) throws SQLException {
        final List<WhereFilter> whereClause = List.of(
                new WhereFilter(Post.PostFieldReference.ID, WhereFilter.FilterOperator.EQUALS, post.id()));

        try (final Connection conn = DatabaseManager.getConnection();
                final PreparedStatement stmt = AppDbInterpreter.prepareUpdateStatement(post, whereClause, conn)) {

            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(String id) throws SQLException {
        List<WhereFilter> whereClause = List.of(
                new WhereFilter(Post.PostFieldReference.ID, WhereFilter.FilterOperator.EQUALS, id));

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = AppDbInterpreter.prepareDeleteStatement(Post.TABLE_NAME, whereClause, conn)) {

            return stmt.executeUpdate() > 0;
        }
    }

}