package io;

import functions.ArrayTabulatedFunction;
import functions.LinkedListTabulatedFunction;
import functions.TabulatedFunction;
import functions.IdentifyFunction;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class TabulatedFunctionFileWriter {
    public static void main(String[] args) {
        try {
            java.nio.file.Files.createDirectories(java.nio.file.Paths.get("output"));

            try (FileWriter fwArray = new FileWriter("output/array function.txt");
                 FileWriter fwList  = new FileWriter("output/linked list function.txt");
                 BufferedWriter bwArray = new BufferedWriter(fwArray);
                 BufferedWriter bwList  = new BufferedWriter(fwList)) {

                double[] x = {0, 1, 2, 3, 4};
                double[] y = {0, 1, 5, 2, 62.8};
                TabulatedFunction arrayFunc = new ArrayTabulatedFunction(x, y);

                TabulatedFunction listFunc = new LinkedListTabulatedFunction(new IdentifyFunction(), 0.0, 4.0, 5);

                FunctionsIO.writeTabulatedFunction(bwArray, arrayFunc);
                FunctionsIO.writeTabulatedFunction(bwList,  listFunc);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}