GRAMMAR
===

本程序语言的文法
---

```text
 ## GRAMMAR

 Program -> MainClass ClassDec*

 MainClass -> class Id { void main() { Statement }}

 ClassDec -> class Id { VarDec* MethodDec* }
          -> class Id : Id { VarDec* MethodDec* }

 VarDec -> Type Id;

 MethodDec -> Type Id (FormalList)
              { VarDec* Statement* return Exp;}

 FormalList -> Type Id FormalRest*
            ->

 FormalRest -> , Type Id

 Type -> int
      -> boolean
      -> Id

 Statement -> { Statement* }
           -> if (Exp) Statement else Statement
           -> while (Exp) Statement
           -> print(Exp);
           -> Id = Exp;

 Exp -> Exp op Exp
     -> Exp.Id(ExpList)
     -> Integer Literal
     -> true
     -> false
     -> Id
     -> this
     -> new Id()
     -> !Exp
     -> (Exp)

 Op -> +
    -> -
    -> *
    -> <
    -> &&

 ExpList -> Exp ExpRest*
         ->

 ExpRest -> ,Exp

 Id -> [A-Za-z_][A-Za-z0-9_]*

 Integer Literal -> [0-9]+

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