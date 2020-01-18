package com.example.julio.apptaller.Model;

/**
 * Created by Julio on 20/11/2019.
 */

public class Vehiculo {
    String placa;
    String tipo;
    String marca;
    String modelo;
    String anio;
    String imageName;

    public Vehiculo() {
    }

    public Vehiculo(String placa, String tipo, String marca, String modelo, String anio, String imageName) {
        this.placa = placa;
        this.tipo = tipo;
        this.marca = marca;
        this.modelo = modelo;
        this.anio = anio;
        this.imageName = imageName;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getAnio() {
        return anio;
    }

    public void setAnio(String anio) {
        this.anio = anio;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
}
