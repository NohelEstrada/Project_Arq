package com.sources.app.dto;

import java.util.Date;

/**
 * DTO para la creación de usuarios que encapsula los múltiples parámetros
 * necesarios para crear un nuevo usuario.
 */
public class UserCreateRequest {
    private String name;
    private String cui;
    private String phone;
    private String email;
    private Date birthDate;
    private String address;
    private String password;

    public UserCreateRequest() {}

    public UserCreateRequest(String name, String cui, String phone, String email, 
                           Date birthDate, String address, String password) {
        this.name = name;
        this.cui = cui;
        this.phone = phone;
        this.email = email;
        this.birthDate = birthDate;
        this.address = address;
        this.password = password;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCui() { return cui; }
    public void setCui(String cui) { this.cui = cui; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Date getBirthDate() { return birthDate; }
    public void setBirthDate(Date birthDate) { this.birthDate = birthDate; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
