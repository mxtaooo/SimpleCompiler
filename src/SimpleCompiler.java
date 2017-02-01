import ast.Ast;
import codegen.ByteCodeGenerator;
import codegen.TranslatorVisitor;
import optimize.Optimizer;
import parser.Parser;
import semantic.SemanticVisitor;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Created by Mengxu on 2017/1/4.
 */
public class SimpleCompiler
{
    public static void main(String[] args)
    {
        if (args.length == 0)
        {
            System.out.println("Hello, this is a simple compiler!");
            System.out.println("Please input the file name which you want to compile");
            System.exit(0);
        }

        final String fname = args[0];
        InputStream fstream = null;
        try
        {
            fstream = new BufferedInputStream(new FileInputStream(fname));
        } catch (FileNotFoundException e)
        {
            System.out.println("Cannot find the file: " + fname);
            System.exit(1);
        }

        Parser parser = new Parser(fstream);
        Ast.Program.T prog = parser.parse();

        SemanticVisitor checker = new SemanticVisitor();
        checker.visit(prog);

        // if the program is correct, we generate code for it
        if (!checker.isOK())
            return;

        Optimizer optimizer = new Optimizer();
        optimizer.optimize(prog);

        TranslatorVisitor translator = new TranslatorVisitor();
        translator.visit(prog);

        ByteCodeGenerator generator = new ByteCodeGenerator();
        generator.visit(translator.prog);

        // ascii instructions to binary file
        jasmin.Main.main(new String[]{translator.prog.mainClass.id + ".il"});
        for (codegen.ast.Ast.Class.ClassSingle cla : translator.prog.classes)
            jasmin.Main.main(new String[]{cla.id + ".il"});

    }
}
