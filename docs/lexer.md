Lexer
===

词法分析是编译器的第一个步骤。词法分析器读入源程序的字符流，将它们组织成有意义的词素，并对每个词素产生词法单元作为输出。

`Token`的定义
---

按照我们规定的程序语法，能给出记号的定义

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

词法分析实现
---

词法分析部分采用了手动实现的方式，具体步骤如下

* 将注释略过不处理
* 跳过所有空白符号，例如 空格，换行符，回车符等
* 处理特殊符号，例如 `+`, `-`, `*`, `{`, `}`等等
* 按照最长匹配原则，将其他串识别成数字或标识符
* 在此过程中，对行号变化保持关注
* 如果发现文法错误，那么立即报告错误并给出相应提示
* 此过程中未尝试错误恢复，发现任何未定义符号/串，词法分析器拒绝继续执行

例如，对于语句样例 `print(new Test().Compute(10));   // just a print statement` 词法分析后的Token流是

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

很显然，空白符、注释被直接跳过。

如果源代码中有文法错误，例如
`print(new Test().Compute(10));   / just a print statement`
那么遇到这个错误时，将会给出错误提示

```text
Comment should begin with "//"
Error is found at line 6
```