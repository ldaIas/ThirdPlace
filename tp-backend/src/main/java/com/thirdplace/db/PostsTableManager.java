package com.thirdplace.db;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.thirdplace.db.schemas.Post;
import com.thirdplace.db.schemas.SchemaFieldReference;

public class PostsTableManager implements TableManager<Post> {

    private static PostsTableManager manager;

    private static final List<SchemaFieldReference> POST_FIELD_REFS = List.of(Post.PostFieldReference.values());

    private PostsTableManager() {
        // Private constructor to enforce singleton pattern
    }

    public static PostsTableManager getInstance() {
        if (manager == null) {
            manager = new PostsTableManager();
        }
        return manager;
    }

    @Override
    public void createTable() throws SQLException {

        String sql = AppDbInterpreter.generateTableDdl(Post.TABLE_NAME, POST_FIELD_REFS);
        
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
    public Optional<Post> findById(String id) throws SQLException {
        String sql = "SELECT * FROM posts WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(AppDbInterpreter.mapResultSetToSchema(Post.class, rs));
            }
            return Optional.empty();
        }
    }
    
    @Override
    public List<Post> findAll() throws SQLException {
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
    public boolean update(Post post) throws SQLException {
        String sql = """
            UPDATE posts SET title = ?, description = ?, end_date = ?, group_size = ?, 
                           tags = ?, location = ?, proposed_time = ?, status = ?, 
                           gender_balance = ?, category = ?
            WHERE id = ?
            """;
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, post.title());
            stmt.setString(2, post.description());
            stmt.setTimestamp(3, Timestamp.from(post.endDate()));
            stmt.setInt(4, post.groupSize());
            stmt.setArray(5, conn.createArrayOf("TEXT", post.tags()));
            stmt.setString(6, post.location());
            stmt.setTimestamp(7, Timestamp.from(post.proposedTime()));
            stmt.setString(8, post.status());
            stmt.setString(9, post.genderBalance());
            stmt.setString(10, post.category());
            stmt.setString(11, post.id());
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    @Override
    public boolean delete(String id) throws SQLException {
        String sql = "DELETE FROM posts WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, id);
            return stmt.executeUpdate() > 0;
        }
    }
    
}