GRAMMAR
===

语言的文法
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

相关说明
---

+ 语言支持 `int` `boolean` 两种基本类型，支持自定义的类类型
+ 在类类型中支持字段和实例方法，不支持静态方法，不支持方法重载/重写，不支持访问权限控制(默认为 `public` )，支持继承(若无显式父类声明，则默认继承自 `Object` 类型)，但不支持显式调用父类方法
+ 只支持类的无参构造器
+ 支持 `if-else` `while` 的分支和循环控制，支持方法调用(由于所有方法都具有返回值，不支持`void`类型，因此未支持“裸方法调用”)
+ 算术运算支持 `+` `-` `*`，比较运算支持 `<`，逻辑运算支持 `&&` `!`
+ 支持 `\\` 引导的单行注释
+ 字段和变量仅支持声明，后期赋值，不支持声明时初始化
+ 编译器仅支持将所有代码写在同一个文件内部，并且约定第一个类是主类(仅有main方法，且作为程序入口)

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