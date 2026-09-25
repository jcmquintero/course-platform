package com.courses.platform.instructor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "instructors")
public class Instructor {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    private String bio;

    protected Instructor() {
    }

    public Instructor(String name, String email, String bio) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.email = email;
        this.bio = bio;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getBio() {
        return bio;
    }
}