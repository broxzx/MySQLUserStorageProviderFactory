package com.projects.entity;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.LegacyUserCredentialManager;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.SubjectCredentialManager;
import org.keycloak.models.UserModel;
import org.keycloak.storage.adapter.AbstractUserAdapter;

import java.sql.*;
import java.util.List;
import java.util.Map;

@Slf4j
public class CustomUser extends AbstractUserAdapter {

    private String _id;

    private String firstName;
    private String lastName;
    private String email;


    private String pinCode;
    private String phoneNumber;
    private String avatar;


    private UserRoles userRole;

    private static Connection connection;

    static {
        try {
            connection = DriverManager.getConnection(
                    "jdbc:mysql://my-sql-db:3306/user-db",
                    "admin",
                    "admin");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private CustomUser(KeycloakSession session, RealmModel realm,
                       ComponentModel storageProviderModel,
                       String email,
                       String firstName,
                       String lastName) throws SQLException {
        super(session, realm, storageProviderModel);
//        PreparedStatement preparedStatement = connection.prepareStatement("select * from users where email = (?)");
//        preparedStatement.setString(1, email);
//        ResultSet resultSet = preparedStatement.executeQuery();
//        if (resultSet.next()) {
//            this._id = resultSet.getString(1);
//            this.firstName = resultSet.getString(2);
//            this.lastName = resultSet.getString(3);
//            this.email = email;
//            this.pinCode = resultSet.getString(4);
//            this.phoneNumber = resultSet.getString(5);
//            this.userRole = UserRoles.valueOf(resultSet.getString(5));
//        }
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
    public void setUsername(String username) {
//        log.info("setting username to user with email: {}", email);
//
//        try {
//            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE users SET email = ? WHERE email = ?");
//            preparedStatement.setString(1, username);
//            preparedStatement.setString(2, email);
//            preparedStatement.executeUpdate();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        this.email = username;
    }


    @Override
    public void setFirstName(String firstName) {
//        log.info("setting first name to user with email: {}", email);
//
//        try {
//            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE users SET first_name = ? WHERE email = ?");
//            preparedStatement.setString(1, firstName);
//            preparedStatement.setString(2, email);
//            preparedStatement.executeUpdate();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        this.firstName = firstName;
    }

    @Override
    public void setLastName(String lastName) {
//        log.info("setting last name to user with email: {}", email);
//
//        try {
//            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE users SET last_name = ? WHERE email = ?");
//            preparedStatement.setString(1, lastName);
//            preparedStatement.setString(2, email);
//            preparedStatement.executeUpdate();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        this.lastName = lastName;
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


    //todo: there is might a problem (setting email and firstname)
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

        public CustomUser build() throws SQLException {
            return new CustomUser(
                    session,
                    realm,
                    storageProviderModel,
                    email,
                    firstName,
                    lastName);

        }
    }
}
