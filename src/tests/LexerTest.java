package tests;

import lexer.Lexer;
import lexer.Token;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Created by Mengxu on 2017/1/6.
 */
public class LexerTest
{
    public static void main(String[] args)
    {
        final String fname;
        if (args.length > 0)
            fname = args[0];
        else fname = "Example.soo";

        InputStream fstream = null;
        try
        {
            fstream = new BufferedInputStream(new FileInputStream(fname));
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
            System.exit(1);
        }

        Lexer lexer = new Lexer(fstream);
        Token current;

        do
        {
            current = lexer.nextToken();
            System.out.println(current.toString());
        } while (current.kind != Token.Kind.EOF);

    }
}
