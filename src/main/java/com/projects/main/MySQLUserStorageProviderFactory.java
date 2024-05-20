package com.projects.main;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.storage.UserStorageProviderFactory;

import java.util.ArrayList;
import java.util.List;

public class MySQLUserStorageProviderFactory implements UserStorageProviderFactory<MySQLUserStorageProvider> {

    public static final String PROVIDER_NAME = "mysql-user-storage-provider";

    @Override
    public MySQLUserStorageProvider create(KeycloakSession session, ComponentModel model) {
        return new MySQLUserStorageProvider(session, model);
    }



    @Override
    public String getId() {
        return PROVIDER_NAME;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        List<ProviderConfigProperty> configProperties = new ArrayList<>();
        return configProperties;
    }

}
