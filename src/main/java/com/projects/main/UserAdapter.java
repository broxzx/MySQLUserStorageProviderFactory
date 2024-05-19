package com.projects.main;

import com.projects.entity.User;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.adapter.AbstractUserAdapterFederatedStorage;

public class UserAdapter extends AbstractUserAdapterFederatedStorage {

    private final User user;
    private String keycloakId;

    public UserAdapter(KeycloakSession session, RealmModel realm, ComponentModel componentModel, User user) {
        super(session, realm, componentModel);
        this.user = user;
        if (user != null) {
            keycloakId = StorageId.keycloakId(componentModel, user.get_id().toString());
        }
        setEnabled(true);
    }

    @Override
    public String getUsername() {
        return user.getFirstName();
    }

    @Override
    public void setUsername(String s) {
        user.setFirstName(s);
    }
}
