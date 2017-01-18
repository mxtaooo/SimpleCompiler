package tests;

import ast.Ast;
import codegen.ByteCodeGenerator;
import codegen.TranslatorVisitor;
import parser.Parser;
import semantic.SemanticVisitor;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Created by Mengxu on 2017/1/18.
 */
public class CodeGeneratorTest
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

        Parser parser = new Parser(fstream);
        Ast.Program.T prog = parser.parse();

        SemanticVisitor checker = new SemanticVisitor();
        checker.visit(prog);

        TranslatorVisitor translator = new TranslatorVisitor();
        translator.visit(prog);

        ByteCodeGenerator generator = new ByteCodeGenerator();
        generator.visit(translator.prog);
    }
}
