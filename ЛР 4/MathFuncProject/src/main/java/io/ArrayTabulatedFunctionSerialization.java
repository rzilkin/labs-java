package io;

import functions.ArrayTabulatedFunction;
import functions.TabulatedFunction;
import operations.TabulatedDifferentialOperator;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ArrayTabulatedFunctionSerialization {
    public static void main(String[] args) {
        try {
            Files.createDirectories(Paths.get("output"));

            try (FileOutputStream fos = new FileOutputStream("output/serialized array functions.bin");
                 BufferedOutputStream bos = new BufferedOutputStream(fos)) {

                double[] x = {0.0, 1.0, 2.0, 3.0, 4.0};
                double[] y = {0.0, 1.0, 4.0, 9.0, 16.0};
                TabulatedFunction f = new ArrayTabulatedFunction(x, y);

                TabulatedDifferentialOperator op = new TabulatedDifferentialOperator();
                TabulatedFunction f1 = op.derive(f);
                TabulatedFunction f2 = op.derive(f1);

                FunctionsIO.serialize(bos, f);
                FunctionsIO.serialize(bos, f1);
                FunctionsIO.serialize(bos, f2);
                System.out.println("Функции сериализованы");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (FileInputStream fis = new FileInputStream("output/serialized array functions.bin");
             BufferedInputStream bis = new BufferedInputStream(fis)) {

            TabulatedFunction rf  = FunctionsIO.deserialize(bis);
            TabulatedFunction rf1 = FunctionsIO.deserialize(bis);
            TabulatedFunction rf2 = FunctionsIO.deserialize(bis);

            System.out.println("\nИсходная функция:");
            System.out.println(rf.toString());

            System.out.println("\nПервая производная:");
            System.out.println(rf1.toString());

            System.out.println("\nВторая производная:");
            System.out.println(rf2.toString());
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
