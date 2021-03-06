package com.example.color_formulation;

public class LAB {
    protected double L, A, B;

    protected int illuminant;

    public LAB( double L, double A, double B) {
        this(L, A, B, ColorTransformation.ILLUMINANT_D65_10_DEGREES);
    }

    public LAB( double L, double A, double B, int illuminant) {
        this.L = L;
        this.A = A;
        this.B = B;
        this.illuminant = illuminant;
    }

    public double getL() { return L; }

    public double getA() {
        return A;
    }

    public double getB() {
        return B;
    }

    public int getIlluminant() { return illuminant; }

}