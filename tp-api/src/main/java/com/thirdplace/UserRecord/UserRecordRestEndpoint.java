package com.thirdplace.roomchat;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.thirdplace.utils.JsonUtils;
import com.thirdplace.utils.JsonUtilsException;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.thirdplace.ThirdPlaceDatabaseService.UserRecord;

@Path("/users")  // Base path for this endpoint
public class UserRecordRestEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserRecordRestEndpoint.class);
    private static final String SERVER_NAME = "server";

    // Example: GET request to fetch a user by ID
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUser(@PathParam("id") int id) {
        LOGGER.info("Fetching user with ID: {}", id);
        
        // Simulated user response
        return Response.ok(user).build();
    }

    // Example: POST request to create a user
    @PUT
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createUser(UserRecord user) {
        LOGGER.info("Creating new user: {}", user);
        
        String hashedPassword = CryptoUtils.hashPassword(user.getPassword());
        user.setPassword(hashedPassword);

        // Simulate database save
        userDatabase.put(user);

        // Simulated creation logic
        return Response.status(Response.Status.CREATED).entity(user).build();
    }


    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response loginUser(LoginRequest loginRequest) {
        LOGGER.info("Logging in user: {}", loginRequest.getUsername());

        User user = userDatabase.get(loginRequest.getUsername());
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("User not found").build();
        }

        // Verify hashed password using bcrypt
        if (!CyptoUtils.verifyPassword(loginRequest.getPassword(), user.getPassword())) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid credentials").build();
        }

        // Generate JWT
        String token = JwtUtils.generateToken(user.getUsername());

        return Response.ok().entity(Map.of("token", token)).build();
    }
  
}





