package com.rajan.apps.modulicalculator;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

import java.math.BigDecimal;
import java.math.MathContext;



/**
 * The Calculator class acts as an orchestrator for the Calculator Activity page, calling other
 * methods and classes to execute the evaluation of the engineering modulii.
 *
 * @author  Rajan Aggarwal
 * @version 1.0
 * @since   2017-01-23
 *
 * Copyright 2017, Rajan Aggarwal, All rights reserved.
 */


public class Calculator extends AppCompatActivity implements OnItemSelectedListener {


    private Button fundamentalFreqButton;
    private Button torsionalFreqButton;

    private Spinner spinner;
    private RelativeLayout rectangularLayout;
    private RelativeLayout cylinderLayout;

    private final static int audioRecordPermissionCode=1;
    private String whichFrequency="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calculator);


        Toolbar myToolbar = (Toolbar) findViewById(R.id.calculator_toolbar);
        setSupportActionBar(myToolbar);
        setTitle("Calculator");
        myToolbar.setTitleTextColor(Color.WHITE);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);


        //setting up all the .xml elements that will be manipulated by the calculator.
        rectangularLayout =(RelativeLayout) this.findViewById(R.id.RectLayout);
        cylinderLayout = (RelativeLayout) this.findViewById(R.id.CylLayout);
        fundamentalFreqButton = (Button) findViewById(R.id.button8);
        torsionalFreqButton = (Button) findViewById(R.id.button9);


        //spinner for rectangular/cylindrical specimens
        spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.SpecimenSpinner, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        //change the layout of the calculator depending on specimen selection
        if(spinner.getSelectedItem().toString().equals("Cylindrical")){
            rectangularLayout.setVisibility(RelativeLayout.GONE);
            cylinderLayout.setVisibility(RelativeLayout.VISIBLE);
            EditText et = (EditText) this.findViewById(R.id.editText7);
            et.setNextFocusDownId(R.id.editText11);
        }
        else {
            rectangularLayout.setVisibility(RelativeLayout.VISIBLE);
            cylinderLayout.setVisibility(RelativeLayout.GONE);
            EditText et = (EditText) this.findViewById(R.id.editText7);
            et.setNextFocusDownId(R.id.editText4);
        }


        spinner.setOnItemSelectedListener(this);

    }



    //Responds to the "up"/"back" button press in the action bar.  Parent activity is the welcome
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }



    //Changes the calculator layout appearance depending on whether the specimen is rectangular
    //or cylindrical.
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        // here we change layout visibility again
        if(spinner.getSelectedItem().toString().equals("Cylindrical")){
            rectangularLayout.setVisibility(RelativeLayout.GONE);
            cylinderLayout.setVisibility(RelativeLayout.VISIBLE);
            EditText et = (EditText) this.findViewById(R.id.editText7);
            et.setNextFocusDownId(R.id.editText11);
        }
        else {
            rectangularLayout.setVisibility(RelativeLayout.VISIBLE);
            cylinderLayout.setVisibility(RelativeLayout.GONE);
            EditText et = (EditText) this.findViewById(R.id.editText7);
            et.setNextFocusDownId(R.id.editText4);
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
    }



    protected void onResume() {
        super.onResume();
    }





    //Called by the "Listen" button when the user wants the app to pickup dominant frequencies.
    //The corresponding EditText field is then filled in.
    public void populateFundamental(View view){
        whichFrequency="Fundamental";
        dealWithPermissions();
    }


    public void populateTorsional(View view){
        whichFrequency="Torsional";
        dealWithPermissions();
    }

    //Checks and proceeds with app microphone permissions for the frequency identification feature.
    public void dealWithPermissions(){

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        audioRecordPermissionCode);

        }

        else { //permission was already granted

            //disable both buttons temporarily so the user can't restart the thread mid-run.
            fundamentalFreqButton.setEnabled(false);
            torsionalFreqButton.setEnabled(false);

            FFT fourier = new FFT(this);
            fourier.displayFrequencies(whichFrequency);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case audioRecordPermissionCode: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    //disable both buttons temporarily so the user can't restart the thread mid-run.
                    fundamentalFreqButton.setEnabled(false);
                    torsionalFreqButton.setEnabled(false);

                    FFT fourier = new FFT(this);
                    fourier.displayFrequencies(whichFrequency);

                } else { //error message if permission is not granted.

                    createAndShowDialog("Error", "To use the frequency identification functionality, " +
                            "the app must be able to access your microphone.");
                }

            }
        }
    }



    //called by the FFT class; better to make a liason function that's public and to keep
    //createAndShowDialog() private.
    public void showTimeOutError(){
        createAndShowDialog("Timeout Error", "Failed to identify a persistent frequency.  " +
                "Please try again.");
    }


    //creates and shows an "OK" only dialog for a given title and message
    private void createAndShowDialog(String title, String message){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(title);

        builder
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {

                    }
                });

        AlertDialog alertDialog = builder.create();

        alertDialog.show();
    }



    // calls the Evaluator Class and calculates the modulii.
    public void calculateModulii(View view){

        //initialization of all the text fields
        EditText weightLBS = (EditText) findViewById(R.id.editText2);
        EditText weightOZS = (EditText) findViewById(R.id.editText6);
        EditText lengthFT = (EditText) findViewById(R.id.editText3);
        EditText lengthIN = (EditText) findViewById(R.id.editText7);
        EditText widthFT = (EditText) findViewById(R.id.editText4);
        EditText widthIN = (EditText) findViewById(R.id.editText8);
        EditText heightFT = (EditText) findViewById(R.id.editText5);
        EditText heightIN = (EditText) findViewById(R.id.editText9);
        EditText fundamentalFreq = (EditText) findViewById(R.id.editText10);
        EditText torsionalFreq = (EditText) findViewById(R.id.editText13);

        EditText diameterFT = (EditText) findViewById(R.id.editText11);
        EditText diameterIN = (EditText) findViewById(R.id.editText12);

        Evaluator eval = new Evaluator();
        spinner = (Spinner) findViewById(R.id.spinner);

        double weight = 0;
        double length = 0;
        double width = 0;
        double height = 0;
        double diameter = 0;


        //Error catching for incomplete data entries.
        if(spinner.getSelectedItem().toString().equals("Rectangular")){
            if(        weightLBS.getText().toString().equals("")
                    || weightOZS.getText().toString().equals("")
                    || lengthFT.getText().toString().equals("")
                    || lengthIN.getText().toString().equals("")
                    || widthFT.getText().toString().equals("")
                    || widthIN.getText().toString().equals("")
                    || heightFT.getText().toString().equals("")
                    || heightIN.getText().toString().equals("")
                    || fundamentalFreq.getText().toString().equals("")
                    || torsionalFreq.getText().toString().equals(""))

            {

                createAndShowDialog("Error", "Please fill in values for all of the fields.  " +
                        "If you don't think a field is necessary, just put in a 0.");
                return;
            }
            else if(    (weightLBS.getText().toString().equals("0") && weightOZS.getText().toString().equals("0"))
                    || (lengthFT.getText().toString().equals("0") && lengthIN.getText().toString().equals("0"))
                    || (widthFT.getText().toString().equals("0") && widthIN.getText().toString().equals("0"))
                    || (heightFT.getText().toString().equals("0") && heightIN.getText().toString().equals("0"))
                    ){
                createAndShowDialog("Error", "Please ensure non-zero geometry");
                return;


            }

        }
        else{
            if(        weightLBS.getText().toString().equals("")
                    || weightOZS.getText().toString().equals("")
                    || lengthFT.getText().toString().equals("")
                    || lengthIN.getText().toString().equals("")
                    || diameterFT.getText().toString().equals("")
                    || diameterIN.getText().toString().equals("")
                    || fundamentalFreq.getText().toString().equals("")
                    || torsionalFreq.getText().toString().equals(""))

            {

                createAndShowDialog("Error", "Please fill in values for all of the fields.  " +
                        "If you don't think a field is necessary, just put in a 0.");
                return;
            }
            else if(    (weightLBS.getText().toString().equals("0") && weightOZS.getText().toString().equals("0"))
                    || (lengthFT.getText().toString().equals("0") && lengthIN.getText().toString().equals("0"))
                    || (diameterFT.getText().toString().equals("0") && diameterIN.getText().toString().equals("0"))
                    ){
                createAndShowDialog("Error", "Please ensure non-zero geometry");
                return;


            }
        }




        //Error catching for two null or two zero frequency input values.
        if((fundamentalFreq.getText().toString().matches("")
            && torsionalFreq.getText().toString().matches(""))
                || (fundamentalFreq.getText().toString().matches("0")
                && torsionalFreq.getText().toString().matches("0"))){


            createAndShowDialog("Error", "Please fill in a value for at least one frequency field.  " +
                    "See the help docs for more information.");
            return;


        }

        //if we make it here, all the geometric fields are filled and there is at least
        //one frequency field filled (the other is 0 or filled also)

        //weight is in lbs, linear dimensions are in inches.
        if(spinner.getSelectedItem().toString().equals("Rectangular")){
            weight = Double.parseDouble(weightLBS.getText().toString())
                    + Double.parseDouble(weightOZS.getText().toString())/16;
            length = Double.parseDouble(lengthFT.getText().toString())*12
                    + Double.parseDouble(lengthIN.getText().toString());
            width = Double.parseDouble(widthFT.getText().toString())*12
                    + Double.parseDouble(widthIN.getText().toString());
            height = Double.parseDouble(heightFT.getText().toString())*12
                    + Double.parseDouble(heightIN.getText().toString());
        }
        else{
            weight = Double.parseDouble(weightLBS.getText().toString())
                    + Double.parseDouble(weightOZS.getText().toString())/16;
            length = Double.parseDouble(lengthFT.getText().toString())*12
                    + Double.parseDouble(lengthIN.getText().toString());
            diameter = Double.parseDouble(diameterFT.getText().toString())*12
                    + Double.parseDouble(diameterIN.getText().toString());

        }




        if(spinner.getSelectedItem().toString().equals("Rectangular")){


            //want to calculate E only, if possible
            if(fundamentalFreq.getText().toString() != ""
                    && torsionalFreq.getText().toString().matches("0")){
                if(length/height>=20){
                    double E = eval.calculateE(weight, length, width, height,
                            Double.parseDouble(fundamentalFreq.getText().toString()));
                    if(checkOutputValue(E)) {

                        createAndShowDialog("Calculation Results", "Young's Modulus: " +
                                roundDouble(E) + " psi"
                                + "\n\n\nNote: Four significant digits provided, use at own discretion");
                        return;
                    }
                    else{
                        createAndShowDialog("Error", "Impossible results obtained; please" +
                                " ensure input values are accurate");
                        return;
                    }

                }
                else{

                    createAndShowDialog("Error", "Due to the given geometry, an iterative " +
                            "technique must be utilized.  Both frequencies will be required for "+
                            "further calculation");
                    return;

                }


            }


            //want to calculate G only
            if(fundamentalFreq.getText().toString().equals("0")
                    && torsionalFreq.getText().toString() != ""){
                double G = eval.calculateG(weight, length, width, height,
                        Double.parseDouble(torsionalFreq.getText().toString()));

                if(checkOutputValue(G)) {

                    createAndShowDialog("Calculation Results", "Shear Modulus: " +
                            roundDouble(G) + " psi"
                            + "\n\n\nNote: Four significant digits provided, use at own discretion");
                    return;
                }
                else{
                    createAndShowDialog("Error", "Impossible results obtained; please" +
                            " ensure input values are accurate");
                    return;
                }


            }


            //basically a calculate-all function
            if(fundamentalFreq.getText().toString() != "" &&
                    torsionalFreq.getText().toString() != "") {
                double[] results = eval.calculateAll(weight, length, width, height,
                        Double.parseDouble(fundamentalFreq.getText().toString()),
                        Double.parseDouble(torsionalFreq.getText().toString()));


                if(checkOutputValue(results)) {
                    createAndShowDialog("Calculation Results", "Young's Modulus: "
                            + roundDouble(results[0]) + " psi" + "\nShear Modulus: "
                            + roundDouble(results[1]) + " psi" +
                            "\nPoisson Ratio: " + roundDouble(results[2])
                            + "\n\n\nNote: Four significant digits provided, use at own discretion");
                    return;
                }
                else{
                    createAndShowDialog("Error", "Impossible results obtained; please" +
                            " ensure input values are accurate");
                    return;
                }


            }


        }
        else{ //we're dealing with a cylindrical sample


            //want to calculate E only, if possible
            if(fundamentalFreq.getText().toString() != ""
                    && torsionalFreq.getText().toString().matches("0")){
                if(length/diameter>=20){
                    double E = eval.calculateE(weight, length, diameter,
                            Double.parseDouble(fundamentalFreq.getText().toString()));

                    if(checkOutputValue(E)) {

                        createAndShowDialog("Calculation Results", "Young's Modulus: "
                                + roundDouble(E) + " psi"
                                + "\n\n\nNote: Four significant digits provided, use at own discretion");
                        return;
                    }
                    else{
                        createAndShowDialog("Error", "Impossible results obtained; please" +
                                " ensure input values are accurate");
                        return;
                    }

                }
                else{
                    createAndShowDialog("Error", "Due to the given geometry, an iterative technique must " +
                            "be utilized.  Both frequencies will be required for further" +
                            " calculation");
                    return;

                }


            }


            //want to calculate G only
            if(fundamentalFreq.getText().toString().equals("0")
                    && torsionalFreq.getText().toString() != ""){
                double G = eval.calculateG(weight, length, diameter,
                        Double.parseDouble(torsionalFreq.getText().toString()));

                if(checkOutputValue(G)) {

                    createAndShowDialog("Calculation Results", "Shear Modulus: " + roundDouble(G) + " psi"
                            + "\n\n\nNote: Four significant digits provided, use at own discretion");
                    return;
                }
                else{
                    createAndShowDialog("Error", "Impossible results obtained; please" +
                            " ensure input values are accurate");
                    return;
                }


            }


            //basically a calculate-all function
            if(fundamentalFreq.getText().toString() != "" &&
                    torsionalFreq.getText().toString() != "") {
                double[] results = eval.calculateAll(weight, length, diameter,
                        Double.parseDouble(fundamentalFreq.getText().toString()),
                        Double.parseDouble(torsionalFreq.getText().toString()));

                if(checkOutputValue(results)) {

                    createAndShowDialog("Calculation Results",
                            "Young's Modulus: " + roundDouble(results[0]) + " psi" +
                                    "\nShear Modulus: " + roundDouble(results[1]) + " psi" +
                                    "\nPoisson Ratio: " + roundDouble(results[2])
                                    + "\n\n\nNote: Four significant digits provided, use at own discretion");
                    return;
                }
                else{
                    createAndShowDialog("Error", "Impossible results obtained; please" +
                            " ensure input values are accurate");
                    return;
                }


            }
        }
    }


    public boolean checkOutputValue(double input){
        if(Double.isNaN(input)){
            return false;
        }
        else {
            return true;
        }
    }

    public boolean checkOutputValue(double[] input){
        for(double val : input){
            if(Double.isNaN(val)){
                return false;
            }
        }
        return true;
    }


    //This method rounds a double to 4 significant figures
    //It still rounds things appropriately even if answers are negative (impossible) - at least
    //the output will still look nice and the user can then see that he has to double check
    //his inputs.

    public Number roundDouble(double input) {
        BigDecimal bd = new BigDecimal(input);
        bd = bd.round(new MathContext(4));
        return bd;
    }

    public void openGeometryHelp(View view){
        createAndShowDialog("Help", "Fill in the corresponding sections with the geometric " +
                "dimensions.  The fields are additive, e.g. to input \"3 ft 4 inches\" you could type " +
                "in (3\',4\"), (0\',40\"), (2\',16\") etc. For a sample image showing which dimensions are which, " +
                "please see the full help document.");

    }

    public void openFrequencyHelp(View view){
        createAndShowDialog("Help", "If you know the resonant frequencies off hand, feel free to fill in the "+
                "corresponding text fields with those values.  However, if you need the app to help " +
                "determine those values for you, set-up the sample in the orientation prescribed in the "+
                "help document, tap it at the defined point, place your microphone near the point of excitation (an anti-node),"
                + " and press \"Listen.\" " +
                "\n\nSometimes, the app will be unable to identify the dominant frequency due to "
                +"ambient noise, weak resonance strength, or the presence of too many harmonics." +
                " If this is the case, please see the help document for more information on other options.");

    }

    public void openSampleHelp(View view){
        createAndShowDialog("Help", "Select your sample geometry from the drop-down menu below." +
                " If your sample is neither rectangular or cylindrical, there is unfortunately no " +
                "easy way to calculate the moduli; however, if the piece is disposable, taking " +
                "it down on a mill or lathe to match one of the geometries is a possible solution.");

    }


}