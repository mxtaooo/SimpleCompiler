# Bad Things

## In Language Grammar

### 问题描述

在本程序语言中，我们对于method的文法定义是

```text
 MethodDec -> Type Id (FormalList)
              { VarDec* Statement* return Exp;}
```

这就意味着我们的方法只能是单返回语句，而且必须放在最后，因此如下的几种状况是不合法的

```java
int Div(int num, int target)
{
    while(num > 0)
    {
        if (num * 10 < target )
            return num;
        else
            num = num - 1;
    }
}

int Compute(int a, int b, boolean switch)
{
    if (switch)
        return a;
    else
        return b;
}
```

很显然，这个问题大大限制了我们的程序的表达能力。

但是我们之所以要这么做，是为了在保证了基本功能的状况下尽量进行简化，这样就能在较短的时间尽可能覆盖到一个编译器的各个方面，而不是从头到尾一直在某个方面挣扎。

### 一些思考

要搞定这个问题，显然，要重新思考方法的文法定义。我们之前对于方法的定义过于武断，要实现以上代码所示的功能，我们不应当把`return Exp;`抽离出来，虽然这样做其实大大简化了后面几个模块。我们应该将之当作某个类型的语句(即，应当有`Statement -> return Exp;`)

好的，假如我们把它的定义修改成这样，那么考虑对我们这个程序的影响。

对词法分析来说，实际上没有任何影响。

接下来便是语法分析阶段，我们需要将之输出成特定的`Stm.Return`类型的对象，由于`return`是一个关键字，因此这里其实是比较简单的。但是我们应当注意将之加入到该方法的`ReturnStms`集合中，用于接下来的语义分析阶段。

语义分析，这个阶段是最难的，也是影响最大的。源程序如果能通过这个阶段，就说明接受的源程序没有任何错误，能成功编译成可执行程序了，这个阶段容不得半点差错。首先应当对所有返回语句的表达式部分进行类型检查，这个倒是与目前没有太大差别，然后检查所有返回语句实际返回的类型是否与方法声明是否匹配，然后要检查是否存在无返回的情况，此外还要检查是否存在提前返回，导致有代码不会被执行到等等情况。

优化，简单优化不会影响，复杂优化暂不考虑。

代码生成，对该阶段无影响。

这样整理似乎并没有太棘手，但应当注意到，这里涉及到了对于全局都在用的AST的结构修改，而且是比较大的修改，由于我们之前是严格按照文法在做，因此返回语句的表达式直接定义在了方法层级里，要将之修改到语句层级里。棘手程度可想而知，这也体现了设计合理的重要性。

## ~~In Parser~~

### 问题描述

```java
class Instance
{
    Instance field;   // 1. -> Id Id Semi
    Instance method() // 2. -> Id Id Lparen Rparen
    {
        Instance local;           // 3. -> Id Id Semi
        local = new Instance();   // 4. -> Id Assign New Id Lparen Rparen Semi

        // Some Statements
    }
}
```

对于如上所述的情况，记号流1与2，3与4，没办法马上做出区分，要到第二个甚至第三个Token才能区分当前行到底是在做什么，过程中还是发生了“回溯”

此处发生的回溯不可避免，但是本编译器采用了在`parseVarDec`中间穿插了`parseMethod` 和 `parseAssign`，这种做法太丑陋

### 可能的解决思路

参考`InputStream`中`mark`/`reset`做法，在某处做一标记然后必要的时候能跳回去。

未参考实际实现，猜想是在`mark`的位置开始，记录下后继所有的字节，当`reset`时，优先输出已记录的字节，内部记录输出完毕之后再输出

### 解决方式

如果要给`Lexer`类实现`mark`/`reset`，那么`Parser`将总是要求`Lexer`提前开始`mark`，这样的话引入太多副作用，对于当前Token过于频繁的存储/删除对性能影响会比较大。

最终结果是给`Parser`实现`mark`/`reset`，对于某些Token的存储/回溯/删除自行决定，`Lexer`只负责流式给出下一个Token。

## In Semantic

```java
class Instance
{
    int field0;
    int field1;

    int Compute(int num)
    {
        int result0;
        int result1;
        result0 = result1 + field0 + num;
        return result0;
    }

    int Init0()
    {
        field0 = 0;
        field1 = 1;
        return 0;
    }

    int Init1(boolean switch)
    {
        if (switch)
            field0 = 1;
        else
            field1 = 1;
        return 0;
    }
}
```

如以上代码所示，对于 `Compute` 方法，很明显，对于 `result1`、`field0` 的引用都是非法的，因为都是在尝试对一个尚未赋值的变量/字段取值，语义分析中，我们应当报告此类错误。而一看上去，似乎并不难，至少对于本地变量来说，并不难。给标识符加一个 `isAssigned` 字段即可，顺序遍历到某个语句，尝试对某个标识符取值的时候若是发现该标识符还未曾被初始化，报告错误即可。

但是我们考虑这样一种情况，假如说在 `Compute` 方法，给 `result0` 赋值之前调用了 `Init0` 方法，那么此刻的 `field0` 是被赋值过的，对其取值是合法的。那么我们可以考虑，可以先扫描一遍语法树，确定所有方法的类型检查顺序(暂不考虑递归调用情况)，然后按顺序对方法进行检查，并记录下每个方法对外界的副作用。当调用的时候通过检查记录的副作用情况，来确定对某个标识符的求值是否合法。

但是这样并不完美，再考虑一种情况。 在 `Compute` 方法，给 `result0` 赋值之前调用了 `Init1` 方法，并且给的参数是 `false` 。那么此时， `field0` 其实并未被赋值，对其的求值是非法的。即使我们是提前确定了方法的检查顺序，但是对于 `Init1` ，我们如何确定它对于外界的副作用呢？很显然，这取决于调用方的行为，甚至运行时用户的输入，这是一个无解的问题。

### 最终决定

观察 `C#`/`Java` 这两个主流语言的主要编译器的行为如下

| 对字段的赋值情况 | C# (Roslyn) | Java (javac.exe) |
|:---:|:---:|:---:|
| 对字段无赋值行为 | 给出警告 | 无任何提示|
| 对字段有赋值行为，未提前调用 | 无任何提示 | 无任何提示 |
| 对字段赋值行为取决于外界 | 无任何提示 | 无任何提示 |

对于 `C#` 未测试其旧版编译器 `csc.exe`

看来这个问题确实无解，或者说，由于处在“外界”，有很多方式能对其产生副作用，编译器根本没办法、没有足够可靠的信息确定每个字段的赋值状态，只有把程序运行一下才能确定，但那是运行时的事情了，编译期根本无从下手。这就取决于程序员自己的把握，编译器并没有那么强的能力，只能尽可能的做检查，给程序员尽可能多的提示，作为检查/修改/重构代码的依据。


## In Code Generation

Java Virtual Machine Specification - 2.3.4 The `boolean` type
> Although the Java Virtual Machine defines a boolean type, it only provides
very limited support for it. There are no Java Virtual Machine instructions solely
dedicated to operations on boolean values. Instead, expressions in the Java
programming language that operate on boolean values are compiled to use values
of the Java Virtual Machine int data type.

我们这个程序加入了`boolean`这一类型，然后强调了它与`int`类型之间的区别，编译器拒绝了双方任何的直接互操作，但是到了代码生成阶段，两个又变成了同一个类型`int`。虽然这里放在了badthings里，但是个人认为这并不是个错误的决定，看上去绕了一个大圈子，但是这样对程序的安全合法做了最大的保证，类型检查能通过的，就一定是合法的。如果允许`boolean`类型和`int`类型之间的互操作，就要引入一些的前提/隐式声明/约定之类的东西，增加的语言复杂度，对程序安全也带来了隐患。