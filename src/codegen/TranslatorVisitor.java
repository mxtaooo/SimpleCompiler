package codegen;

import ast.Ast;
import codegen.ast.Ast.*;

import java.util.Hashtable;
import java.util.LinkedList;

/**
 * Created by Mengxu on 2017/1/17.
 */
public class TranslatorVisitor implements ast.Visitor
{
    private String classId;
    private int index;
    private Hashtable<String, Integer> indexTable;
    private Type.T type;
    private Dec.DecSingle dec;
    private LinkedList<Stm.T> stms;
    private Method.MethodSingle method;
    private codegen.ast.Ast.Class.ClassSingle classs;
    private MainClass.MainClassSingle mainClass;
    private Program.ProgramSingle prog;

    public TranslatorVisitor()
    {
        this.classId = null;
        this.indexTable = null;
        this.type = null;
        this.dec = null;
        this.stms = new LinkedList<>();
        this.method = null;
        this.classId = null;
        this.mainClass = null;
        this.classs = null;
        this.prog = null;
    }

    private void emit(Stm.T s)
    {
        this.stms.add(s);
    }

    public void visit(Ast.Type.Boolean t)
    {
        this.type = new Type.Int();
    }

    @Override
    public void visit(Ast.Type.ClassType t)
    {
        this.type = new Type.ClassType(t.id);
    }

    @Override
    public void visit(Ast.Type.Int t)
    {
        this.type = new Type.Int();
    }

    @Override
    public void visit(Ast.Dec.DecSingle d)
    {
        this.visit(d.type);
        this.dec = new Dec.DecSingle(this.type, d.id);
        //if (this.indexTable != null)
        // but how about the field
        this.indexTable.put(d.id, index++);
    }

    @Override
    public void visit(Ast.Exp.Add e)
    {
        this.visit(e.left);
        this.visit(e.right);
        emit(new Stm.Iadd());
    }

    @Override
    public void visit(Ast.Exp.And e)
    {

    }

    @Override
    public void visit(Ast.Exp.Call e)
    {

    }

    @Override
    public void visit(Ast.Exp.False e)
    {
        emit(new Stm.Ldc(0));
    }

    @Override
    public void visit(Ast.Exp.Id e)
    {

    }

    @Override
    public void visit(Ast.Exp.LT e)
    {

    }

    @Override
    public void visit(Ast.Exp.NewObject e)
    {

    }

    @Override
    public void visit(Ast.Exp.Not e)
    {

    }

    @Override
    public void visit(Ast.Exp.Num e)
    {

    }

    @Override
    public void visit(Ast.Exp.Sub e)
    {

    }

    @Override
    public void visit(Ast.Exp.This e)
    {

    }

    @Override
    public void visit(Ast.Exp.Times e)
    {
        this.visit(e.left);
        this.visit(e.right);
        emit(new Stm.Imul());
    }

    @Override
    public void visit(Ast.Exp.True e)
    {
        emit(new Stm.Ldc(1));
    }

    @Override
    public void visit(Ast.Stm.Assign s)
    {

    }

    @Override
    public void visit(Ast.Stm.Block s)
    {
        s.stms.forEach(this::visit);
    }

    @Override
    public void visit(Ast.Stm.If s)
    {

    }

    @Override
    public void visit(Ast.Stm.Print s)
    {

    }

    @Override
    public void visit(Ast.Stm.While s)
    {

    }

    @Override
    public void visit(Ast.Method.MethodSingle m)
    {

    }

    @Override
    public void visit(Ast.Class.ClassSingle c)
    {

    }

    @Override
    public void visit(Ast.MainClass.MainClassSingle c)
    {

    }

    @Override
    public void visit(Ast.Program.ProgramSingle p)
    {

    }
}
