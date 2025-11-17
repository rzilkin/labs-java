package mathproj.io;

import mathproj.functions.LinkedListTabulatedFunction;
import mathproj.functions.TabulatedFunction;
import mathproj.functions.factory.LinkedListTabulatedFunctionFactory;
import mathproj.operations.TabulatedDifferentialOperator;

import java.io.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinkedListTabulatedFunctionSerialization  {
    private static final Logger logger = LoggerFactory.getLogger(LinkedListTabulatedFunctionSerialization.class);

    public static void main(String[] args) {
        logger.info("Сериализуем табулированные функции на связном списке в файл output/serialized linked list functions.bin");
        try (FileOutputStream fos = new FileOutputStream("output/serialized linked list functions.bin");
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {

            double[] xValues = {1, 2, 3, 4, 5};
            double[] yValues = {1, 3, 5, 7, 9};

            TabulatedFunction func = new LinkedListTabulatedFunction(xValues, yValues);
            logger.debug("Исходная функция на списках содержит {} точек", func.getCount());
            TabulatedDifferentialOperator diffOperator = new TabulatedDifferentialOperator(new LinkedListTabulatedFunctionFactory());
            TabulatedFunction firstDeriv = diffOperator.derive(func);
            logger.debug("Первая производная посчитана с количеством точек {}", firstDeriv.getCount());
            TabulatedFunction secondDeriv = diffOperator.derive(firstDeriv);
            logger.debug("Вторая производная посчитана с количеством точек {}", secondDeriv.getCount());

            FunctionsIO.serialize(bos, func);
            FunctionsIO.serialize(bos, firstDeriv);
            FunctionsIO.serialize(bos, secondDeriv);

            logger.info("Функции на связном списке успешно сериализованы");
            System.out.println("Функции сериализованы");
        } catch(IOException e) {
            logger.error("Не удалось сериализовать функции на связном списке", e);
            e.printStackTrace();
        }

        logger.info("Десериализуем функции на связном списке с диска");
        try (FileInputStream fis = new FileInputStream("output/serialized linked list functions.bin");
             BufferedInputStream bis = new BufferedInputStream(fis)) {

            TabulatedFunction deserializedFunc = FunctionsIO.deserialize(bis);
            TabulatedFunction deserializedFirstDeriv = FunctionsIO.deserialize(bis);
            TabulatedFunction deserializedSecondDeriv = FunctionsIO.deserialize(bis);

            logger.info("Исходная функция после десериализации:\n{}", deserializedFunc);
            logger.info("Первая производная после десериализации:\n{}", deserializedFirstDeriv);
            logger.info("Вторая производная после десериализации:\n{}", deserializedSecondDeriv);

            System.out.println("\nИсходная функция:");
            System.out.println(deserializedFunc.toString());

            System.out.println("\nПервая производная:");
            System.out.println(deserializedFirstDeriv.toString());

            System.out.println("\nВторая производная:");
            System.out.println(deserializedSecondDeriv.toString());

        } catch(IOException | ClassNotFoundException e) {
            logger.error("Не удалось десериализовать функции на связном списке", e);
            e.printStackTrace();
        }
    }
}
