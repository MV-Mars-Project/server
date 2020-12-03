package be.howest.ti.mars.logic.data.h2repositories;

import be.howest.ti.mars.logic.controller.Endpoint;
import be.howest.ti.mars.logic.controller.converters.ShortEndpoint;
import be.howest.ti.mars.logic.controller.exceptions.DatabaseException;
import be.howest.ti.mars.logic.controller.exceptions.EndpointException;
import be.howest.ti.mars.logic.data.util.MarsConnection;
import be.howest.ti.mars.logic.data.Repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EndpointsH2Repository implements EndpointsRepository {
    private static final Logger LOGGER = Logger.getLogger(SubscriptionH2Repository.class.getName());

    // Endpoints
    private static final String SQL_GET_ENDPOINT = "SELECT * FROM ENDPOINTS WHERE ID = ?";
    private static final String SQL_GET_ENDPOINTS = "SELECT * FROM ENDPOINTS";
    private static final String SQL_INSERT_ENDPOINT = "INSERT INTO ENDPOINTS(name) VALUES(?)";

    @Override
    public ShortEndpoint getShortEndpoint(int id) {
        Endpoint endpoint = Repositories.getEndpointsRepo().getEndpoint(id);
        return new ShortEndpoint(endpoint.getId(), endpoint.getName());
    }

    @Override
    public boolean endpointExists(int id) {
        return getEndpoints().stream().anyMatch(endpoint -> endpoint.getId() == id);
    }

    @Override
    // TODO: 21-11-2020 add endpoint visibility logic: users see only their endpoint and public endpoints and friend home endpoints(if sharing), companies see all endpoints but what if normal person needs to send package to other person ???
    public Set<ShortEndpoint> getEndpoints() {
        Set<ShortEndpoint> endpoints = new HashSet<>();

        try (Connection con = MarsConnection.getConnection();
             PreparedStatement stmt = con.prepareStatement(SQL_GET_ENDPOINTS)) {

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    endpoints.add(new ShortEndpoint(rs.getInt("id"), rs.getString("name")));
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            throw new DatabaseException("Cannot retrieve endpoints");
        }
        return endpoints;
    }

    @Override
    public void addEndpoint(String endpoint) {
        try (Connection con = MarsConnection.getConnection();
             PreparedStatement stmt = con.prepareStatement(SQL_INSERT_ENDPOINT)) {
            stmt.setString(1, endpoint);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new DatabaseException("Can't add endpoint!");
        }
    }

    @Override
    public Endpoint getEndpoint(int id) {
        try (Connection con = MarsConnection.getConnection();
             PreparedStatement stmt = con.prepareStatement(SQL_GET_ENDPOINT)
        ) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Endpoint(id, rs.getString("name"), true, "todo", false);
                } else {
                    throw new EndpointException("Endpoint with ID (" + id + ") doesn't exist!");
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            throw new DatabaseException("Cannot retrieve endpoint with id: " + id + "!");
        }
    }
}
