package io;

import functions.ArrayTabulatedFunction;
import functions.LinkedListTabulatedFunction;
import functions.TabulatedFunction;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TabulatedFunctionFileOutputStream {
    public static void main(String[] args) {
        try {
            Files.createDirectories(Paths.get("output"));
            try (FileOutputStream arrayStream = new FileOutputStream("output/array function.bin");
                 FileOutputStream linkedListStream = new FileOutputStream("output/linked list function.bin");
                 BufferedOutputStream bufferedArrayStream = new BufferedOutputStream(arrayStream);
                 BufferedOutputStream bufferedLinkedListStream = new BufferedOutputStream(linkedListStream)) {
                double[] xValues = {0.0, 1.0, 2.0, 3.0, 4.0};
                double[] yValues = {0.0, 1.0, 4.0, 9.0, 16.0};

                TabulatedFunction funcWithArray = new ArrayTabulatedFunction(xValues, yValues);
                TabulatedFunction funcWithLinkedList = new LinkedListTabulatedFunction(xValues, yValues);

                FunctionsIO.writeTabulatedFunction(bufferedArrayStream, funcWithArray);
                FunctionsIO.writeTabulatedFunction(bufferedLinkedListStream, funcWithLinkedList);

                System.out.println("Табулированные функции записаны в файлы");
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}