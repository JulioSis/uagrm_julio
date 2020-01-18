package com.example.julio.apptaller.Model;

/**
 * Created by Julio on 11/10/2019.
 */

public class Taller {
    private String Nombre;
    private String Direccion;
    private String Telefono;
    private String Horario;
    private String Latitud;
    private String Longitud;
    private String Imagen;

    public Taller() {
    }

    public Taller(String nombre, String direccion, String telefono, String horario,String latitud, String longitud, String imagen) {
        Nombre = nombre;
        Direccion = direccion;
        Telefono = telefono;
        Horario = horario;
        Latitud = latitud;
        Longitud = longitud;
        Imagen = imagen;
    }

    public String getNombre() {
        return Nombre;
    }

    public void setNombre(String nombre) {
        Nombre = nombre;
    }

    public String getDireccion() {
        return Direccion;
    }

    public void setDireccion(String direccion) {
        Direccion = direccion;
    }

    public String getTelefono() {
        return Telefono;
    }

    public void setTelefono(String telefono) {
        Telefono = telefono;
    }

    public String getHorario() {
        return Horario;
    }

    public void setHorario(String horario) {
        Horario = horario;
    }

    public String getLatitud() {
        return Latitud;
    }

    public void setLatitud(String latitud) {
        Latitud = latitud;
    }

    public String getLongitud() {
        return Longitud;
    }

    public void setLongitud(String longitud) {
        Longitud = longitud;
    }

    public String getImagen() {
        return Imagen;
    }

    public void setImagen(String imagen) {
        Imagen = imagen;
    }
}
