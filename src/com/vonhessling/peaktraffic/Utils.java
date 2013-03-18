package com.vonhessling.peaktraffic;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Utils {
    
    
    /**
     * Reads all or the given number of lines from the file with the given name. Trims leading/trailing whitespace from lines.
     * This method is intended for small to medium sized files (e.g. configuration files), not for huge files.
     *   
     * @param fileName The file name to read from. Absolute or relative names are OK. 
     * @param numLines The number of lines to read, or a negative int for all.  Since the lines are stored in-memory, the number should not huge. 
     * @return Returns a list of string containing the file's lines, in-order from top to bottom.
     * @throws FileNotFoundException Throws exception if file is not found.
     * @throws IOException Throws exception if other I/O error occurred.
     */
    public static List<String> readLines(String fileName, int numLines) throws FileNotFoundException, IOException {
        List<String> lines = new ArrayList<String>();
        BufferedReader inReader = new BufferedReader(new FileReader(fileName));
        String curLine;
        int i = 0;
        while ((curLine = inReader.readLine()) != null) {
            if ((numLines >= 0) && (i >= numLines)) {
                break;
            }
            lines.add(curLine.trim());
            i++;
        }
        return lines;
    }
    
    
    /**
     * Returns all combinations of a given list of strings where the group size has is between a certain minimum value/ maximum value (inclusive)
     * @param minGroupSize The minimum size for the groups to find
     * @param maxGroupSize The maximum size for the groups to find
     * @param all The list of strings
     * @return A list of all sub-groups matching the given criteria
     */
    public static List<List<String>> getAllCombinations(int minGroupSize, int maxGroupSize, List<String> all) {
        List<List<String>> allCombinations = new ArrayList<List<String>>();
        for (int curSize = minGroupSize; curSize <= maxGroupSize; curSize++) {
            allCombinations.addAll(getCombinations(curSize, all));
        }
        return allCombinations;
    }
    
    
    /**
     * Returns an ordered list of k-combinations of the given list where k == groupSize.
     * @param groupSize The number of elements each subset is supposed to have
     * @param all A list of strings
     */
    public static List<List<String>> getCombinations(int groupSize, List<String> all) {
        List<List<Integer>> combinationIndicesList = Utils.getTCombinations(groupSize, all.size());
        List<List<String>> combinations = new ArrayList<List<String>>();
        List<Integer> indices;
        for (int i = 0; i < combinationIndicesList.size(); i++) { // loop over all available combinations of indices
            List<String> combination = new ArrayList<String>();
            indices = combinationIndicesList.get(i);
            for (int j = 0; j < indices.size(); j++) {    // loop over all individual indices
                combination.add(all.get(indices.get(j))); // convert current index into a string
            }
            combinations.add(combination);
        }
        return combinations;
    }
    
    
    
    /**
     * Generates the list of all t-combinations of indices of items from 0...n-1. 
     * Implementation description: http://www-cs-faculty.stanford.edu/~knuth/fasc3a.ps.gz, page 9: Algorithm T (faster than Algorithm L)
     * @param t The value for t: the number of desired items in the subset(s)
     * @param n The value for n: the set's size
     * @return Returns all possible (ordered) t-combinations of n 
     */
    public static List<List<Integer>> getTCombinations(int t, int n) {
        if ((t > n) || ((n < 1) || (t < 1))) {
            throw new IllegalArgumentException("Error: need to supply n and t values both > 1, where n >= t. You supplied n: " + n + ", t: " + t);
        }
        List<List<Integer>> combinations = new ArrayList<List<Integer>>();
        
        if (t == n) { // special case, only one combination with all indices
            List<Integer> combination = new ArrayList<Integer>();
            for (int i = 1; i <= t; i++) {
                combination.add(i - 1);
            }
            combinations.add(combination);
            return combinations;
        }
        
        // Step T1: Initialize
        int c[] = new int[t + 3];
        for (int j = 1; j <= t; j++) {
            c[j] =  j - 1;
        }
        c[t + 1] = n;
        c[t + 2] = 0;
        int j = t; 
        
        while (true) {
            // Step T2: Visit
            // j is now the smallest index such that c[j + 1] > j 
            // harvest c[1] through c[t]
            List<Integer> combination = new ArrayList<Integer>();
            for (int i = 1; i <= t; i++) {
                combination.add(c[i]);
            }
            combinations.add(combination);
            
            int x;
            if (j > 0) {
                x = j; 
                // GOTO Step 6
            } else {
                // Step T3: Easy case
                if (c[1] + 1 < c[2]) { 
                    c[1]++;
                    continue; // GOTO Step 2
                } else {
                    j = 2;
                }
                
                boolean repeat;                
                // Step 4: Find j
                do {
                    c[j - 1] = j - 2;
                    x = c[j] + 1;
                    repeat = false;                    
                    if (x == c[j + 1]) {
                        j++;
                        repeat = true; // I'm too proud to write a GOTO here...                        
                    }
                } while (repeat);
                
                // Step 5: Done?
                if (j > t) {   
                    break;
                }
            } // end GOTO Step 6 from Step 2
            
            // Step 6: Increase c[j]
            c[j] = x;
            j--;
        }
        return combinations;
    }

    
}
