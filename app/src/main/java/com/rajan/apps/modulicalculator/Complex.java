package com.rajan.apps.modulicalculator;

/**
 * The Complex Class is a helper class that deals with the complex variables and their specific
 * arithmetic.
 *
 * @author  Rajan Aggarwal
 * @version 1.0
 * @since   2017-01-23
 *
 * Copyright 2017, Rajan Aggarwal, All rights reserved.
 */

public class Complex {


    private double real;   // real component
    private double imag;   // complex component


    public Complex(double realComponent, double imaginaryComponent) {
        real = realComponent;
        imag = imaginaryComponent;
    }

    public double re() {
        return real;
    }

    public double im() {
        return imag;
    }


    // returns this + (Complex) b.
    public Complex plus(Complex b) {
        Complex a = this;             // invoking object
        double real = a.real + b.real;
        double imag = a.imag + b.imag;
        return new Complex(real, imag);
    }

    // returns this - (Complex) b.
    public Complex minus(Complex b) {
        Complex a = this;
        double real = a.real - b.real;
        double imag = a.imag - b.imag;
        return new Complex(real, imag);
    }

    // returns this * (Complex) b.
    public Complex times(Complex b) {
        Complex a = this;
        double real = a.real * b.real - a.imag * b.imag;
        double imag = a.real * b.imag + a.imag * b.real;
        return new Complex(real, imag);
    }

}