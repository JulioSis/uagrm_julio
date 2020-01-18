package com.example.julio.apptaller_taller.Model;

/**
 * Created by Julio on 23/10/2019.
 */

public class User_Mecanico {
    String nombre;
    String password;
    String email;
    String telefono;
    String imageName;

    public User_Mecanico() {
    }

    public User_Mecanico(String nombre, String password, String email, String telefono, String imageName) {
        this.nombre = nombre;
        this.password = password;
        this.email = email;
        this.telefono = telefono;
        this.imageName = imageName;
    }

    public String getNombre() {
            return nombre;
        }

    public void setNombre(String nombre) {
            this.nombre = nombre;
        }

    public String getPassword() {
            return password;
        }

    public void setPassword(String password) {
            this.password = password;
        }

    public String getEmail() {
            return email;
        }

    public void setEmail(String email) {
            this.email = email;
        }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
}
