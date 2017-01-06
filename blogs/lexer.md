## Lexer

* 首先给出本程序语言的具体文法

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
   
* 由以上文法给出的程序样例

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