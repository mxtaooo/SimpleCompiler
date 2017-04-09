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