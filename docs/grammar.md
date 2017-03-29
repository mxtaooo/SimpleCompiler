GRAMMAR
===

本程序语言的文法
---

```text
## GRAMMAR

Program
   -> MainClass
   | MainClass ClassDecList

MainClass
   -> class Id { void main() { Statement }}

ClassDecList
   -> ClassDec
   | ClassDec ClassDecList

ClassDec
   -> class Id { VarDecList MethodDecList }
   | class Id : Id { VarDecList MethodDecList }

VarDecList
   -> VarDec
   | VarDec VarDecList
   |

VarDec -> Type Id;

MethodDecList
   -> MethodDec
   | MethodDec MethodDecList
   |

MethodDec -> Type Id (FormalList)
             { VarDecList StatementList return Exp;}

FormalList
   -> Type Id
   | Type Id, FormalList
   |

Type -> int
     -> boolean
     -> Id

StatementList
   -> Statement
   | Statement StatementList
   |

Statement
   -> { StatementList }
   | if (Exp) Statement else Statement
   | while (Exp) Statement
   | print(Exp);
   | Id = Exp;

Exp
   -> Exp Op Exp
   | Exp.Id(ExpList)
   | IntegerLiteral
   | true
   | false
   | Id
   | this
   | new Id()
   | !Exp
   | (Exp)

Op -> +
   | -
   | *
   | <
   | &&

ExpList
   -> Exp
   | Exp, ExpList
   |

Id -> [A-Za-z_][A-Za-z0-9_]*

IntegerLiteral -> [0-9]+

LineComment -> // the total line is comment
```

由以上文法给出的程序样例
---

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

备注
---

应当注意，在实际的java语言中，对于程序入口点的 `main` 方法的完整定义是 `public static void main(String[] args)`。我们在这个“子集”里面，精简了这个声明，因为我们目前暂不支持 `static` 方法，也不需要给程序传递参数(不支持 `String` 类)。精简处理的收益仅是简化一些语法分析的工作。

同样的，对于 `print` 语句，在实际java代码中是对于 `System.out.println()` 的调用，我们也将之简化了。