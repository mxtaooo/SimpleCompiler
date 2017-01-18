package codegen;

import codegen.ast.Ast;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by Mengxu on 2017/1/18.
 */
public class ByteCodeGenerator implements codegen.ast.Visitor
{
    private java.io.BufferedWriter writer;

    private void writeln(String s)
    {
        write(s + "\n");
    }

    private void write(String s)
    {
        try
        {
            this.writer.write(s);
        } catch (IOException e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void iwriteln(String s)
    {
        write("    " + s + "\n");
    }

    @Override
    public void visit(Ast.Type.ClassType t)
    {
        this.write("L" + t.id + ";");
    }

    @Override
    public void visit(Ast.Type.Int t)
    {
        this.write("I");
    }

    @Override
    public void visit(Ast.Dec.DecSingle d)
    {

    }

    @Override
    public void visit(Ast.Stm.Aload s)
    {
        this.iwriteln("aload " + s.index);
    }

    @Override
    public void visit(Ast.Stm.Areturn s)
    {
        this.iwriteln("areturn");
    }

    @Override
    public void visit(Ast.Stm.Astore s)
    {
        this.iwriteln("astore " + s.index);
    }

    @Override
    public void visit(Ast.Stm.Goto s)
    {
        this.iwriteln("goto " + s.l.toString());
    }

    @Override
    public void visit(Ast.Stm.Getfield s)
    {
        this.iwriteln("getfield " + s.fieldSpec + " " + s.descriptor);
    }

    @Override
    public void visit(Ast.Stm.Iadd s)
    {
        this.iwriteln("iadd");
    }

    @Override
    public void visit(Ast.Stm.Ificmplt s)
    {
        this.iwriteln("if_icmplt " + s.l.toString());
    }

    @Override
    public void visit(Ast.Stm.Iload s)
    {
        this.iwriteln("iload " + s.index);
    }

    @Override
    public void visit(Ast.Stm.Imul s)
    {
        this.iwriteln("imul");
    }

    @Override
    public void visit(Ast.Stm.Invokevirtual s)
    {
        this.write("    invokevirtual " + s.c + "/" + s.f + "(");
        s.at.forEach(this::visit);
        this.write(")");
        this.visit(s.rt);
        this.writeln("");
    }

    @Override
    public void visit(Ast.Stm.Ireturn s)
    {
        this.iwriteln("ireturn");
    }

    @Override
    public void visit(Ast.Stm.Istore s)
    {
        this.iwriteln("istore " + s.index);
    }

    @Override
    public void visit(Ast.Stm.Isub s)
    {
        this.iwriteln("isub");
    }

    @Override
    public void visit(Ast.Stm.LabelJ s)
    {
        this.writeln(s.label.toString() + ":");
    }

    @Override
    public void visit(Ast.Stm.Ldc s)
    {
        this.iwriteln("ldc " + s.i);
    }

    @Override
    public void visit(Ast.Stm.New s)
    {
        this.iwriteln("new " + s.c);
        this.iwriteln("dup");
        this.iwriteln("invokespecial " + s.c + "/<init>()V");
    }

    @Override
    public void visit(Ast.Stm.Print s)
    {
        this.iwriteln("getstatic java/lang/System/out Ljava/io/PrintStream;");
        this.iwriteln("swap");
        this.iwriteln("invokevirtual java/io/PrintStream/println(I)V");
    }

    @Override
    public void visit(Ast.Stm.Putfield s)
    {
        this.iwriteln("putfield " + s.fieldSpec + " " + s.descriptor);
    }

    @Override
    public void visit(Ast.Method.MethodSingle m)
    {
        this.write(".method public " + m.id + "(");
        m.formals.forEach(this::visit);
        this.write(")");
        this.visit(m.retType);
        this.writeln("");
        this.writeln(".limit stack 4096");
        this.writeln(".limit locals " + (m.index + 1));

        m.stms.forEach(this::visit);
        this.writeln(".end method");
    }

    @Override
    public void visit(Ast.Class.ClassSingle c)
    {
        try
        {
            this.writer = new java.io.BufferedWriter(new java.io.OutputStreamWriter(
                    new java.io.FileOutputStream(c.id + ".j")));
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
            System.exit(1);
        }

        this.writeln("; This file is automatically generated by the compiler\n");
        this.writeln("; Do Not Modify!\n");

        this.writeln("class public " + c.id);
        if (c.base == null)
            this.writeln(".super java/lang/Object");
        else this.writeln(".super " + c.base);

        c.fields.forEach(f ->
        {
            this.write(".field public " + f.id + " ");
            this.visit(f.type);
            this.writeln("");
        });

        this.writeln(".method public <init>()V");
        this.iwriteln("aload 0");
        if (c.base == null)
            this.iwriteln("invokespecial java/lang/Object/<init>()V");
        else this.iwriteln("invokespecial " + c.base + "/<init>()V");
        this.iwriteln("return");
        this.writeln(".end method");
        c.methods.forEach(this::visit);

        try
        {
            this.writer.close();
        } catch (IOException e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void visit(Ast.MainClass.MainClassSingle c)
    {
        try
        {
            this.writer = new java.io.BufferedWriter(new java.io.OutputStreamWriter(
                    new java.io.FileOutputStream(c.id + ".j")));
        } catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }

        this.writeln("; This file is automatically generated by the compiler\n");
        this.writeln("; Do Not Modify!\n");

        this.writeln(".class public " + c.id);
        this.writeln(".super java/lang/Object");
        this.writeln(".method public static main([Ljava/lang/String;)V");
        this.writeln(".limit stack 4096");
        this.writeln(".limit locals 2");
        c.stms.forEach(this::visit);
        this.iwriteln("return");
        this.writeln(".end method");

        try
        {
            this.writer.close();
        } catch (IOException e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void visit(Ast.Program.ProgramSingle p)
    {
        this.visit(p.mainClass);
        p.classes.forEach(this::visit);
    }
}
