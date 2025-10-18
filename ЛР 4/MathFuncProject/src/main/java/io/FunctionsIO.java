package io;

import functions.TabulatedFunction;
import functions.Point;
import functions.factory.TabulatedFunctionFactory;

import java.io.*;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.io.BufferedOutputStream;
import java.io.ObjectOutputStream;

public final class FunctionsIO {
    private FunctionsIO() {
        throw new UnsupportedOperationException("Экземпляры у данного класса не допускаются.");
    }

    public static void writeTabulatedFunction(BufferedWriter writer, TabulatedFunction function) {
        PrintWriter out = new PrintWriter(writer, true);
        out.println(function.getCount());
        for (Point p : function){
            out.printf("%f %f\n", p.x, p.y);
        }
        try{
            writer.flush();
        } catch(IOException e){}
    }

    public static void writeTabulatedFunction(BufferedOutputStream outputStream, TabulatedFunction function) throws IOException {
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
        String line = reader.readLine();
        if (line == null) {
            throw new IOException("Неожиданный конец потока: отсутствует строка с количеством точек");
        }
        int count = Integer.parseInt(line.trim());

        double[] xValues = new double[count];
        double[] yValues = new double[count];

        NumberFormat nf = NumberFormat.getInstance(Locale.forLanguageTag("ru"));

        for (int i = 0; i < count; i++) {
            line = reader.readLine();
            if (line == null) {
                throw new IOException("Неожиданный конец потока на строке данных с индексом  " + i);
            }
            String[] parts = line.trim().split(" ");
            if (parts.length != 2) {
                throw new IOException("Некорректная строка данных (ожидалось два значения): " + line);
            }
            try {
                Number xn = nf.parse(parts[0]);
                Number yn = nf.parse(parts[1]);
                xValues[i] = xn.doubleValue();
                yValues[i] = yn.doubleValue();
            } catch (ParseException e) {
                throw new IOException("Не удалось разобрать числа в строке " + (i + 2) + ": " + line, e);
            }
        }
        return factory.create(xValues, yValues);
    }

    public static TabulatedFunction readTabulatedFunction(BufferedInputStream inputStream, TabulatedFunctionFactory factory) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(inputStream);

        int count;
        try {
            count = dataInputStream.readInt();
        } catch (EOFException e) {
            throw new IOException("Неожиданный конец потока: отсутствует количество точек", e);
        }

        if(count <= 0) {
            throw new IOException("Некорректное количество точек: " + count);
        }

        double[] xValues = new double[count];
        double[] yValues = new double[count];

        for(int i = 0; i < count; ++i) {
            try {
                xValues[i] = dataInputStream.readDouble();
                yValues[i] = dataInputStream.readDouble();
            } catch(EOFException e) {
                throw new IOException("Неожиданный конец потока при чтении точки с индексом " + i, e);
            } catch(IOException e) {
                throw new IOException("Ошибка ввода-вывода при чтении точки с индексом " + i, e);
            }
        }
        return factory.create(xValues, yValues);
    }

    public static void serialize(BufferedOutputStream stream, TabulatedFunction function) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(stream);
        oos.writeObject(function);
        oos.flush();
        stream.flush();
    }

    public static TabulatedFunction deserialize(BufferedInputStream stream) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(stream);

        return (TabulatedFunction) ois.readObject();
    }

}
