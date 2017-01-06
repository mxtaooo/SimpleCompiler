## Lexer

>### 首先给出本程序语言的具体文法

```
 Program -> MainClass ClassDec*

 MainClass -> class Id { void main() { Statement }}

 ClassDec -> class Id { VarDec* MethodDec* }
          -> class Id : Id { VarDec* MethodDec* }
         
 VarDec -> Type Id;
 
 MethodDec -> Type Id (FormalList) 
              { VarDec* Statement* return Exp;}

 FromalList -> Type Id FormalRest*
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
           -> Id[Exp] = Exp;
 
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
   
>### 由以上文法给出的程序样例

```
class TestMain 
{
    void main()
    {
        print(new Test().Compute(10));   
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
>### 实现详解  
词法分析部分采用了手动实现的方式，具体思路如下  
1. 将注释略过不处理  
2. 跳过所有空白符号，例如 空格，换行符，回车符等  
3. 先处理所有特殊符号，例如`+`, `-`,`*`, `{`,`}`等等  
4. 再按照最长匹配原则，将其他串识别成数字或标识符  
5. 在此过程中，对行号变化保持关注  
6. 如果发现文法错误，那么立即报告错误并给出相应提示    
