package io;

import functions.LinkedListTabulatedFunction;
import functions.TabulatedFunction;
import functions.factory.LinkedListTabulatedFunctionFactory;
import operations.DifferentialOperator;
import operations.TabulatedDifferentialOperator;

import java.io.*;

public class LinkedListTabulatedFunctionSerialization  {
    public static void main(String[] args) {
        try (FileOutputStream fos = new FileOutputStream("output/serialized linked list functions.bin");
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {

            double[] xValues = {1, 2, 3, 4, 5};
            double[] yValues = {1, 3, 5, 7, 9};

            TabulatedFunction func = new LinkedListTabulatedFunction(xValues, yValues);
            TabulatedDifferentialOperator diffOperator = new TabulatedDifferentialOperator(new LinkedListTabulatedFunctionFactory());
            TabulatedFunction firstDeriv = diffOperator.derive(func);
            TabulatedFunction secondDeriv = diffOperator.derive(firstDeriv);

            FunctionsIO.serialize(bos, func);
            FunctionsIO.serialize(bos, firstDeriv);
            FunctionsIO.serialize(bos, secondDeriv);

            System.out.println("Функции сериализованы");
        } catch(IOException e) {
            e.printStackTrace();
        }

        try (FileInputStream fis = new FileInputStream("output/serialized linked list functions.bin");
             BufferedInputStream bis = new BufferedInputStream(fis)) {

            TabulatedFunction deserializedFunc = FunctionsIO.deserialize(bis);
            TabulatedFunction deserializedFirstDeriv = FunctionsIO.deserialize(bis);
            TabulatedFunction deserializedSecondDeriv = FunctionsIO.deserialize(bis);

            System.out.println("\nИсходная функция:");
            System.out.println(deserializedFunc.toString());

            System.out.println("\nПервая производная:");
            System.out.println(deserializedFirstDeriv.toString());

            System.out.println("\nВторая производная:");
            System.out.println(deserializedSecondDeriv.toString());

        } catch(IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
