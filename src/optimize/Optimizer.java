package optimize;

/**
 * Created by Mengxu on 2017/1/31.
 */
public class Optimizer
{
    public void optimize(ast.Ast.Program.T prog)
    {
        UnUsedVarDel varDeler = new UnUsedVarDel();
        varDeler.visit(prog);

        ConstantFolder folder = new ConstantFolder();
        folder.visit(prog);

        UnReachableDel deler = new UnReachableDel();
        deler.visit(prog);

        DeadCodeDel deadDeler = new DeadCodeDel();
        deadDeler.visit(prog);

        ConstantAndCopyPropagation proper = new ConstantAndCopyPropagation();
        proper.visit(prog);
    }
}
