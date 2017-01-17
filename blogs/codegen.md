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

由于此时的语法树，尤其是表达式部分已经和原本的表达式已经完全不同


## New AST

## Code Translation

## ASCII Instructions File to .class File