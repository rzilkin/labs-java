package mathproj.io;

import mathproj.functions.ArrayTabulatedFunction;
import mathproj.functions.LinkedListTabulatedFunction;
import mathproj.functions.TabulatedFunction;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TabulatedFunctionFileOutputStream {
    private static final Logger logger = LoggerFactory.getLogger(TabulatedFunctionFileOutputStream.class);

    public static void main(String[] args) {
        logger.info("Записываем табулированные функции в бинарные файлы");
        try {
            Files.createDirectories(Paths.get("output"));
            try (FileOutputStream arrayStream = new FileOutputStream("output/array function.bin");
                 FileOutputStream linkedListStream = new FileOutputStream("output/linked list function.bin");
                 BufferedOutputStream bufferedArrayStream = new BufferedOutputStream(arrayStream);
                 BufferedOutputStream bufferedLinkedListStream = new BufferedOutputStream(linkedListStream)) {
                double[] xValues = {0.0, 1.0, 2.0, 3.0, 4.0};
                double[] yValues = {0.0, 1.0, 4.0, 9.0, 16.0};

                TabulatedFunction funcWithArray = new ArrayTabulatedFunction(xValues, yValues);
                logger.debug("Функция на массивах подготовлена с количеством точек {}", funcWithArray.getCount());
                TabulatedFunction funcWithLinkedList = new LinkedListTabulatedFunction(xValues, yValues);
                logger.debug("Функция на связном списке подготовлена с количеством точек {}", funcWithLinkedList.getCount());

                FunctionsIO.writeTabulatedFunction(bufferedArrayStream, funcWithArray);
                FunctionsIO.writeTabulatedFunction(bufferedLinkedListStream, funcWithLinkedList);

                logger.info("Табулированные функции записаны в каталог output");
            }
        } catch(IOException e) {
            logger.error("Не удалось записать табулированные функции в бинарные файлы", e);
        }
    }
}
