package tests;

import ast.Ast;

/**
 * Created by Mengxu on 2017/1/12.
 */
public class AstPrintVisitor implements ast.Visitor
{
    @Override
    public void visit(Ast.Type.T t)
    {
        if (t instanceof Ast.Type.Boolean)
            this.visit(((Ast.Type.Boolean) t));
        else if (t instanceof Ast.Type.ClassType)
            this.visit(((Ast.Type.ClassType) t));
        else if (t instanceof Ast.Type.Int)
            this.visit(((Ast.Type.Int) t));
    }

    @Override
    public void visit(Ast.Type.Boolean t)
    {
        System.out.print("boolean");
    }

    @Override
    public void visit(Ast.Type.ClassType t)
    {
        System.out.print(t.id);
    }

    @Override
    public void visit(Ast.Type.Int t)
    {
        System.out.print("int");
    }

    @Override
    public void visit(Ast.Dec.DecSingle d)
    {
        this.visit(d.type);
        System.out.print(" " + d.id);
        System.out.println(";");
    }

    @Override
    public void visit(Ast.Exp.T e)
    {
        if (e instanceof Ast.Exp.Add)
            this.visit(((Ast.Exp.Add) e));
        else if (e instanceof Ast.Exp.And)
            this.visit(((Ast.Exp.And) e));
        else if (e instanceof Ast.Exp.Call)
            this.visit(((Ast.Exp.Call) e));
        else if (e instanceof Ast.Exp.False)
            this.visit(((Ast.Exp.False) e));
        else if (e instanceof Ast.Exp.Id)
            this.visit(((Ast.Exp.Id) e));
        else if (e instanceof Ast.Exp.LT)
            this.visit(((Ast.Exp.LT) e));
        else if (e instanceof Ast.Exp.NewObject)
            this.visit(((Ast.Exp.NewObject) e));
        else if (e instanceof Ast.Exp.Not)
            this.visit(((Ast.Exp.Not) e));
        else if (e instanceof Ast.Exp.Num)
            this.visit(((Ast.Exp.Num) e));
        else if (e instanceof Ast.Exp.Sub)
            this.visit(((Ast.Exp.Sub) e));
        else if (e instanceof Ast.Exp.This)
            this.visit(((Ast.Exp.This) e));
        else if (e instanceof Ast.Exp.Times)
            this.visit(((Ast.Exp.Times) e));
        else // if (e instanceof Ast.Exp.True)
            this.visit(((Ast.Exp.True) e));
    }

    @Override
    public void visit(Ast.Exp.Add e)
    {
        this.visit(e.left);
        System.out.print(" + ");
        this.visit(e.right);
    }

    @Override
    public void visit(Ast.Exp.And e)
    {
        this.visit(e.left);
        System.out.print(" && ");
        this.visit(e.right);
    }

    @Override
    public void visit(Ast.Exp.Call e)
    {
        this.visit(e.exp);
        System.out.print("." + e.id + "(");
        for (int i = 0; i < e.args.size(); i++)
        {
            if (i != 0)
                System.out.print(",");
            this.visit(e.args.get(i));
        }
        System.out.print(")");
    }

    @Override
    public void visit(Ast.Exp.False e)
    {
        System.out.print("false");
    }

    @Override
    public void visit(Ast.Exp.Id e)
    {
        System.out.println(e.id);
    }

    @Override
    public void visit(Ast.Exp.LT e)
    {
        this.visit(e.left);
        System.out.print(" < ");
        this.visit(e.right);
    }

    @Override
    public void visit(Ast.Exp.NewObject e)
    {
        System.out.print("new " + e.id + "()");
    }

    @Override
    public void visit(Ast.Exp.Not e)
    {
        System.out.print("!(");
        this.visit(e.exp);
        System.out.print(")");
    }

    @Override
    public void visit(Ast.Exp.Num e)
    {
        System.out.print(e.num);
    }

    @Override
    public void visit(Ast.Exp.Sub e)
    {
        this.visit(e.left);
        System.out.print(" - ");
        this.visit(e.right);
    }

    @Override
    public void visit(Ast.Exp.This e)
    {
        System.out.print("this.");
    }

    @Override
    public void visit(Ast.Exp.Times e)
    {
        this.visit(e.left);
        System.out.print(" * ");
        this.visit(e.right);
    }

    @Override
    public void visit(Ast.Exp.True e)
    {
        System.out.print("true");
    }

    @Override
    public void visit(Ast.Stm.T s)
    {
        if (s instanceof Ast.Stm.Assign)
            this.visit(((Ast.Stm.Assign) s));
        else if (s instanceof Ast.Stm.Block)
            this.visit(((Ast.Stm.Block) s));
        else if (s instanceof Ast.Stm.If)
            this.visit(((Ast.Stm.If) s));
        else if (s instanceof Ast.Stm.Print)
            this.visit(((Ast.Stm.Print) s));
        else // if (s instanceof Ast.Stm.While)
            this.visit(((Ast.Stm.While) s));
    }

    @Override
    public void visit(Ast.Stm.Assign s)
    {
        System.out.print(s.id + " = ");
        this.visit(s.exp);
        System.out.println(";");
    }

    @Override
    public void visit(Ast.Stm.Block s)
    {
        System.out.println("{");
        for (Ast.Stm.T stm : s.stms)
        {
            this.visit(stm);
        }
        System.out.println("}");
    }

    @Override
    public void visit(Ast.Stm.If s)
    {
        System.out.print("if (");
        this.visit(s.condition);
        System.out.print(")");
        this.visit(s.then_stm);
        System.out.println("else");
        this.visit(s.else_stm);
    }

    @Override
    public void visit(Ast.Stm.Print s)
    {
        System.out.print("print(");
        this.visit(s.exp);
        System.out.println(");");
    }

    @Override
    public void visit(Ast.Stm.While s)
    {
        System.out.print("while (");
        this.visit(s.condition);
        System.out.println(")");
        this.visit(s.body);
    }

    @Override
    public void visit(Ast.Method.MethodSingle m)
    {
        this.visit(m.retType);
        System.out.print(" " + m.id + "(");
        for (int i = 0; i < m.formals.size(); i++)
        {
            if (i != 0)
                System.out.print(",");
            this.visit(((Ast.Dec.DecSingle) m.formals.get(i)));
        }
        System.out.println(")");
        System.out.println("{");
        for (Ast.Dec.T dec : m.decs)
        {
            this.visit(((Ast.Dec.DecSingle) dec));
            System.out.println(";");
        }
        for (Ast.Stm.T stm : m.stms)
        {
            this.visit(stm);
        }
        System.out.print("return ");
        this.visit(m.retExp);
        System.out.println(";");
        System.out.println("}");
    }

    @Override
    public void visit(Ast.Class.ClassSingle c)
    {
        System.out.println("class " + c.id);
        System.out.println("{");
        for (Ast.Dec.T dec : c.fields)
        {
            this.visit(((Ast.Dec.DecSingle) dec));
            System.out.println(";");
        }
        for (Ast.Method.T method : c.methods)
        {
            this.visit(((Ast.Method.MethodSingle) method));
        }
        System.out.println("}");
    }

    @Override
    public void visit(Ast.MainClass.MainClassSingle c)
    {
        System.out.println("class " + c.id);
        System.out.println("{");
        System.out.println("void main()");
        System.out.println("{");
        this.visit(c.stm);
        System.out.println("}");
        System.out.println("}");
    }

    @Override
    public void visit(Ast.Program.ProgramSingle p)
    {
        this.visit(((Ast.MainClass.MainClassSingle) p.mainClass));
        for (Ast.Class.T c : p.classes)
        {
            this.visit(((Ast.Class.ClassSingle) c));
        }
    }
}
