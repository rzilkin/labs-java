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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TabulatedFunctionFileReader {
    private static final Logger logger = LoggerFactory.getLogger(TabulatedFunctionFileReader.class);

    public static void main(String[] args) {
        logger.info("Начинаем чтение табулированных функций из файла input/function.txt");
        try {
            Files.createDirectories(Paths.get("input"));
            try (FileReader fr1 = new FileReader("input/function.txt");
                 FileReader fr2 = new FileReader("input/function.txt");
                 BufferedReader br1 = new BufferedReader(fr1);
                 BufferedReader br2 = new BufferedReader(fr2)) {

                TabulatedFunctionFactory arrayFactory = new ArrayTabulatedFunctionFactory();
                TabulatedFunctionFactory listFactory  = new LinkedListTabulatedFunctionFactory();

                logger.debug("Читаем функцию с помощью {}", arrayFactory.getClass().getSimpleName());
                TabulatedFunction fArray  = FunctionsIO.readTabulatedFunction(br1, arrayFactory);
                logger.debug("Читаем функцию с помощью {}", listFactory.getClass().getSimpleName());
                TabulatedFunction fLinked = FunctionsIO.readTabulatedFunction(br2, listFactory);

                logger.info("Функция на массивах:\n{}", fArray);
                logger.info("Функция на списках:\n{}", fLinked);
            }
        } catch (IOException e) {
            logger.error("Не удалось прочитать табулированные функции", e);
        }
        logger.info("Чтение табулированных функций завершено");
    }
}
