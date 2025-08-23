package com.thirdplace.db;

import java.sql.*;
import java.util.*;

import com.thirdplace.schemas.Post;

public class PostsTableManager implements TableManager<Post> {

    private static PostsTableManager manager;
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
        String sql = """
            CREATE TABLE IF NOT EXISTS posts (
                id VARCHAR(255) PRIMARY KEY,
                title VARCHAR(100) NOT NULL,
                author VARCHAR(255) NOT NULL,
                description VARCHAR(500),
                created_at TIMESTAMP NOT NULL,
                end_date TIMESTAMP NOT NULL,
                group_size INTEGER NOT NULL,
                tags TEXT[],
                location VARCHAR(255),
                geohash VARCHAR(9) NOT NULL,
                latitude DOUBLE PRECISION NOT NULL,
                longitude DOUBLE PRECISION NOT NULL,
                proposed_time TIMESTAMP NOT NULL,
                is_date_activity BOOLEAN NOT NULL,
                status VARCHAR(20) NOT NULL,
                gender_balance VARCHAR(20),
                category VARCHAR(50) NOT NULL
            )
            """;
        
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }
    
    @Override
    public String insert(Post post) throws SQLException {
        String sql = """
            INSERT INTO posts (id, title, author, description, created_at, end_date, 
                             group_size, tags, location, geohash, latitude, longitude, 
                             proposed_time, is_date_activity, status, gender_balance, category)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, post.id());
            stmt.setString(2, post.title());
            stmt.setString(3, post.author());
            stmt.setString(4, post.description());
            stmt.setTimestamp(5, Timestamp.from(post.createdAt()));
            stmt.setTimestamp(6, Timestamp.from(post.endDate()));
            stmt.setInt(7, post.groupSize());
            stmt.setArray(8, conn.createArrayOf("TEXT", post.tags().toArray()));
            stmt.setString(9, post.location());
            stmt.setString(10, post.geohash());
            stmt.setDouble(11, post.latitude());
            stmt.setDouble(12, post.longitude());
            stmt.setTimestamp(13, Timestamp.from(post.proposedTime()));
            stmt.setBoolean(14, post.isDateActivity());
            stmt.setString(15, post.status());
            stmt.setString(16, post.genderBalance());
            stmt.setString(17, post.category());
            
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
                return Optional.of(mapResultSetToPost(rs));
            }
            return Optional.empty();
        }
    }
    
    @Override
    public List<Post> findAll() throws SQLException {
        String sql = "SELECT * FROM posts ORDER BY created_at DESC";
        List<Post> posts = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                posts.add(mapResultSetToPost(rs));
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
            stmt.setArray(5, conn.createArrayOf("TEXT", post.tags().toArray()));
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
    
    private Post mapResultSetToPost(ResultSet rs) throws SQLException {
        Array tagsArray = rs.getArray("tags");
        List<String> tags = tagsArray != null ? 
            Arrays.asList((String[]) tagsArray.getArray()) : 
            new ArrayList<>();
        
        return new Post(
            rs.getString("id"),
            rs.getString("title"),
            rs.getString("author"),
            rs.getString("description"),
            rs.getTimestamp("created_at").toInstant(),
            rs.getTimestamp("end_date").toInstant(),
            rs.getInt("group_size"),
            tags,
            rs.getString("location"),
            rs.getString("geohash"),
            rs.getDouble("latitude"),
            rs.getDouble("longitude"),
            rs.getTimestamp("proposed_time").toInstant(),
            rs.getBoolean("is_date_activity"),
            rs.getString("status"),
            rs.getString("gender_balance"),
            rs.getString("category")
        );
    }
}