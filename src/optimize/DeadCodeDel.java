package optimize;

import ast.Ast;

import java.util.HashSet;

/**
 * Created by Mengxu on 2017/1/27.
 */
public class DeadCodeDel implements ast.Visitor
{
    private HashSet<String> curFields;  // the fields of current class
    private HashSet<String> localVars;  // the local variables and formals in current method
    private HashSet<String> localLiveness;  // the living id in current statement
    private boolean isAssign;   // current id is in the left of assign(true), or is being evaluated(false)
    private boolean shouldDel;  // should delete current statement?

    @Override
    public void visit(Ast.Type.Boolean t) {}

    @Override
    public void visit(Ast.Type.ClassType t) {}

    @Override
    public void visit(Ast.Type.Int t) {}

    @Override
    public void visit(Ast.Dec.DecSingle d) {}

    @Override
    public void visit(Ast.Exp.Add e)
    {

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

    }

    @Override
    public void visit(Ast.Exp.True e)
    {

    }

    @Override
    public void visit(Ast.Stm.Assign s)
    {

    }

    @Override
    public void visit(Ast.Stm.Block s)
    {

    }

    @Override
    public void visit(Ast.Stm.If s)
    {

    }

    @Override
    public void visit(Ast.Stm.Print s)
    {
        this.isAssign = false;
        this.visit(s.exp);
        this.shouldDel = false;
    }

    @Override
    public void visit(Ast.Stm.While s)
    {
        //((Ast.Stm.Block) s.body).stms.forEach(this::visit);
        this.visit(s.body);
        if (!(s.body instanceof Ast.Stm.Block) && shouldDel)
            s.body = null;
        this.shouldDel = s.body == null
                || (s.body instanceof Ast.Stm.Block
                && ((Ast.Stm.Block) s.body).stms.size() == 0);
        this.isAssign = false;
        this.visit(s.condition);
    }

    @Override
    public void visit(Ast.Method.MethodSingle m)
    {
        this.localVars = new HashSet<>();
        m.formals.forEach(f -> this.localVars.add(((Ast.Dec.DecSingle) f).id));
        m.locals.forEach(l -> this.localVars.add(((Ast.Dec.DecSingle) l).id));
        this.localLiveness = new HashSet<>();

        this.isAssign = false;
        this.visit(m.retExp);

        // LinkedList<Integer> delStms = new LinkedList<>();
        for (int i = m.stms.size() - 1; i >= 0; i++)
        {
            this.visit(m.stms.get(i));
            if (this.shouldDel)
                m.stms.remove(i);
            // if (this.shouldDel)
            //     delStms.add(i);
        }
        // delStms.forEach(d -> m.stms.remove(d));
    }

    @Override
    public void visit(Ast.Class.ClassSingle c)
    {
        this.curFields = new HashSet<>();
        c.fields.forEach(f ->
                this.curFields.add(((Ast.Dec.DecSingle) f).id));

        c.methods.forEach(this::visit);
    }

    @Override
    public void visit(Ast.MainClass.MainClassSingle c) {}

    @Override
    public void visit(Ast.Program.ProgramSingle p)
    {
        p.classes.forEach(this::visit);
    }
}
