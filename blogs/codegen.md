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

有了上文的“完善”的指令集(对于我们这个程序来说)，我们接下来的工作便是将树状的AST转换成线性的指令。这里的转换主要关注的方法中的真正“干活”的代码，

## Jasmin - ASCII Instructions File to .class File