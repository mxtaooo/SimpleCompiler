package ast;

import ast.Ast.*;

/**
 * Created by Mengxu on 2017/1/7.
 */
public interface Visitor
{
    // Type
    void visit(Type.Boolean t);

    void visit(Type.ClassType t);

    void visit(Type.Int t);

    // Dec
    void visit(Dec.DecSingle d);

    // Exp
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
