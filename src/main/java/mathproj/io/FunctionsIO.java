package mathproj.io;

import mathproj.functions.TabulatedFunction;
import mathproj.functions.Point;
import mathproj.functions.factory.TabulatedFunctionFactory;

import java.io.*;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.io.BufferedOutputStream;
import java.io.ObjectOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FunctionsIO {
    private static final Logger logger = LoggerFactory.getLogger(FunctionsIO.class);

    private FunctionsIO() {
        throw new UnsupportedOperationException("Экземпляры у данного класса не допускаются.");
    }

    public static void writeTabulatedFunction(BufferedWriter writer, TabulatedFunction function) {
        logger.debug("Записываем табулированную функцию с {} точками в символьный поток", function.getCount());
        PrintWriter out = new PrintWriter(writer, true);
        out.println(function.getCount());
        for (Point p : function){
            out.printf("%f %f\n", p.x, p.y);
        }
        try{
            writer.flush();
        } catch(IOException e){
            logger.warn("Не удалось сбросить буфер символьного потока", e);
        }
    }

    public static void writeTabulatedFunction(BufferedOutputStream outputStream, TabulatedFunction function) throws IOException {
        logger.debug("Записываем табулированную функцию с {} точками в бинарный поток", function.getCount());
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

        int count = function.getCount();
        dataOutputStream.writeInt(count);

        for (Point p : function) {
            dataOutputStream.writeDouble(p.x);
            dataOutputStream.writeDouble(p.y);
        }

        dataOutputStream.flush();
    }

    public static TabulatedFunction readTabulatedFunction(BufferedReader reader,
                                                          TabulatedFunctionFactory factory) throws IOException {
        logger.debug("Читаем табулированную функцию из символьного потока с помощью фабрики {}", factory.getClass().getSimpleName());
        String line = reader.readLine();
        if (line == null) {
            logger.error("Неожиданный конец потока: отсутствует строка с количеством точек");
            throw new IOException("Неожиданный конец потока: отсутствует строка с количеством точек");
        }
        int count = Integer.parseInt(line.trim());
        logger.trace("Получено количество точек = {}", count);

        double[] xValues = new double[count];
        double[] yValues = new double[count];

        NumberFormat nf = NumberFormat.getInstance(Locale.forLanguageTag("ru"));

        for (int i = 0; i < count; i++) {
            line = reader.readLine();
            if (line == null) {
                logger.error("Неожиданный конец потока на индексе {}", i);
                throw new IOException("Неожиданный конец потока на строке данных с индексом  " + i);
            }
            String[] parts = line.trim().split(" ");
            if (parts.length != 2) {
                logger.error("Некорректная строка данных: {}", line);
                throw new IOException("Некорректная строка данных (ожидалось два значения): " + line);
            }
            try {
                Number xn = nf.parse(parts[0]);
                Number yn = nf.parse(parts[1]);
                xValues[i] = xn.doubleValue();
                yValues[i] = yn.doubleValue();
                logger.trace("Считали точку {} -> ({}, {})", i, xValues[i], yValues[i]);
            } catch (ParseException e) {
                logger.error("Не удалось разобрать строку данных {}", i + 2, e);
                throw new IOException("Не удалось разобрать числа в строке " + (i + 2) + ": " + line, e);
            }
        }
        TabulatedFunction function = factory.create(xValues, yValues);
        logger.debug("Создали табулированную функцию с {} точками из символьного потока", function.getCount());
        return function;
    }

    public static TabulatedFunction readTabulatedFunction(BufferedInputStream inputStream, TabulatedFunctionFactory factory) throws IOException {
        logger.debug("Читаем табулированную функцию из бинарного потока с помощью фабрики {}", factory.getClass().getSimpleName());
        DataInputStream dataInputStream = new DataInputStream(inputStream);

        int count;
        try {
            count = dataInputStream.readInt();
        } catch (EOFException e) {
            logger.error("Неожиданный конец потока: отсутствует количество точек", e);
            throw new IOException("Неожиданный конец потока: отсутствует количество точек", e);
        }

        if(count <= 0) {
            logger.error("Некорректное значение количества точек: {}", count);
            throw new IOException("Некорректное количество точек: " + count);
        }

        double[] xValues = new double[count];
        double[] yValues = new double[count];

        for(int i = 0; i < count; ++i) {
            try {
                xValues[i] = dataInputStream.readDouble();
                yValues[i] = dataInputStream.readDouble();
                logger.trace("Считали бинарную точку {} -> ({}, {})", i, xValues[i], yValues[i]);
            } catch(EOFException e) {
                logger.error("Неожиданный конец потока при чтении точки {}", i, e);
                throw new IOException("Неожиданный конец потока при чтении точки с индексом " + i, e);
            } catch(IOException e) {
                logger.error("Ошибка ввода-вывода при чтении точки {}", i, e);
                throw new IOException("Ошибка ввода-вывода при чтении точки с индексом " + i, e);
            }
        }
        TabulatedFunction function = factory.create(xValues, yValues);
        logger.debug("Создали табулированную функцию с {} точками из бинарного потока", function.getCount());
        return function;
    }

    public static void serialize(BufferedOutputStream stream, TabulatedFunction function) throws IOException {
        logger.debug("Сериализуем табулированную функцию с {} точками", function.getCount());
        ObjectOutputStream oos = new ObjectOutputStream(stream);
        oos.writeObject(function);
        oos.flush();
        stream.flush();
    }

    public static TabulatedFunction deserialize(BufferedInputStream stream) throws IOException, ClassNotFoundException {
        logger.debug("Десериализуем табулированную функцию из бинарного потока");
        ObjectInputStream ois = new ObjectInputStream(stream);
        TabulatedFunction function = (TabulatedFunction) ois.readObject();
        logger.debug("Десериализованная табулированная функция содержит {} точек", function.getCount());
        return function;
    }

}
