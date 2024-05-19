package com.projects.main;

import com.projects.entity.CustomUser;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputUpdater;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.credential.CredentialModel;
import org.keycloak.models.*;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.storage.user.UserQueryProvider;
import org.keycloak.storage.user.UserRegistrationProvider;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class MySQLUserStorageProvider implements UserStorageProvider,
        UserLookupProvider, UserRegistrationProvider, UserQueryProvider,
        CredentialInputValidator, CredentialInputUpdater {

    private static final Logger logger = Logger.getLogger(MySQLUserStorageProvider.class.getName());

    private KeycloakSession session;
    private ComponentModel model;
    private Connection connection;

    public MySQLUserStorageProvider(KeycloakSession session, ComponentModel model) {
        this.session = session;
        this.model = model;
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
    public void close() {
        try {
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public UserModel getUserById(RealmModel realm, String id) {
        logger.info("Getting user by ID: " + id);
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE _id = ?");
            statement.setString(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return createAdapter(realm, resultSet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public UserModel getUserByUsername(RealmModel realm, String username) {
        logger.info("Getting user by username: " + username);
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE email = ?");
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return createAdapter(realm, resultSet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public UserModel getUserByEmail(RealmModel realm, String email) {
        return getUserByUsername(realm, email);
    }

    private UserModel createAdapter(RealmModel realm, ResultSet rs) throws Exception {
        DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        CustomUser user = new CustomUser.Builder(session, realm, model)
                .email(rs.getString("email"))
                .firstName(rs.getString("first_name"))
                .lastName(rs.getString("last_name"))
                .build();

        return user;
//        return new AbstractUserAdapter(session, realm, model) {
//            @Override
//            public String getUsername() {
//                try {
//                    return resultSet.getString("email");
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }
//            }
//
//            @Override
//            public String getEmail() {
//                try {
//                    return resultSet.getString("email");
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }
//            }
//
//            @Override
//            public SubjectCredentialManager credentialManager() {
//                return null;
//            }
//
//            @Override
//            public String getFirstName() {
//                try {
//                    return resultSet.getString("firstName");
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }
//            }
//
//            @Override
//            public String getLastName() {
//                try {
//                    return resultSet.getString("lastName");
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }
//            }
//
//            @Override
//            public void setUsername(String username) {
//                // Not needed for this implementation
//            }
//
//            @Override
//            public String getId() {
//                try {
//                    return resultSet.getString("_id");
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        };
    }

    @Override
    public UserModel addUser(RealmModel realm, String username) {
        logger.info("Adding user with username: " + username);
        try {
            String id = UUID.randomUUID().toString();
            PreparedStatement statement = connection.prepareStatement("INSERT INTO users (_id, email) VALUES (?, ?)");
            statement.setString(1, id);
            statement.setString(2, username);
            statement.executeUpdate();
            return getUserById(realm, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean removeUser(RealmModel realm, UserModel user) {
        logger.info("Removing user with ID: " + user.getId());
        try {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM users WHERE _id = ?");
            statement.setString(1, user.getId());
            statement.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void preRemove(RealmModel realm) {
        // Not needed
    }

    @Override
    public void preRemove(RealmModel realm, GroupModel group) {
        // Not needed
    }

    @Override
    public void preRemove(RealmModel realm, RoleModel role) {
        // Not needed
    }

    @Override
    public boolean supportsCredentialType(String credentialType) {
        return CredentialModel.PASSWORD.equals(credentialType);
    }

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        return supportsCredentialType(credentialType);
    }

    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput credentialInput) {
        if (!(credentialInput instanceof UserCredentialModel)) return false;
        UserCredentialModel cred = (UserCredentialModel) credentialInput;

        logger.info("Validating credentials for user ID: " + user.getId());
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT pin_code FROM users WHERE _id = ?");
            statement.setString(1, user.getId());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String storedPinCode = resultSet.getString("pin_code");
                return BCrypt.checkpw(cred.getChallengeResponse(), storedPinCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateCredential(RealmModel realm, UserModel user, CredentialInput input) {
        if (input.getType().equals(CredentialModel.PASSWORD)) {
            UserCredentialModel cred = (UserCredentialModel) input;
            String hashedPassword = BCrypt.hashpw(cred.getChallengeResponse(), BCrypt.gensalt());
            logger.info("Updating credentials for user ID: " + user.getId());
            try {
                PreparedStatement statement = connection.prepareStatement("UPDATE users SET pin_code = ? WHERE _id = ?");
                statement.setString(1, hashedPassword);
                statement.setString(2, user.getId());
                statement.executeUpdate();
                return true;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }


    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realm, String search) {
        logger.info("Searching users with search string: " + search);
        List<UserModel> users = new ArrayList<>();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE email LIKE ? OR users.first_name LIKE ? OR users.last_name LIKE ?");
            statement.setString(1, "%" + search + "%");
            statement.setString(2, "%" + search + "%");
            statement.setString(3, "%" + search + "%");
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                users.add(createAdapter(realm, resultSet));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return users.stream();
    }

    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realm, Map<String, String> params, Integer firstResult, Integer maxResults) {
        logger.info("Searching users with parameters: " + params);
        List<UserModel> users = new ArrayList<>();
        try {
            StringBuilder query = new StringBuilder("SELECT * FROM users WHERE 1=1");
            for (String key : params.keySet()) {
                query.append(" AND ").append(key).append(" = ?");
            }
            query.append(" LIMIT ? OFFSET ?");
            PreparedStatement statement = connection.prepareStatement(query.toString());
            int index = 1;
            for (String key : params.keySet()) {
                statement.setString(index++, params.get(key));
            }
            statement.setInt(index++, maxResults);
            statement.setInt(index, firstResult);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                users.add(createAdapter(realm, resultSet));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return users.stream();
    }

    @Override
    public Stream<UserModel> getGroupMembersStream(RealmModel realm, GroupModel group, Integer firstResult, Integer maxResults) {
        // Implement logic to get group members if required
        return Stream.empty();
    }

    @Override
    public Stream<UserModel> searchForUserByUserAttributeStream(RealmModel realm, String attrName, String attrValue) {
        logger.info("Searching users by attribute: " + attrName + " = " + attrValue);
        List<UserModel> users = new ArrayList<>();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE " + attrName + " = ?");
            statement.setString(1, attrValue);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                users.add(createAdapter(realm, resultSet));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return users.stream();
    }

    @Override
    public int getUsersCount(RealmModel realm) {
        logger.info("Getting user count");
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM users");
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public void disableCredentialType(RealmModel realmModel, UserModel userModel, String credentialType) {
        // Implement logic to disable a credential type if required
    }

    @Override
    public Stream<String> getDisableableCredentialTypesStream(RealmModel realmModel, UserModel userModel) {
        // Return stream of disableable credential types if required
        return Stream.empty();
    }
}
