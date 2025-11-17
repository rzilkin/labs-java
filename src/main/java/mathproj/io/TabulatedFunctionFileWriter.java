package mathproj.io;

import mathproj.functions.ArrayTabulatedFunction;
import mathproj.functions.LinkedListTabulatedFunction;
import mathproj.functions.TabulatedFunction;
import mathproj.functions.IdentifyFunction;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TabulatedFunctionFileWriter {
    private static final Logger logger = LoggerFactory.getLogger(TabulatedFunctionFileWriter.class);

    public static void main(String[] args) {
        logger.info("Готовим запись табулированных функций на диск");
        try {
            java.nio.file.Files.createDirectories(java.nio.file.Paths.get("output"));

            try (FileWriter fwArray = new FileWriter("output/array function.txt");
                 FileWriter fwList  = new FileWriter("output/linked list function.txt");
                 BufferedWriter bwArray = new BufferedWriter(fwArray);
                 BufferedWriter bwList  = new BufferedWriter(fwList)) {

                double[] x = {0, 1, 2, 3, 4};
                double[] y = {0, 1, 5, 2, 62.8};
                TabulatedFunction arrayFunc = new ArrayTabulatedFunction(x, y);
                logger.debug("Функция на массивах подготовлена с количеством точек {}", arrayFunc.getCount());

                TabulatedFunction listFunc = new LinkedListTabulatedFunction(new IdentifyFunction(), 0.0, 4.0, 5);
                logger.debug("Функция на списках подготовлена с количеством точек {}", listFunc.getCount());

                logger.info("Записываем функцию на массивах в output/array function.txt");
                FunctionsIO.writeTabulatedFunction(bwArray, arrayFunc);
                logger.info("Записываем функцию на списках в output/linked list function.txt");
                FunctionsIO.writeTabulatedFunction(bwList,  listFunc);
            }
        } catch (IOException e) {
            logger.error("Не удалось записать табулированные функции", e);
        }
        logger.info("Запись табулированных функций завершена");
    }
}
