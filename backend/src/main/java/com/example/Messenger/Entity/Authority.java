package com.example.Messenger.Entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name="authority")
public class Authority   {
    @Id
    private String id;
    private String name;
    @ManyToMany(mappedBy = "authorities")
    private List<User> user;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<User> getUser() {
        return user;
    }

    public void setUser(List<User> user) {
        this.user = user;
    }
}