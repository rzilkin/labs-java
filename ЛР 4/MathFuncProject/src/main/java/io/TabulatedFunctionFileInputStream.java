package io;

import functions.TabulatedFunction;
import functions.factory.ArrayTabulatedFunctionFactory;
import functions.factory.LinkedListTabulatedFunctionFactory;
import operations.TabulatedDifferentialOperator;

import java.io.*;

public class TabulatedFunctionFileInputStream {
    public static void main(String[] args) {
        try (FileInputStream fileStream = new FileInputStream("input/binary function.bin");
             BufferedInputStream bufferedStream = new BufferedInputStream(fileStream)) {

            TabulatedFunction f = FunctionsIO.readTabulatedFunction(bufferedStream, new ArrayTabulatedFunctionFactory());

            System.out.println("Функция из файла:");
            System.out.println(f);

        } catch (IOException e) {
            System.err.println("Ошибка при чтении из файла:");
            e.printStackTrace();
        }

        System.out.println("Введите размер и значения функции:");
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(System.in);
            BufferedReader reader = new BufferedReader(inputStreamReader);

            TabulatedFunction funcFromConsole = FunctionsIO.readTabulatedFunction(reader, new LinkedListTabulatedFunctionFactory());

            TabulatedDifferentialOperator differentialOperator = new TabulatedDifferentialOperator();
            TabulatedFunction derivative = differentialOperator.derive(funcFromConsole);

            System.out.println("Производная функции:");
            System.out.println(derivative);

        } catch (IOException e) {
            System.err.println("Ошибка при чтении из консоли:");
            e.printStackTrace();
        }
    }
}
