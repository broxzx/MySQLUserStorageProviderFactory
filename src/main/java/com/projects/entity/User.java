package com.projects.entity;

import lombok.Data;


@Data
public class User {

    private String _id;
    private String firstName;
    private String lastName;
    private String email;
    private String pinCode;
    private UserRoles userRole;

}
