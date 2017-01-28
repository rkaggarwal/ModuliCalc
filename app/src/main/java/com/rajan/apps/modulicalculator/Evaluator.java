package com.rajan.apps.modulicalculator;

/**
 * The Evaluator Class holds all of the explicit and iterative formulas that input key geometries
 * and frequencies and output the modulii values.
 *
 * @author  Rajan Aggarwal
 * @version 1.0
 * @since   2017-01-23
 *
 * Copyright 2017, Rajan Aggarwal, All rights reserved.
 */

public class Evaluator {

    private static final double convergenceLimit = .0001;

    public Evaluator(){}


    //FOR RECTANGULAR SPECIMENS
    //this method only gets called if length/height >= 20, so an explicit formula can be used.
    //inputs are in lbs and inches.
    public double calculateE(double weight, double length, double width, double height, double fundamentalFreq) {
        //Young's modulus
        double E;

        weight *= 453.592; //convert lbs to grams
        length *= 25.4; //convert in to mm
        width *= 25.4;
        height *= 25.4;

        double correctionFactor;

        correctionFactor = 1 + 6.585 * Math.pow((height / length), 2);
        E = .9465 * (weight * Math.pow(fundamentalFreq, 2) / width) * Math.pow((length / height), 3) * correctionFactor;
        return E/6894.76; //convert Pa to psi

    }

    //for shear iterative schemes never need to be used, explicit formulas are always acceptable.
    public double calculateG(double weight, double length, double width, double height, double torsionalFreq) {
        //correction factors
        double A;
        double B;
        //shear modulus
        double G;

        weight *= 453.592; //convert lbs to grams
        length *= 25.4; //convert in to mm
        width *= 25.4;
        height *= 25.4;

        B = (width/height + height/width)/(4*height/width - 2.52*Math.pow(height/width, 2)*.21*Math.pow(height/width, 6));
        A = (.5062-.8776*width/height + .3504*Math.pow(width/height, 2) - .0078*Math.pow(width/height, 3))
                / (12.03*width/height + 9.892*Math.pow(width/height, 2));

        G = 4*length*weight*Math.pow(torsionalFreq, 2)/width/height * (B/(1+A));
        return G/6894.76; //convert Pa to psi

    }

    public double[] calculateAll(double weight, double length, double width, double height, double fundamentalFreq, double torsionalFreq) {
        double E = 0; //Young's Modulus;
        double G = 0; //Shear Modulus;
        double u_old, u_new; //Poisson's Ratio
        double A, B, correctionFactor; //various correction factors
        double[] results = new double[3];

        weight *= 453.592; //convert lbs to grams
        length *= 25.4; //convert in to mm
        width *= 25.4;
        height *= 25.4;


        //dummy initial guesses for Poisson's Ratio.
        u_old = .4;
        u_new = .5;

        while(Math.abs(u_new-u_old)>convergenceLimit) {
            u_old = u_new;
            correctionFactor = 1+6.585*(1+.0752*u_old+.8109*Math.pow(u_old,2))*Math.pow((height/length), 2)
                    - .868*Math.pow(height/length, 4)-((8.340*(1+.2023*u_old+2.173*Math.pow(u_old, 2))
                    * Math.pow(height/length, 4))/(1+6.338*(1+.1408*u_old+1.536*Math.pow(u_old,2))
                    * (Math.pow(height/length, 2))));
            E = .9465*(weight*Math.pow(fundamentalFreq, 2)/width)*Math.pow((length/height), 3)*correctionFactor;
            B = (width/height + height/width)/(4*height/width - 2.52*Math.pow(height/width, 2)*.21*Math.pow(height/width, 6));
            A = (.5062-.8776*width/height + .3504*Math.pow(width/height, 2) - .0078*Math.pow(width/height, 3))
                    / (12.03*width/height + 9.892*Math.pow(width/height, 2));

            G = 4*length*weight*Math.pow(torsionalFreq, 2)/width/height * (B/(1+A));
            u_new = E/2/G - 1;

        }
        //once we reach here, u_old = u_new, and the values for E and G are consistent.
        results[0] = E/6894.76; //convert Pa to psi
        results[1] = G/6894.76;
        results[2] = u_new;

        return results;

    }





    //FOR CYLINDRICAL SPECIMENS (methods are overloaded)

    //To determine Young's modulus, only gets called if length/diameter>=20 and an explicit relation can be used.
    public double calculateE(double weight, double length, double diameter, double fundamentalFreq){
        //Young's Modulus
        double E;

        weight *= 453.592; //convert lbs to grams
        length *= 25.4; //convert in to mm
        diameter *= 25.4;

        double correctionFactor;

        correctionFactor = 1 + 4.939*Math.pow(diameter/length, 2);
        E = 1.6067*Math.pow(length, 3)/Math.pow(diameter, 4)*weight*Math.pow(fundamentalFreq,2)
                *correctionFactor;
        return E/6894.76;

    }

    public double calculateG(double weight, double length, double diameter, double torsionalFreq){
        return 16*weight*Math.pow(torsionalFreq,2)*(length/(3.141592*Math.pow(diameter,2)))/6894.76;
    }


    public double[] calculateAll(double weight, double length, double diameter,
                                 double fundamentalFreq, double torsionalFreq){

        double E = 0; //Young's Modulus;
        double G = 0; //Shear Modulus;
        double u_old, u_new; //Poisson's Ratio
        double correctionFactor; //various correction factors
        double[] results = new double[3];

        weight *= 453.592; //convert lbs to grams
        length *= 25.4; //convert in to mm
        diameter *= 25.4;


        //dummy initial guesses for Poisson's Ratio.
        u_old = .4;
        u_new = .5;

        while(Math.abs(u_new-u_old)>convergenceLimit) {
            u_old = u_new;
            correctionFactor = 1 + 4.939*Math.pow(diameter/length, 2);
            E = 1.6067*Math.pow(length, 3)/Math.pow(diameter, 4)*weight*Math.pow(fundamentalFreq,2)
                    *correctionFactor;
            G = 16*weight*Math.pow(torsionalFreq,2)*(length/(3.141592*Math.pow(diameter,2)));
            u_new = E/2/G - 1;

        }
        //once we reach here, u_old = u_new, and the values for E and G are consistent.
        results[0] = E/6894.76;
        results[1] = G/6894.76;
        results[2] = u_new;

        return results;


    }

}
