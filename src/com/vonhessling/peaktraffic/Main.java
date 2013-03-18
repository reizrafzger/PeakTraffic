package com.vonhessling.peaktraffic;

import java.io.FileNotFoundException;
import java.io.IOException;

public class Main {

    /**
     * Main class calling the cluster detector algorithm for the given input file name
     * @param args Requires the input file name as a single parameter. 
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        if (args.length < 1) {
            System.err.println("Error: need to provide file name containing input!  Suggestion: try var/peaktraffic-9erDuplicates.txt");
            System.exit(-1);
        }
        new OnlineDetector().findClusters(args[0]);
    }
}
