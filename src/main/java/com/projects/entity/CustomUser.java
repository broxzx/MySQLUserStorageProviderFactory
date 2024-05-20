package com.projects.entity;

import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.LegacyUserCredentialManager;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.SubjectCredentialManager;
import org.keycloak.models.UserModel;
import org.keycloak.storage.adapter.AbstractUserAdapter;

import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    private CustomUser(KeycloakSession session, RealmModel realm,
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
}
