package com.projects.entity;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.LegacyUserCredentialManager;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.SubjectCredentialManager;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.adapter.AbstractUserAdapter;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
public class UserAdapter extends AbstractUserAdapter {

    protected User entity;
    protected String keycloakId;

    public UserAdapter(KeycloakSession session, RealmModel realm, ComponentModel model, User entity) {
        super(session, realm, model);
        this.entity = entity;
        keycloakId = StorageId.keycloakId(model, entity.get_id());
    }

    public String getPassword() {
        return entity.getPinCode();
    }

    public void setPassword(String pinCode) {
        entity.setPinCode(pinCode);
    }


    @Override
    public void setEmail(String email) {
        entity.setEmail(email);
    }

    @Override
    public String getUsername() {
        return entity.getEmail();
    }

    @Override
    public String getFirstName() {
        return entity.getFirstName();
    }

    @Override
    public String getLastName() {
        return entity.getLastName();
    }

    @Override
    public String getEmail() {
        return entity.getEmail();
    }

    @Override
    public String getId() {
        return keycloakId;
    }

    //todo: replaced phone don't forget to change it back
    @Override
    public void setSingleAttribute(String name, String value) {
        if (name.equals("phone")) {
            entity.setLastName(value);
        } else {
            super.setSingleAttribute(name, value);
        }
    }

    @Override
    public void removeAttribute(String name) {
        if (name.equals("phone")) {
            entity.setLastName(null);
        } else {
            super.removeAttribute(name);
        }
    }

    @Override
    public void setAttribute(String name, List<String> values) {
        if (name.equals("phone")) {
            entity.setLastName(values.get(0));
        } else {
            super.setAttribute(name, values);
        }
    }

    @Override
    public String getFirstAttribute(String name) {
        if (name.equals("phone")) {
            return entity.getLastName();
        } else {
            return super.getFirstAttribute(name);
        }
    }

    @Override
    public Map<String, List<String>> getAttributes() {
        Map<String, List<String>> attrs = super.getAttributes();
        MultivaluedHashMap<String, String> all = new MultivaluedHashMap<>();
        all.putAll(attrs);
        all.add("phone", entity.getLastName());
        return all;
    }

    @Override
    public Stream<String> getAttributeStream(String name) {
        if (name.equals("phone")) {
            List<String> phone = new LinkedList<>();
            phone.add(entity.getLastName());
            return phone.stream();
        } else {
            return super.getAttributeStream(name);
        }
    }

    @Override
    public SubjectCredentialManager credentialManager() {
        return new LegacyUserCredentialManager(session, realm, this);
    }


}
