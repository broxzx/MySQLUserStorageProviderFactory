package com.projects.entity;

import jakarta.persistence.*;
import lombok.Data;


@Data
@Entity
@Table(name = "users")
@NamedQueries({
        @NamedQuery(name = "getUserByUsername", query = "select u from User u where u.email = :email"),
        @NamedQuery(name = "getUserByEmail", query = "select u from User u where u.email = :email"),
        @NamedQuery(name = "getUserCount", query = "select count(u) from User u"),
        @NamedQuery(name = "getAllUsers", query = "select u from User u"),
        @NamedQuery(name = "searchForUser", query = "select u from User u where " +
                                                    "( lower(u.email) like :search or u.email like :search ) order by u.email"),
})
public class User {

    @Id
    private String _id;

    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;
    @Column(name = "email", unique = true)
    private String email;
    @Column(name = "pin_code")
    private String pinCode;
    @Enumerated(EnumType.STRING)
    @Column(name = "user_role")
    private UserRoles userRole;

}
