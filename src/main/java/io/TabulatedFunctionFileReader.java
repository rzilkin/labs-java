package io;

import functions.TabulatedFunction;
import functions.factory.ArrayTabulatedFunctionFactory;
import functions.factory.LinkedListTabulatedFunctionFactory;
import functions.factory.TabulatedFunctionFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TabulatedFunctionFileReader {
    public static void main(String[] args) {
        try {
            Files.createDirectories(Paths.get("input"));
            try (FileReader fr1 = new FileReader("input/function.txt");
                 FileReader fr2 = new FileReader("input/function.txt");
                 BufferedReader br1 = new BufferedReader(fr1);
                 BufferedReader br2 = new BufferedReader(fr2)) {

                TabulatedFunctionFactory arrayFactory = new ArrayTabulatedFunctionFactory();
                TabulatedFunctionFactory listFactory  = new LinkedListTabulatedFunctionFactory();

                TabulatedFunction fArray  = FunctionsIO.readTabulatedFunction(br1, arrayFactory);
                TabulatedFunction fLinked = FunctionsIO.readTabulatedFunction(br2, listFactory);

                System.out.println(fArray.toString());
                System.out.println(fLinked.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
