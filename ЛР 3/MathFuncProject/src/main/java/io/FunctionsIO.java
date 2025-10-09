package io;

import functions.TabulatedFunction;
import functions.Point;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;

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
}
