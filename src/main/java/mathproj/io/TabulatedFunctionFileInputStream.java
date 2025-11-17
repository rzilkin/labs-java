package mathproj.io;

import mathproj.functions.TabulatedFunction;
import mathproj.functions.factory.ArrayTabulatedFunctionFactory;
import mathproj.functions.factory.LinkedListTabulatedFunctionFactory;
import mathproj.operations.TabulatedDifferentialOperator;

import java.io.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TabulatedFunctionFileInputStream {
    private static final Logger logger = LoggerFactory.getLogger(TabulatedFunctionFileInputStream.class);

    public static void main(String[] args) {
        logger.info("Читаем табулированную функцию из файла input/binary function.bin");
        try (FileInputStream fileStream = new FileInputStream("input/binary function.bin");
             BufferedInputStream bufferedStream = new BufferedInputStream(fileStream)) {

            TabulatedFunction f = FunctionsIO.readTabulatedFunction(bufferedStream, new ArrayTabulatedFunctionFactory());

            logger.info("Функция прочитана из файла:\n{}", f);
            System.out.println("Функция из файла:");
            System.out.println(f);

        } catch (IOException e) {
            logger.error("Не удалось прочитать функцию из бинарного файла", e);
            System.err.println("Ошибка при чтении из файла:");
            e.printStackTrace();
        }

        logger.info("Запрашиваем ввод табулированной функции у пользователя");
        System.out.println("Введите размер и значения функции:");
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(System.in);
            BufferedReader reader = new BufferedReader(inputStreamReader);

            TabulatedFunction funcFromConsole = FunctionsIO.readTabulatedFunction(reader, new LinkedListTabulatedFunctionFactory());
            logger.debug("Функция, введённая через консоль, содержит {} точек", funcFromConsole.getCount());

            TabulatedDifferentialOperator differentialOperator = new TabulatedDifferentialOperator();
            TabulatedFunction derivative = differentialOperator.derive(funcFromConsole);
            logger.info("Вычисленная производная:\n{}", derivative);

            System.out.println("Производная функции:");
            System.out.println(derivative);

        } catch (IOException e) {
            logger.error("Не удалось прочитать функцию из консоли", e);
            System.err.println("Ошибка при чтении из консоли:");
            e.printStackTrace();
        }
    }
}
