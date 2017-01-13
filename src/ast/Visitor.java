package ast;

import ast.Ast.*;

/**
 * Created by Mengxu on 2017/1/7.
 */
public interface Visitor
{
    // Type
    default void visit(Type.T t)
    {
        if (t instanceof Ast.Type.Boolean)
            this.visit(((Ast.Type.Boolean) t));
        else if (t instanceof Ast.Type.ClassType)
            this.visit(((Ast.Type.ClassType) t));
        else if (t instanceof Ast.Type.Int)
            this.visit(((Ast.Type.Int) t));
    }

    void visit(Type.Boolean t);

    void visit(Type.ClassType t);

    void visit(Type.Int t);

    // Dec
    void visit(Dec.DecSingle d);

    // Exp
    default void visit(Exp.T e)
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

    void visit(Exp.Add e);

    void visit(Exp.And e);

    void visit(Exp.Call e);

    void visit(Exp.False e);

    void visit(Exp.Id e);

    void visit(Exp.LT e);

    void visit(Exp.NewObject e);

    void visit(Exp.Not e);

    void visit(Exp.Num e);

    void visit(Exp.Sub e);

    void visit(Exp.This e);

    void visit(Exp.Times e);

    void visit(Exp.True e);

    // Stm
    default void visit(Stm.T s)
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

    void visit(Stm.Assign s);

    void visit(Stm.Block s);

    void visit(Stm.If s);

    void visit(Stm.Print s);

    void visit(Stm.While s);

    // Method
    void visit(Method.MethodSingle m);

    // Class
    void visit(Ast.Class.ClassSingle c);

    void visit(MainClass.MainClassSingle c);

    // Program
    void visit(Program.ProgramSingle p);
}
