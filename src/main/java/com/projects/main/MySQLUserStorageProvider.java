package com.projects.main;

import com.projects.entity.CustomUser;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputUpdater;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.credential.CredentialModel;
import org.keycloak.models.*;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.storage.user.UserQueryMethodsProvider;
import org.keycloak.storage.user.UserQueryProvider;
import org.keycloak.storage.user.UserRegistrationProvider;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class MySQLUserStorageProvider implements UserStorageProvider,
        UserLookupProvider, UserRegistrationProvider, UserQueryProvider, UserQueryMethodsProvider,
        CredentialInputValidator, CredentialInputUpdater {

    private static final Logger logger = Logger.getLogger(MySQLUserStorageProvider.class.getName());
    private static final Map<String, String> PARAM_TO_SQL_FIELD = new HashMap<>();

    static {
        PARAM_TO_SQL_FIELD.put("userId", "_id");
        PARAM_TO_SQL_FIELD.put("email", "email");
        PARAM_TO_SQL_FIELD.put("firstName", "first_name");
        PARAM_TO_SQL_FIELD.put("lastName", "last_name");
        // todo: other mappings
    }

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
            String persistenceId = StorageId.externalId(id);
            logger.info("External ID: " + persistenceId);
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE email = ?");
            statement.setString(1, persistenceId);
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
    }

    @Override
    public UserModel addUser(RealmModel realm, String email) {
        logger.info("Adding user with email: " + email);
        try {
            String id = UUID.randomUUID().toString();
            PreparedStatement statement = connection.prepareStatement("INSERT INTO users (_id, email) VALUES (?, ?)");
            statement.setString(1, id);
            statement.setString(2, email);
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
            //todo: test
            String email = user.getEmail();
            PreparedStatement statement = connection.prepareStatement("DELETE FROM users WHERE email = ?");
            statement.setString(1, email);
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
            String userEmail = user.getEmail();
            try {
                PreparedStatement statement = connection.prepareStatement("UPDATE users SET pin_code = ? WHERE email = ?");
                statement.setString(1, hashedPassword);
                statement.setString(2, userEmail);
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
        if (search == null || search.trim().isEmpty()) {
            return users.stream(); // Return an empty stream if the search string is empty or null.
        }
        try {
            // Prepare a SQL statement to search users by email, first name, or last name
            String sql = "SELECT * FROM users WHERE email LIKE ? OR first_name LIKE ? OR last_name LIKE ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            String searchPattern = "%" + search + "%";
            statement.setString(1, searchPattern);
            statement.setString(2, searchPattern);
            statement.setString(3, searchPattern);

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                users.add(createAdapter(realm, resultSet));
            }
        } catch (Exception e) {
            logger.severe("Error searching for users: " + e.getMessage());
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

            List<Object> queryParameters = new ArrayList<>();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                String sqlField = PARAM_TO_SQL_FIELD.get(entry.getKey());
                if (sqlField != null) {
                    query.append(" AND ").append(sqlField).append(" = ?");
                    queryParameters.add(entry.getValue());
                }
            }
            query.append(" LIMIT ? OFFSET ?");
            queryParameters.add(maxResults);
            queryParameters.add(firstResult);

            PreparedStatement statement = connection.prepareStatement(query.toString());
            for (int i = 0; i < queryParameters.size(); i++) {
                statement.setObject(i + 1, queryParameters.get(i));
            }

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                users.add(createAdapter(realm, resultSet));
            }
        } catch (Exception e) {
            logger.severe("Error searching for users: " + e.getMessage());
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
