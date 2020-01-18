package com.example.julio.apptaller.Model;

/**
 * Created by Julio on 31/08/2018.
 */

public class User {
    private String email, password, name,  telefono, imageName;

    public User(){
    }

    public User(String Email,String Password,String Name, String Telefono, String imageName){
        this.email = Email;
        this.password = Password;
        this.name = Name;
        this.telefono = Telefono;
        this.imageName = imageName;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String registro) {
        this.name = registro;
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
