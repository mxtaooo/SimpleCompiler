Lexer
===

词法分析是编译器的第一个步骤。词法分析器读入源程序的字符流，将它们组织成有意义的词素，并对每个词素产生词法单元作为输出。

概要介绍
---

+ 本程序语言支持的符号是： `+` `-` `*` `&&` `:` `;` `,` `.` `!` `<` `{` `}` `(` `)`
  + `+` `-` `*` `&&` `<` 是二元操作符
  + `!` 是一元操作符
  + `.` 是方法调用符
  + `:` 是继承符
  + `{` `}` `(` `)` 是定界符
+ 本程序语言定义的关键字是： `boolean` `int` `class` `new` `if` `else` `while` `true` `false` `this` `void` `return`
  + `int` `boolean` 是类型关键字
  + `class` 声明一个类型时使用的前导关键字
  + `new` 创建一个某类型对象
  + `if` `else` `while` 分支/循环结构关键字
  + `true` `false` 上文所述 `boolean` 类型的两字面量
  + `this` 指代当前对象
  + `void` 空返回类型，只在`main`方法声明中用到
  + `return` 方法中返回语句的前导关键字
+ 特别说明: `Id` `NUM` `comment` `space` `\n` `\r` `EOF` `main` `print`
  + `Id` 标识符，将类型/变量名等处理成一个标识符类型的记号，具体意义取决于所处的位置
  + `NUM` 正整数
  + `space` `\n` `\r` 分别是空格符 换行符 回车符， 处理源代码文件时将忽略它们
  + `// comment` 行注释
  + `EOF` 是源代码的文件结束符
  + `main` 程序入口点 `main` 方法。应当注意，在实际的java语言中，对于程序入口点的 `main` 方法的完整定义是 `public static void main(String[] args)`。我们在这个“子集”里面，精简了这个声明，因为我们目前暂不支持 `static` 方法，也不需要给程序传递参数(不支持 `String` 类)。
  + `print` 输出语句，在实际java代码中是对于 `System.out.println()` 的调用，我们也将之简化。此外，`println`方法还有一系列的重载方法，但在我们这里只使用 `println(int arg)` 版本。


记号的定义
---

按照我们规定的程序文法，能给出记号的定义

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

其中 `kind` 字段指出该记号的类型，若有必要，将在 `lexeme` 字段中给出当前记号的值(例如标识符或数字)。此外，还会在 `lineNum` 字段给出当前记号的行号，这将有利于给用户一个尽可能精确的错误提示。

词法分析实现
---

词法分析部分采用了手动实现的方式，大致步骤如下

1. 初始化源文件的按字符输入流，并读入一个字符，初始化全局行号为1，然后循环进行后面的所有步骤
2. 检查该字符是否是文件结束符，如果是，那么返回一个携带了行号信息且类型为 `EOF` 的记号并结束词法分析；
3. 检查是否是空白符(空格、换行符、回车符)，如果是，直接尝试读下一个字符，但如果发生换行，行号应自加；
4. 查看是否是 `\` 字符，如果是，那么将尝试再读入一个 `\` 字符，这样就能确认从此处到行末是注释，跳过行注释，并在转入下一行时让全局行号自加；如果读入的第二个字符不是 `\` ，那么将给出一个携带了行号的错误信息并结束程序；
5. 查看当前的字符是否为特殊字符，例如 `+`, `-`, `*`, `{`, `}`等等，如果是，那就返回一个类型符合的、携带了行号信息的记号。特别地，如果是当前是 `&` 符，将尝试再读入一个字符来确认是否为我们支持的 `&&` ，如果是，那么正确返回记号，如果不是，那么给出错误提示并结束分析；
6. 当前的字符不是我们支持的特殊字符，那就一直读取下去，直到空格符/换行符/回车符/文件结束符/注释，这样是尽可能找到了一个最长的序列。检查这个序列是否是我们定义的关键字之一，如果是关键字，那么就返回正确类型和行号的记号；如果不是关键字，那么查看这个序列是不是一串数字，如果是数字，那么返回一个类型为`NUM`、正确的行号和数字串的记号；如果不是数字串，那么检查是否符合程序对于标识符的规定，如果符合，那么返回一个类型为`Id`，正确行号和标识符串的记号，如果不符合，那么只能是一个错误，给出提示并结束分析。

例子分析
---

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
那么遇到这个错误时，将会给出错误提示，并且结束词法分析。

```text
Comment should begin with "//"
Error is found at line 6
```