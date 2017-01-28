package com.rajan.apps.modulicalculator;


import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.util.Arrays;

/**
 * FFT holds methods that perform the actual fast-fourier transform of the microphone input, and
 * has variables that control the FFT specifications such as bin size and sample rate.  Peak
 * finding is also implemented here, as well as peak persistence methods.
 *
 * @author  Rajan Aggarwal
 * @version 1.0
 * @since   2017-01-23
 *
 * Copyright 2017, Rajan Aggarwal, All rights reserved.
 */

public class FFT {

    public Calculator returnActivity;

    private static final int sampleRate = 27000; //in Hertz, standard value for mp3 = 44100.
    //the Nyquist freq is twice the expected freq however, and 27000 is well above that.
    //this gives an overall FFT resolution of <12.7 Hz
    private int bufferSize;

    private TextView frequencyIdentification; //text view that shows dynamic frequency FFT
    //processing
    private EditText frequencyFieldFundamental;
    private EditText frequencyFieldTorsional;
    private Button fundamentalFreqButton;
    private Button torsionalFreqButton;

    private AudioRecord audioRecorder;
    private Handler methodHandler = new Handler();
    private static final int methodDelay = 100; //method delay in ms
    private static final double amplifier = 100; // scaled max/min range of the original 16 bit signal
    private int windowSize;

    private double lowestDominantFreq = 0; //the persistent lowest dominant frequency, initialized to 0.
    private int persistanceLength = 0; //time duration of lowest dom. freq, initialized to 0 ms.
    private static final int desiredPersistanceLength=300; //in milliseconds
    private static final int maxTotalFreqIdentificationTime = 6000; //timeout for freq ID. 6 seconds.


    private String whichField;
    private int totalFreqIdentificationTime;



    public FFT(Calculator calculator){
        returnActivity=calculator;

    }



    public void displayFrequencies(String whichFrequency) {
        whichField = whichFrequency;


        frequencyIdentification = (TextView) returnActivity.findViewById(R.id.textView6);
        frequencyFieldFundamental = (EditText) returnActivity.findViewById(R.id.editText10);
        frequencyFieldTorsional = (EditText) returnActivity.findViewById(R.id.editText13);
        fundamentalFreqButton = (Button) returnActivity.findViewById(R.id.button8);
        torsionalFreqButton = (Button) returnActivity.findViewById(R.id.button9);

        bufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        audioRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
        windowSize = (int) Math.pow(2, maxPowerOfTwo(bufferSize));

        audioRecorder.startRecording();


        totalFreqIdentificationTime = 0; //start the timer at 0 ms.
        displayFrequenciesRepeat.run();

    }

    Runnable displayFrequenciesRepeat = new Runnable() {

        public void run() {


            if(totalFreqIdentificationTime < maxTotalFreqIdentificationTime) {

                //buffer contains a array of shorts from the actual audioRecorder
                //bufferForFFT is a double array, scaled by the amplifier value so the range isn't -32k
                //  to 32k but rather -amplifier to amplifier.
                //bufferForFFT is also a shortened version of buffer, as its length is the largest
                //  power of two under buffer.length. This is necessary so that the FFT algorithm can
                //  operate properly.

                short[] buffer = new short[bufferSize];
                double[] bufferForFFT = new double[windowSize];
                audioRecorder.read(buffer, 0, bufferSize);


                //converting shorts to doubles in the bufferForFFT array, and scaling by amplifier
                for (int i = 0; i < windowSize; i++) {
                    bufferForFFT[i] = amplifier *
                            (double) Integer.parseInt(Short.toString(buffer[i])) / 32767;
                }

                //setting up the input to the FFT function as a Complex[] array.  The inputs to the FFT
                //  are technically complex numbers, but the imaginary components are 0.
                Complex[] FFTInput = new Complex[windowSize];
                for (int i = 0; i < windowSize; i++) {
                    FFTInput[i] = new Complex(bufferForFFT[i], 0);

                }

                Complex[] FFTOutput = performFFT(FFTInput);
                double[] peakFindResults = findPeaks(FFTOutput);



                frequencyIdentification.setText("Listening..."
                        + "\nFirst Freq. (Lowest) (Hz): " + Double.toString(peakFindResults[0])
                        + "\nSecond Freq. (Hz): " + Double.toString(peakFindResults[1])
                        + "\nThird Freq. (Hz): " + Double.toString(peakFindResults[2])
                        + "\nFourth Freq. (Highest) (Hz): " + Double.toString(peakFindResults[3]));

                if (peakFindResults[0] == lowestDominantFreq && persistanceLength < desiredPersistanceLength) {
                    persistanceLength += methodDelay;
                } else if (peakFindResults[0] == lowestDominantFreq && persistanceLength >= desiredPersistanceLength) {
                    if (whichField == "Fundamental") {
                        frequencyFieldFundamental.setText(Double.toString(lowestDominantFreq));
                        frequencyIdentification.setText("");
                    } else {
                        frequencyFieldTorsional.setText(Double.toString(lowestDominantFreq));
                        frequencyIdentification.setText("");
                    }

                    audioRecorder.stop();
                    fundamentalFreqButton.setEnabled(true); //re-enable both buttons after the function has finished
                    torsionalFreqButton.setEnabled(true);
                    return; //stop the runnable
                } else {
                    //basically if we have a new minimum frequency
                    //disallow frequencies near 60, 120, and 240 Hz (standard ambient AC frequencies)
                    if (Math.abs(peakFindResults[0] - 60) > 5 && Math.abs(peakFindResults[0] - 120) > 5 && Math.abs(peakFindResults[0] - 240) > 5) {
                        lowestDominantFreq = peakFindResults[0];
                    } else {
                        lowestDominantFreq = 0;
                    }
                    persistanceLength = 0;
                }


                methodHandler.postDelayed(displayFrequenciesRepeat, methodDelay);
                totalFreqIdentificationTime += methodDelay;

            }
            else{

                frequencyIdentification.setText("");
                audioRecorder.stop();
                fundamentalFreqButton.setEnabled(true);
                torsionalFreqButton.setEnabled(true);
                returnActivity.showTimeOutError();

            }

        }


    };

    //for a given integer input, maxPowerOfTwo returns the largest power of two that is smaller
    //  than or equal to that input.
    private int maxPowerOfTwo(int ceiling){
        int power = 0;
        while(ceiling>=1){
            ceiling = ceiling/2;
            power++;
        }
        return power-1;
    }





    private double[] findPeaks(Complex[] FFTResults){
        double tempMag = 0;
        double tempFreq = 0;

        double prevMag = 0;
        double nextMag = 0;


        double[][] FFTPeaks = new double[2][FFTResults.length];
        int FFTPeaksIndex = 0;

        int minFreqDistance = 200;
        int highestAllowedFreq = 5000;


        for(int i = 1; i<FFTResults.length-1; i++){
            //calculating dB magnitude
            tempMag = (10*Math.log10(FFTResults[i].re()*FFTResults[i].re() + FFTResults[i].im()*FFTResults[i].im()));
            prevMag = (10*Math.log10(FFTResults[i-1].re()*FFTResults[i-1].re() + FFTResults[i-1].im()*FFTResults[i-1].im()));
            nextMag = (10*Math.log10(FFTResults[i+1].re()*FFTResults[i+1].re() + FFTResults[i+1].im()*FFTResults[i+1].im()));

            //tempMag = FFTResults[i].re()*FFTResults[i].re() + FFTResults[i].im()*FFTResults[i].im();
            //prevMag = FFTResults[i-1].re()*FFTResults[i-1].re() + FFTResults[i-1].im()*FFTResults[i-1].im();
            //nextMag = FFTResults[i+1].re()*FFTResults[i+1].re() + FFTResults[i+1].im()*FFTResults[i+1].im();




            if(tempMag > prevMag && tempMag > nextMag) { //we have a peak
                tempFreq = sampleRate*i/windowSize;
                FFTPeaks[0][FFTPeaksIndex] = tempFreq;
                FFTPeaks[1][FFTPeaksIndex] = tempMag;
                FFTPeaksIndex++;

            }


        }

        double maxMag = 0;
        double maxFreq = 0;

        double medMag = 0;
        double medFreq = 0;

        double lowMag = 0;
        double lowFreq = 0;

        double bottomMag = 0;
        double bottomFreq = 0;

        for(int i = 0; i<FFTPeaksIndex+1; i++){
            if(FFTPeaks[1][i] > maxMag && FFTPeaks[0][i]<highestAllowedFreq){
                maxMag = FFTPeaks[1][i];
                maxFreq = FFTPeaks[0][i];
            }
        }

        for(int i = 0; i<FFTPeaksIndex+1; i++){
            if(FFTPeaks[1][i] > medMag && FFTPeaks[0][i]<highestAllowedFreq && Math.abs(FFTPeaks[0][i] - maxFreq) > minFreqDistance){
                medMag = FFTPeaks[1][i];
                medFreq = FFTPeaks[0][i];
            }
        }

        for(int i = 0; i<FFTPeaksIndex+1; i++){
            if(FFTPeaks[1][i] > lowMag && FFTPeaks[0][i]<highestAllowedFreq && Math.abs(FFTPeaks[0][i] - maxFreq) > minFreqDistance && Math.abs(FFTPeaks[0][i] - medFreq) > minFreqDistance){
                lowMag = FFTPeaks[1][i];
                lowFreq = FFTPeaks[0][i];
            }
        }

        for(int i = 0; i<FFTPeaksIndex+1; i++){
            if(FFTPeaks[1][i] > bottomMag && FFTPeaks[0][i]<highestAllowedFreq && Math.abs(FFTPeaks[0][i] - maxFreq) > minFreqDistance && Math.abs(FFTPeaks[0][i] - medFreq) > minFreqDistance && Math.abs(FFTPeaks[0][i] - lowFreq) > minFreqDistance){
                bottomMag = FFTPeaks[1][i];
                bottomFreq = FFTPeaks[0][i];
            }
        }




        double[] freqsToReturn = new double[4];
        freqsToReturn[0] = maxFreq;
        freqsToReturn[1] = medFreq;
        freqsToReturn[2] = lowFreq;
        freqsToReturn[3] = bottomFreq;

        Arrays.sort(freqsToReturn);


        return freqsToReturn;

    }




    // compute the FFT of x, assuming its length is a power of 2
    // basic Cooley Tukey algorithm implementaiton
    private static Complex[] performFFT(Complex[] x) {
        int n = x.length;

        // base case
        if (n == 1){
            return new Complex[]{x[0]};
        }


        // fft of even terms (recursive)
        Complex[] even = new Complex[n / 2];
        for (int k = 0; k < n / 2; k++) {
            even[k] = x[2 * k];
        }
        Complex[] q = performFFT(even);

        // fft of odd terms (recursive)
        Complex[] odd = new Complex[n/2];
        for (int k = 0; k < n / 2; k++) {
            odd[k] = x[2 * k + 1];
        }
        Complex[] r = performFFT(odd);

        // combination step.  Implements a collapsing root technique in the complex domain.
        Complex[] y = new Complex[n];
        for (int k = 0; k < n / 2; k++) {
            double kth = -2 * k * Math.PI / n;
            Complex wk = new Complex(Math.cos(kth), Math.sin(kth));
            y[k] = q[k].plus(wk.times(r[k]));
            y[k + n / 2] = q[k].minus(wk.times(r[k]));
        }
        return y;
    }
}