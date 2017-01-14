Lexer
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

由以上文法给出`Token`的定义
---

```java
public enum Kind
{
    Add, // +
    And, // &&
    Assign, // =
    Boolean, // boolean
    Class, // class
    Colon, // :
    Commer, // ,
    Dot, // .
    Else, // else
    EOF, // End of file
    False, // false
    ID, // Identifier
    If, // if
    Int, // int
    Lbrace, // {
    Lparen, // (
    LT, // <
    Main, // main
    New, // new
    Not, // !
    NUM, // Integer literal
    Print, // print, we just treat it as a key word
    Rbrace, // }
    Return, // return
    Rparen, // )
    Semi, // ;
    Sub, // -
    This, // this
    Times, // *
    True, // true
    Void, // void
    While, // while
}

public class Token
{
    public Kind kind;       // the kind of this token
    public String lexeme;   // extra lexeme of this token, if exsists
    public int lineNum;     // the token's position in source file
}
```

特别的，应当注意到 `Print`、`main` 不应该是关键字，仅仅是两个方法名而已。在此处将之视为关键字，能大大方便后期的分析和识别

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

词法分析实现
---

词法分析部分采用了手动实现的方式，具体思路如下

* 将注释略过不处理
* 跳过所有空白符号，例如 空格，换行符，回车符等
* 先处理所有特殊符号，例如`+`, `-`,`*`, `{`,`}`等等
* 再按照最长匹配原则，将其他串识别成数字或标识符
* 在此过程中，对行号变化保持关注
* 如果发现文法错误，那么立即报告错误并给出相应提示
* 此过程中未尝试错误恢复，发现任何未定义符号/串，词法分析器拒绝继续执行

例如，对于上文的程序样例中第6行 `print(new Test().Compute(10));   // just a print statement` 词法分析后的Token流是

```text
Token_Print : at line 6
Token_Lparen : at line 6
Token_New : at line 6
Token_ID : Test : at line 6
Token_Lparen : at line 6
Token_Rparen : at line 6
Token_Dot : at line 6
Token_ID : Compute : at line 6
Token_Lparen : at line 6
Token_NUM : 10 : at line 6
Token_Rparen : at line 6
Token_Rparen : at line 6
Token_Semi : at line 6
```

很显然，跳过了空格，注释等。

如果源代码中有文法错误，例如
`print(new Test().Compute(10));   / just a print statement`
那么遇到这个错误时，将会给出错误提示

```text
Comment should begin with "//"
Error is found at line 6
```