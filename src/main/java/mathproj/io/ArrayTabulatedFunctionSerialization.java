package mathproj.io;

import mathproj.functions.ArrayTabulatedFunction;
import mathproj.functions.TabulatedFunction;
import mathproj.operations.TabulatedDifferentialOperator;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArrayTabulatedFunctionSerialization {
    private static final Logger logger = LoggerFactory.getLogger(ArrayTabulatedFunctionSerialization.class);

    public static void main(String[] args) {
        logger.info("Сериализуем табулированные функции на массивах в файл output/serialized array functions.bin");
        try {
            Files.createDirectories(Paths.get("output"));

            try (FileOutputStream fos = new FileOutputStream("output/serialized array functions.bin");
                 BufferedOutputStream bos = new BufferedOutputStream(fos)) {

                double[] x = {0.0, 1.0, 2.0, 3.0, 4.0};
                double[] y = {0.0, 1.0, 4.0, 9.0, 16.0};
                TabulatedFunction f = new ArrayTabulatedFunction(x, y);
                logger.debug("Исходная функция создана с количеством точек {}", f.getCount());

                TabulatedDifferentialOperator op = new TabulatedDifferentialOperator();
                TabulatedFunction f1 = op.derive(f);
                logger.debug("Первая производная посчитана с количеством точек {}", f1.getCount());
                TabulatedFunction f2 = op.derive(f1);
                logger.debug("Вторая производная посчитана с количеством точек {}", f2.getCount());

                FunctionsIO.serialize(bos, f);
                FunctionsIO.serialize(bos, f1);
                FunctionsIO.serialize(bos, f2);
                logger.info("Функции успешно сериализованы");
                System.out.println("Функции сериализованы");
            }
        } catch (IOException e) {
            logger.error("Не удалось сериализовать функции", e);
            e.printStackTrace();
        }

        logger.info("Десериализуем табулированные функции на массивах с диска");
        try (FileInputStream fis = new FileInputStream("output/serialized array functions.bin");
             BufferedInputStream bis = new BufferedInputStream(fis)) {

            TabulatedFunction rf  = FunctionsIO.deserialize(bis);
            TabulatedFunction rf1 = FunctionsIO.deserialize(bis);
            TabulatedFunction rf2 = FunctionsIO.deserialize(bis);

            logger.info("Исходная функция после десериализации:\n{}", rf);
            logger.info("Первая производная после десериализации:\n{}", rf1);
            logger.info("Вторая производная после десериализации:\n{}", rf2);

            System.out.println("\nИсходная функция:");
            System.out.println(rf.toString());

            System.out.println("\nПервая производная:");
            System.out.println(rf1.toString());

            System.out.println("\nВторая производная:");
            System.out.println(rf2.toString());
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Не удалось десериализовать функции", e);
            e.printStackTrace();
        }
    }
}
