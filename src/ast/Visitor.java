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
}
