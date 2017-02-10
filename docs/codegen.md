# Code Generation

代码生成是编译器的最终工作，在这个阶段，就应该生成可执行的最终代码了。当然生成目标是任意的，可以生成字节码，IL，x86等(类)汇编代码，也可以去生成Java/C#/C/C++等代码，然后由该语言的编译器编译，做接下来的工作。

## Instruction Selection

之前已经提到过了，我们是在面向JVM生成字节码。由于JVM是基于栈式计算机概念的虚拟机，对其的代码生成较为简单，不必考虑寄存器分配等问题；而且操作指令较为丰富。

我们的语言由于支持的类型很有限，支持的基本类型只有`int`和`boolean`，因此对于长整型(`int`)，单精度浮点型(`float`)，双精度浮点型(`double`)，以及涉及到基本类型之间互相转换的操作指令可以不用考虑，我们支持的比较也比较少，因此跳转指令可以只考虑部分，由于不存在一些特殊类型或者操作符，比如`interface` `abstract class` `instanceof`等指令。

此外，我们注意到：

Java Virtual Machine Specification - 2.3.4 The `boolean` type
> Although the Java Virtual Machine defines a boolean type, it only provides
very limited support for it. There are no Java Virtual Machine instructions solely
dedicated to operations on boolean values. Instead, expressions in the Java
programming language that operate on boolean values are compiled to use values
of the Java Virtual Machine int data type.

因此，此处也是我们要注意的地方。

### New AST

在代码生成阶段，我们要将我们用抽象语法树表示的源程序，改写/翻译成用指令形式的表示，因此语法结构大变，整体风格从树状结构变成了线性结构，所以我们需要一个 New AST作为从AST到指令的中间表示。New AST主要关注点在Statement和Expression方面，即：把我们用到的指令先类型化，然后把原AST类型化。

按照JVM规范，JVM支持共计大约150个指令，我们用到的指令仅仅是相当小的一个子集，先给出我们使用的指令

```text
aload
areturn
astore
getfield
goto <label>
iadd
if_icmplt <label>
iload
imul
invokevirtual
ireturn
istore
isub
ldc
new
putfield
```

很显然，这是个相当“精简”的指令集。但是，还有两条扩展“指令”我们应当注意。

```text
label
```

这条“指令”的存在，是交由下文提到的`Jasmin`处理

```text
print
```

这条“指令”是我们纯粹为了编程简单而扩展的。考虑到我们的语言的特点，将他们作为指令对待，能够大大简化我们编程处理的复杂度，而且也不会造成任何的副作用，因为之前几个阶段的分析已经保证了源程序的合法。这样用不会有问题，因为后面我们会将扩展的指令翻译成jvm指令。

## Code Translation

有了上文的“完善”的指令集(对于我们这个程序来说)，我们接下来的工作便是将树状的AST转换成线性的指令。这里的转换主要关注的方法中的真正“干活”的代码，就是方法体内部的工作代码。下面通过一个例子展示具体工作。

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

观察以上一段代码，我们主要关注`Compute`方法编译出的指令，而且给出了较详细的注释。

```text
.method public Compute(I)I
.limit stack 4096 ; 栈调用深度，这个算法我们还没有实现，因此编译结果给出默认值 4096
.limit locals 4 ; 共计有4个本地变量
    ; num < 1 对于if语句中的判别式进行计算
    iload 1     ; 从本地变量表中加载变量1的值(num)到栈上
    ldc 1       ; 将整型数字1压入栈
    if_icmplt Label_2 ;比较两个值，如果第一个值(num)小于整数1，跳转至Label_2
    ldc 0       ; 将整数0压入栈(用于表示比较结果为false)
    goto Label_3
Label_2:
    ldc 1       ; 将整数1压入栈(用于表示比较结果为真)
Label_3:        ; 判别式计算完成
    ldc 1
    if_icmplt Label_0 ; 对于求值真假进行计算
    ldc 1       ; 将整数1压入栈
    istore 2    ; 将栈上的数字存入本地变量2
    goto Label_1
Label_0:
    iload 1     ; 从本地变量表中加载变量1的值 (num)
    aload 0     ; 从本地变量表中加载变量0的值 (this)
    iload 1
    ldc 1
    isub
    invokevirtual Test/Compute(I)I ; 调用实例方法(在指令参数处指出了方法的从属及签名)
    imul
    istore 2
Label_1:
    iload 2     ; 从本地变量表中加赞变量2的值
    ireturn     ; 从方法返回。
.end method
```

## Jasmin - ASCII Instructions File to .class File

最后的工作就是从字符形式的指令，由“汇编器”转换成二进制形式的.class文件，用于jvm的运行。此处我们采用了已经成熟的一个工具，[Jasmin](http://jasmin.sourceforge.net/)。