package com.projects.entity;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.LegacyUserCredentialManager;
import org.keycloak.models.*;
import org.keycloak.storage.adapter.AbstractUserAdapter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Stream;

@Slf4j
public class CustomUser extends AbstractUserAdapter {

    private UUID _id;

    private String firstName;
    private String lastName;
    private String email;


    private String pinCode;
    private String phoneNumber;
    private String avatar;


    private UserRoles userRole;


    private Socials userSocials;

    private static Connection connection;

    static {
        try {
            connection = DriverManager.getConnection(
                    "jdbc:mysql://my-sql-db:3306/user-db",
                    "admin",
                    "admin");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public CustomUser(KeycloakSession session, RealmModel realm,
                       ComponentModel storageProviderModel,
                       String email,
                       String firstName,
                       String lastName) {
        super(session, realm, storageProviderModel);
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;

    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getFirstName() {
        return firstName;
    }

    @Override
    public String getLastName() {
        return lastName;
    }

    @Override
    public String getEmail() {
        return email;
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

    @Override
    public SubjectCredentialManager credentialManager() {
        return new LegacyUserCredentialManager(session, realm, this);
//        return null;
    }

    public static class Builder {
        private final KeycloakSession session;
        private final RealmModel realm;
        private final ComponentModel storageProviderModel;
        private String email;
        private String firstName;
        private String lastName;

        public Builder(KeycloakSession session, RealmModel realm, ComponentModel storageProviderModel) {
            this.session = session;
            this.realm = realm;
            this.storageProviderModel = storageProviderModel;
        }

        public CustomUser.Builder email(String email) {
            this.email = email;
            return this;
        }

        public CustomUser.Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public CustomUser.Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public CustomUser build() {
            return new CustomUser(
                    session,
                    realm,
                    storageProviderModel,
                    email,
                    firstName,
                    lastName);

        }
    }

    @Override
    public Stream<RoleModel> getRoleMappingsStream() {
        return getRoleMappingsInternal().stream();
    }

    @Override
    protected Set<RoleModel> getRoleMappingsInternal() {
        Set<RoleModel> roles = new HashSet<>();
        if (userRole != null) {
            RoleModel roleModel = realm.getRole(userRole.toString());
            if (roleModel != null) {
                roles.add(roleModel);
            }
        }
        return roles;
    }

    @Override
    public void grantRole(RoleModel role) {
        log.info("Granting role: " + role.getName() + " to user: " + email);
        if (!hasRole(role)) {
            userRole = UserRoles.valueOf(role.getName());
            updateUserRoleInDatabase(_id, role.getName());
        } else {
            log.info("User already has role: {}", role.getName());
        }
    }

    @Override
    public void deleteRoleMapping(RoleModel role) {
        log.info("Deleting role: " + role.getName() + " from user: " + email);
        if (hasRole(role)) {
            userRole = null;
            updateUserRoleInDatabase(_id, null);
        } else {
            log.info("User does not have role: {}", role.getName());
        }
    }

    @Override
    public boolean hasRole(RoleModel role) {
        boolean hasRole = userRole != null && userRole.toString().equals(role.getName());
        log.info("User: {} has role {}: {}", email, role.getName(), hasRole);
        return hasRole;
    }

    private void updateUserRoleInDatabase(UUID userId, String roleName) {
        String sql = "UPDATE users SET user_role = ? WHERE _id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            if (roleName != null) {
                statement.setString(1, roleName);
            } else {
                statement.setNull(1, java.sql.Types.VARCHAR);
            }
            statement.setObject(2, userId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update user role in database", e);
        }
    }
}
