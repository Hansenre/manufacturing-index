package com.manufacturing.manufacturingindex.model;

import jakarta.persistence.*;

@Entity
public class Factory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String country;

    private String city;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User owner;

    // construtor vazio (JPA)
    public Factory() {}

    public Factory(String name, String country, String city, User owner) {
        this.name = name;
        this.country = country;
        this.city = city;
        this.owner = owner;
    }

    // getters e setters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCountry() {
        return country;
    }

    public String getCity() {
        return city;
    }

    public User getOwner() {
        return owner;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }
}
