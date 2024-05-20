package com.projects.main;

import com.projects.entity.User;
import com.projects.entity.UserRoles;
import jakarta.ws.rs.core.MultivaluedHashMap;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.adapter.AbstractUserAdapterFederatedStorage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

@Slf4j
public class UserAdapter extends AbstractUserAdapterFederatedStorage {

    private final User user;
    private String keycloakId;
    private Connection connection;

    public UserAdapter(KeycloakSession session, RealmModel realm, ComponentModel componentModel, User user) {
        super(session, realm, componentModel);
        this.user = user;
        if (user != null) {
            keycloakId = StorageId.keycloakId(componentModel, user.get_id().toString());
        }
        try {
            this.connection = DriverManager.getConnection(
                    "jdbc:mysql://my-sql-db:3306/user-db",
                    "admin",
                    "admin");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public Stream<String> getRequiredActionsStream() {
        return super.getRequiredActionsStream();
    }

    @Override
    public void removeRequiredAction(String action) {
        super.removeRequiredAction(action);
    }

    @Override
    public String getUsername() {
        return user.getFirstName();
    }

    @Override
    public void setUsername(String s) {
        user.setFirstName(s);
    }

    @Override
    public String getEmail() {
        return user.getEmail();
    }

    @Override
    public String getId() {
        return user.get_id().toString();
    }

    @Override
    public String getFirstName() {
        return user.getFirstName();

    }

    @Override
    public String getLastName() {
        return user.getLastName();
    }

    @Override
    public void setFirstName(String firstName) {
        String email = user.getEmail();
        log.info("setting first name to user with email: {}", email);

        try {
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE users SET first_name = ? WHERE email = ?");
            preparedStatement.setString(1, firstName);
            preparedStatement.setString(2, email);
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }

        user.setFirstName(firstName);
    }

    @Override
    public void setLastName(String lastName) {
        String email = user.getEmail();
        log.info("setting last name to user with email: {}", email);

        try {
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE users SET last_name = ? WHERE email = ?");
            preparedStatement.setString(1, lastName);
            preparedStatement.setString(2, email);
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }

        user.setLastName(lastName);
    }

    @Override
    public void setEmail(String email) {
        String userEmail = user.getEmail();
        log.info("setting email to user with email: {}", email);

        try {
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE users SET email = ? WHERE email = ?");
            preparedStatement.setString(1, email);
            preparedStatement.setString(2, userEmail);
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }

        user.setEmail(email);
    }

    @Override
    protected Set<RoleModel> getRoleMappingsInternal() {
        Set<RoleModel> roles = new HashSet<>();

        UserRoles userRole = user.getUserRole();

        roles.add(realm.getRole(userRole.name()));

        return roles;
    }

    @Override
    public Map<String, List<String>> getAttributes() {
        MultivaluedHashMap<String, String> attributes = new MultivaluedHashMap<>();
        attributes.add(UserModel.USERNAME, getUsername());
        attributes.add(UserModel.EMAIL, getEmail());
        attributes.add(UserModel.FIRST_NAME, getFirstName());
        attributes.add(UserModel.LAST_NAME, getLastName());
        return attributes;
    }

    //todo: user read-only
    @Override
    public void setAttribute(String name, List<String> values) {
        super.setAttribute(name, values);
    }
}
