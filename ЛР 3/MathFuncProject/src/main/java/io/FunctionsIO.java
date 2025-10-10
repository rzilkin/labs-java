package io;

import functions.TabulatedFunction;
import functions.Point;
import functions.factory.TabulatedFunctionFactory;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

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
}
