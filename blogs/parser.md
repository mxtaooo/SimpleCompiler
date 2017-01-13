Parser
===

AST (abstract syntax tree)
---

语法分析器工作的结果便是抽象语法树。分析器读完Token流之后，在语言的语法规则指导下，对源程序有了自己的理解，将这种理解转化成方便后期使用的一种中间表示，这个中间表示是层次分明的树形结构，叫做抽象语法树。

本质来讲，抽象语法树可以看作是对该语言文法的“类型化”重写，即对于每个非终结符，每个产生式，给出确切定义，并赋予不同的属性。类型实例便成为要输出的抽象语法树的“节点”

例如对于lexer.md中给出的程序文法，选择一个片段：对于`Statement`的文法描述如下

```text
 Statement -> Id = Exp;
           -> { Statement* }
           -> if (Exp) Statement else Statement
           -> while (Exp) Statement
           -> print(Exp);
```

由此给出，对于`Statement`的抽象语法树节点定义是

```java
// Statement
public class Stm
{
    public static abstract class T
    {
        public int lineNum;
    }

    public static class Assign extends T
    {
        public String id;
        public Exp.T exp;
        public Type.T type; // type of the id

        public Assign(String id, Exp.T exp, int lineNum)
        {
            this.id = id;
            this.exp = exp;
            this.type = null;
            this.lineNum = lineNum;
        }
    }

    public static class Block extends T
    {
        public java.util.LinkedList<T> stms;

        public Block(java.util.LinkedList<T> stms, int lineNum)
        {
            this.stms = stms;
            this.lineNum = lineNum;
        }
    }

    public static class If extends T
    {
        public Exp.T condition;
        public T then_stm, else_stm;

        public If(Exp.T condition, T then_stm, T else_stm, int lineNum)
        {
            this.condition = condition;
            this.then_stm = then_stm;
            this.else_stm = else_stm;
            this.lineNum = lineNum;
        }
    }

    public static class Print extends T
    {
        public Exp.T exp;

        public Print(Exp.T exp, int lineNum)
        {
            this.exp = exp;
            this.lineNum = lineNum;
        }
    }

    public static class While extends T
    {
        public Exp.T condition;
        public T body;

        public While(Exp.T condition, T body, int lineNum)
        {
            this.condition = condition;
            this.body = body;
            this.lineNum = lineNum;
        }
    }
}
```

语法分析实现
---

语法分析器的任务是读入记号流，由语言的语法规则指导，生成抽象语法树。

分析算法主要分为

+ 自顶向下分析
    - 递归下降分析算法 (预测分析算法)
    - LL 分析算法
+ 自底向上分析
    - LR 分析算法

本程序采用的是递归下降分析算法(预测分析)，该算法的主要优点

- 分析高效，线性时间复杂度
- 容易实现，方便手工编码
- 错误定位和诊断信息准确

很多开源/商业编译器也采用了该算法，比如GCC4.0，LLVM等

该算法的基本思想是：

- 为每个非终结符构造一个分析函数
- 用**前看符号**指导产生式规则的选择

例如对于前面给出的`Statement`的文法，可以给出一个针对它的处理函数

```java
// Statement -> { Statement* }
//           -> if (Exp) Statement else Statement
//           -> while (Exp) Statement
//           -> print(Exp);
//           -> id = Exp;
private Ast.Stm.T parseStatement()
{
    Ast.Stm.T stm = null;
    int lineNum;
    if (current.kind == Kind.Lbrace)
    {
        eatToken(Kind.Lbrace);
        lineNum = current.lineNum;
        stm = new Ast.Stm.Block(parseStatements(), lineNum);
        eatToken(Kind.Rbrace);
    } else if (current.kind == Kind.If)
    {
        lineNum = current.lineNum;
        eatToken(Kind.If);
        eatToken(Kind.Lparen);
        Ast.Exp.T condition = parseExp();
        eatToken(Kind.Rparen);
        Ast.Stm.T then_stm = parseStatement();
        eatToken(Kind.Else);
        Ast.Stm.T else_stm = parseStatement();
        stm = new Ast.Stm.If(condition, then_stm, else_stm, lineNum);
    } else if (current.kind == Kind.While)
    {
        lineNum = current.lineNum;
        eatToken(Kind.While);
        eatToken(Kind.Lparen);
        Ast.Exp.T condition = parseExp();
        eatToken(Kind.Rparen);
        Ast.Stm.T body = parseStatement();
        stm = new Ast.Stm.While(condition, body, lineNum);
    } else if (current.kind == Kind.Print)
    {
        lineNum = current.lineNum;
        eatToken(Kind.Print);
        eatToken(Kind.Lparen);
        Ast.Exp.T exp = parseExp();
        eatToken(Kind.Rparen);
        eatToken(Kind.Semi);
        stm = new Ast.Stm.Print(exp, lineNum);
    } else if (current.kind == Kind.ID)
    {
        String id = current.lexeme;
        lineNum = current.lineNum;
        eatToken(Kind.ID);
        eatToken(Kind.Assign);
        Ast.Exp.T exp = parseExp();
        stm = new Ast.Stm.Assign(id, exp, lineNum);
    } else
        error();
    return stm;
}
```

其中`eatToken`方法是吞掉指定类型的记号，如果当前记号类型与要求吞掉的类型不符，说明源程序一定存在语法错误，能够实现立即报错，并能给出较为准确的错误信息。

实例分析
---

对于lexer.md 中给出的程序样例

```java
class TestMain
{
    // This is the entry point of the program
    void main()
    {
        print(new Test().Compute(10));   // just a print statement
    }
}

class Test
{
    int Compute(int num)
    {
        int total;
        if ( num < 1)
            total = 1;
        else
            total = num * (this.Compute(num-1));
        return total;
    }
}
```

语法分析完成后的输出的抽象语法树如下示意


+ Program
    + MainClass
        + id : TestMain
        + MainMethod
            + id : main
            + stm (size = 1)
                + Print // statement -> print(exp);
                    + exp : Call // expression -> exp.id(args)
                        + exp: NewObject // expression -> new id()
                            + id : Test
                        + id : Compute
                        + args (size = 1)
                            + Num(10)
    + Classes (size = 1)
        + Class
            + id : Test
            + base : null
            + fields (size = 0)
            + methods (size = 1)
                + method
                    + retType : int
                    + id : Compute
                    + formals (size = 1)
                        + VarDecl
                            + Type : int
                            + id : num
                    + locals (size = 1)
                        + VarDecl
                            + Type : int
                            + id : total
                    + stms (size = 1)
                        + If // Statement -> if (condition) then_stm else else_stm
                            + condition
                                + LT // expression -> leftExp < rightExp
                                    + leftExp
                                        + exp : id // expression -> id
                                            + id(num)
                                    + rightExp
                                        + exp : Num // expression -> Num
                                            + Num(1)
                            + then_stm (size = 1) // then statement
                                + stm: Assign // statement -> id = exp;
                                    + id : total
                                    + exp : Num // expression -> Num
                                        + Num(1)
                            + else_stm (size = 1) // else statement
                                + stm : Assign // statement -> id = exp;
                                    + id : total
                                    + exp : Times // expression -> leftExp * rightExp
                                        + leftExp
                                            + exp : id // expression -> id
                                                + id(num)
                                        + rightExp
                                            + exp : Call // expression -> exp.id(args)
                                                + exp : This // exp -> this
                                                    + This
                                                + id : Compute
                                                + args (size = 1)
                                                    + exp : Sub // expression -> leftExp - rightExp
                                                        + leftExp
                                                            + exp : id
                                                                + id(num)
                                                        + rightExp
                                                            + exp : Num
                                                                + Num(1)
                    + retExp // return exp;
                        + exp : id
                            + id(total)

