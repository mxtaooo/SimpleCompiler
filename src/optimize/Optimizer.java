package optimize;

/**
 * Created by Mengxu on 2017/1/31.
 */
public class Optimizer
{
    public void optimize(ast.Ast.Program.T prog)
    {
        UnUsedVarDel varDeler = new UnUsedVarDel();
        varDeler.givesWarning = true;
        ConstantFolder folder = new ConstantFolder();
        UnReachableDel deler = new UnReachableDel();

        boolean flag;
        do
        {
            varDeler.visit(prog);
            varDeler.givesWarning = false;
            folder.visit(prog);
            deler.visit(prog);
            flag = varDeler.isOptimizing()
                    || folder.isOptimizing()
                    || deler.isOptimizing();
        } while (flag);
    }
}
