# Code Generation

代码生成是编译器的最终工作，在这个阶段，就应该生成可执行的最终代码了。当然生成目标是任意的，可以生成字节码，IL，x86等(类)汇编代码，也可以去生成Java/C#/C/C++等代码，然后由该语言的编译器编译，做接下来的工作。

考虑到JVM接受的字节码文件，每个类单独一个文件，在类内部以方法组织代码。保证该阶段工作的正确，我们将这阶段的工作划分成三个步骤

1. 语法树重写： 处理方法中的语句表达式，将方法中的代码“指令化”，但是语法树的其余部分内容暂不改变
2. 代码翻译： 为每个类分别生成一个文件，将ASCII形式的指令输出到文件中，此阶段过后不再需要抽象语法树
3. 字节码生成： 调用开源工具 `jasmin`，阶段二输出的文件“二进制化”，输出成JVM真正可用的文件

## “指令集”选择

我们这门语言的目标是JVM。由于JVM是基于栈式计算机概念的虚拟机，对其的代码生成较为简单，不必考虑寄存器分配等问题，而且操作指令较为丰富。对于正在执行的每个方法，JVM维护了一个本地变量表和操作栈，由于程序支持的方法都是实例方法，因此隐含地第0个参数必定是对象自身的引用，剩下的便是参数和该方法中定义的变量。

我们的语言由于支持的类型很有限，支持的基本类型只有`int`和`boolean`，因此对于长整型(`long`)、单精度浮点型(`float`)、双精度浮点型(`double`)、字符型(`char`)的相关操作以及涉及到他们之间互相转换的操作指令可以不用考虑；比较操作符只有小于，因此可以只考虑部分跳转指令；此外我们这门语言支持的概念较少(没有 `interface` `abstract class` 等，没有位操作符、`instanceof`操作符等)，因此对于它们的相关指令的支持也可以暂时不考虑。

此外，我们注意到：

Java Virtual Machine Specification - 2.3.4 The `boolean` type
> Although the Java Virtual Machine defines a boolean type, it only provides
very limited support for it. There are no Java Virtual Machine instructions solely
dedicated to operations on boolean values. Instead, expressions in the Java
programming language that operate on boolean values are compiled to use values
of the Java Virtual Machine int data type.

因此，在我们重写语法树的时候应当注意到将 `boolean` 类型及其相关操作转换成对于 `int` 类型的相关操作。

按照JVM规范，JVM支持共计大约150个指令，我们用到的指令仅仅是相当小的一个子集，这里给出我们使用的指令以及对其作用的简要解释。

```text
aload <varnum>  ; 从本地变量表找到一个对象引用，并将之压入操作栈。该指令需要一个无符号整数参数<varnum>，指出该对象引用在本地变量表中的位置
areturn ; 该指令从当前操作栈弹出一个对象引用，并将之压入调用者操作栈。该对象必须和方法声明的返回对象类型兼容。
astore <varnum>  ; 从当前操作栈弹出一个对象引用，并将之存储至本地变量表。该指令需要一个无符号整数参数<varnum>，指出在变量表中的存储位置
getfield <field-spec> <descriptor>  ; 将当前操作栈顶的对象引用弹出，然后将该对象的字段压入该操作栈。该指令需要两个参数，<field-spec>指出了字段的“路径”，由两部分组合成，类名和字段名，<descriptor>指出了该字段的类型。
goto <label>    ; 无条件跳转指令，直接跳到<label>标记处。<label>标记指向的代码必须处于本方法内。
iadd    ; 从操作栈上弹出两个整数，并将相加结果压入操作栈。
if_icmplt <label>   ; 条件跳转指令。从操作栈上连续弹出两个整型数，若第二个数小于第一个，则跳转到<label>标记的代码，否则顺序执行。同样的，<label>必须指向本方法中的某个位置。
iload <varnum>  ; 将保存在本地变量表中的整型数压入操作栈。该指令需要一个无符号整数参数<varnum>，指出该整型数在本地变量表中的位置
imul    ; 从操作栈上连续弹出两个整型数，将其相乘，并将结果压入操作栈
invokevirtual <method-spec> ; 调用一个实例方法。该指令需要一个参数<method-spec>，该参数由类名，方法名，方法描述符组成
ireturn ; 该指令从当前操作栈弹出一个整数，并将之压入调用者操作栈
istore <varnum> ; 将整数存入本地变量表。该指令需要一个无符号整数参数<varnum>，该参数指出了存储位置在本地变量表中的位置。
isub    ; 从操作栈上连续弹出两个整型数，计算第二个数字减去第一个数字的结果，并将结果压入操作栈
ldc <value> ; 将一个单字常量压入操作栈。该指令主要一个参数<value>，该参数就是要压入操作栈的值
new <class> ; 创建一个对象。该指令需要一个参数<class>，该参数指出要创建的对象的类型
putfield <field-spec> <descriptor> ; 从操作栈上先弹出要一个值，然后弹出一个对象的引用，为这个对象的字段赋值。该指令需要两个参数，<field-spec>指出了字段的“路径”，由两部分组合成，类名和字段名，<descriptor>指出了该字段的类型。
```

很显然，这是个相当“精简”的指令集。但还有两条扩展“指令”应当注意到。

```text
label
```

上文也有提到，它指出了要跳转到的目标位置，除了作为标记，没有别的任何应用。而且也并不算是JVM支持的“指令”，但我们将之作为指令对待，能方便我们生成代码。

```text
print
```

这条“指令”是我们为了代码生成方便而扩展的。考虑到我们的语言的特点，遇见 `print()` 语句暂将之作为一条指令对待，能简化些代码生成的工作。不需要担心是否引发副作用，之前几个阶段的分析已经保证了源程序的合法。而且接下来我们就会将扩展的指令翻译成jvm指令。


## 语法树重写

在代码生成阶段，我们要将我们用抽象语法树表示的源程序，翻译成用指令形式的表示，因此语法结构大变，大致风格从树状结构变成了线性结构，所以我们需要一个新语法树作为从AST到指令的中间表示。

该新语法树和原语法树主要不同之处在语句和表达式层面，即：语句和表达式在这个步骤将会被“指令化”。下面我们给出“指令化”的具体实现方式

+ 表达式

  在我们的程序中，所有形式的表达式具有一个特点，当计算完毕后，操作栈顶必定是该表达式的计算结果。

  + 数字

    该类型的算是最简单的表达式了，输出一条 `ldc <num>` 即可，例如数字5，它对应的指令是 `ldc 5`, 表示向操作栈上压入整数5。

  + `true`/`false`

    上文提到，对于该类型的操作应当替换成对于整型数的操作。当遇到了`true`，就向操作栈上压入整型数1，反之，压入0。

  + `this`

    这是个关键字，指出该实例方法所在的对象本身，隐含地作为方法的第0个参数传入，位于本地方法表的第0处。因此，遇见该关键字，输出 `aload 0` 即可完成任务。

  + 创建对象

    这也是一种较为简单的表达式形式，但是输出的指令是存在固定形式的。对于`new MyObject()`表达式，输出如下三条指令
    ```text
    new MyObject
    dup
    invokespecial MyObject/<init>()V    ; 使用 invokespecial 指令调用该类的构造方法，由于程序并未支持含参构造器，因此该过程不需要考虑参数的相关问题
    ```
    这三条指令执行完毕后，栈顶便有一个 MyObject 类型的实例的引用。

  + 取值

    对于某个标识符的“身份”来说，它可能是个简单的变量，也可能是这个对象的某个字段；对于它的内容来说，可能是个简单的数字，也可能是某个对象的引用。因此取得某个标识符的值是个较为棘手的问题。

    + 确定该标识符是否为字段，如果是，那么要用到`getfield`指令，在指令的参数中就已经分别体现了找到该字段的路径和字段的具体类型，指令执行完毕后，字段中存储的值就被压入了栈顶
    + 如果该标识符是个本地变量或参数，那么就要确认该标识符在本地变量表中的位置和标识符的类型，如果是个值，那么就用`iload`指令；如果是个对象的引用，那么就用到`aload`指令。

  + 加、减、乘

    这是很常规的计算方式，其计算思路也很简单，先计算左值和右值，然后将两值进行计算。
    例如对于表达式 `3+4+5`，不考虑常量折叠优化的效果，其语法树是
    //TODO： (3+4)+5 树形表示
    生成的指令是
    ```text
    ; 计算3+4+5
    ldc 3
    ldc 4
    iadd    ; 先后弹出4、3，计算4+3，将结果压入栈
    ldc 5
    iadd    ; 弹出5、7，计算5+7，将结果压入栈
    ```

  + 比较运算

    程序仅支持小于运算。该运算的结果是`boolean`类型值，但在此处，应将之`int`化，程序采用的方式是1为`true`，0为`false`。此处通过一个例子来展示小于运算的工作细节。
    ```text
      ; 3 < 4
      ldc 3
      ldc 4         ; 顺序向栈上压入左右两值
      if_icmplt t   ; 弹出两个值执行比较，如果为真，那么跳转到 t 标签处，如果为假，那么顺序向下执行
      ldc 0
      goto r        ; 向操作栈压入0后，跳转到表达式出口(r 标签处)
    t:
      ldc 1         ; 向操作栈压入1
    r:
    ```
    这样，小于表达式执行完毕之后，栈顶为0或1，指出比较的结果。

  + 逻辑运算

    程序支持逻辑与和逻辑非运算。
    + 逻辑与
        ```text
          ; exp1 && exp2
          ; 计算 exp1，计算结束后，0或1将被压入栈顶，用于表示 exp1 的结果
          ldc 1
          if_icmplt f   ; 将 exp1 的结果与 1 比较，如果小于成立，说明 exp1 为否，exp2 没必要继续计算，直接跳转到 f 标签处执行
          ; 计算 exp2，计算结束后，0或1将被压入栈顶，用于表示 exp2 的结果
          ldc 1
          if_icmplt f   ; 将 exp2 的结果与 1 比较，如果小于成立，说明 exp2 为否，跳转到 f 标签处执行
          ldc 1
          goto r        ; 向操作栈压入1后，跳转到表达式出口(r 标签处)
        f:
          ldc 0
        r:
        ```
        逻辑与运算结束后，操作栈顶是0或者1，指出逻辑运算与运算的结果。
    + 逻辑非

        逻辑非的计算思路与逻辑与大致相同。计算表达式，如果结果为真，那么压入0，如果为假，那么压入1。

  + 方法调用

    实例方法的一次调用主要关注三个方面：对象、参数和方法名。对象可能是某个变量、某个字段、`this`，甚至某个表达式求得的结果。参数方面，应当考虑到参数不存在的情况。下面通过一个例子展示其工作细节。
    ```text
    ; this.Compute(1,2) 返回两参数之和
    aload 0 ; 将 this 的引用压入操作栈
    ldc 1
    ldc 2   ; 顺序压入两个参数
    invokevirtual ClassName/Compute(II)I    ; 顺序弹出所有参数和对象引用，调用指令参数所描述的方法
    ```
    指令执行完毕后，操作栈上的参数和对象引用被弹出，栈顶是方法的执行结果

+ 语句

  程序支持的语句类型有限，各语句具有自身的特点，为每种语句找到一个固定的模式，按照该模式生成指令序列即可。

  + if-else 语句

    对于该类型的语句，按照如下模式生成指令。
    ```text
      ; if (condition) { then_stms } else { else_stms }

      'condition'  ; condition 的指令序列
      ldc 1
      if_icmplt l   ; 将condition表达式结果和1比较，若小于成立，进入 else 分支
      'then_stms'   ; then 分支的指令序列
      goto r    ; then 分支执行完毕之后，跳转到该语句出口
    l:
      'else_stms'   ; else 分支的指令序列
    r:
    ```
    该模式思路很明显，查看条件语句是否满足，然后跳转到不同的分支。应当注意到，在then分支结束之后，应当添加跳转到语句结束的指令。

  + while 语句

    该类型的语句的模式是
    ```text
      ; while(condition) { body }

    con:
      'condition' ; condition 的指令序列
      ldc 1
      if_icmplt end ; 判断循环条件是否满足，若不满足，直接跳出
    body:
      'body'    ; body 的指令序列
      goto con  ; 跳转到条件判断处
    end:
    ```

  + print 语句

    在语法树重写步骤中，遇见print语句之后，立即输出一条`print`指令。该“指令”其实是某个特定方法调用（指令序列）的简化形式，其完整形式是
    ```text
    getstatic java/lang/System/out Ljava/io/PrintStream;
    swap
    invokevirtual java/io/PrintStream/println(I)V
    ```
    在之后的阶段中，`print`指令将被翻译成完整形式。

  + 赋值语句

    在该语句中，需要关注的是赋值目标的相关信息。
    + 确定该标识符是否为字段，如果是，那么要用到`putfield`指令，在指令的参数中就已经分别体现了找到该字段的路径和字段的具体类型，指令执行完毕后，栈顶的值就被弹出并存储到字段中
    + 如果该标识符是个本地变量或参数，那么就要确认该标识符在本地变量表中的位置和标识符的类型，如果是个值，那么就用`istore`指令；如果是个对象的引用，那么就用到`astore`指令。

+ `boolean` 的 `int`化

    上文提到的各个过程中，涉及到`boolean`类型的相关操作都被转换成了`int`类型的操作，但这并不会导致类型的不安全，因为在代码的语义分析阶段，已经进行了足够多的检查和分析。

## 代码翻译

如果将语法树的“指令化”过程划分成4个层次，从低到高依次是表达式层次，语句层次，方法层次，类层次。在上一步骤，完成了第一层次和第二层次的“指令化”，在这个步骤将完成第三第四层次。

在这个步骤，还将把一直保存在内存中的指令输出成文本文件，用于下个步骤。

+ 方法

    首先给出方法的声明格式
    ```text
    .method public <NameAndTypeInfo>
    .limit stack 4096
    .limit locals <num>
        ; 指令序列
    .end method
    ```
    按照这个格式，添加上必要的信息，例如方法名和类型信息，参数个数。注意对于栈深度的限制处，由于程序不支持对它的相关统计，因此给出了默认值4096。
    下文实例分析处体现了对于该格式的应用。

+ 类

    类型的声明格式如下
    ```text
    .class public <ClassName>
    .super <BaseClassName>
    .field public <FieldName> <TypeInfo>
    .field public <FieldName> <TypeInfo>

    .method public <init>()V
      aload 0
      invokespecial <BaseClassName>/<init>()V
      return
    .end method

    ...
    ```
    需要注意的是，如果在源代码中没有显式声明父类，那么隐含的父类为`java/lang/Object`。程序未支持自定义的构造方法，因此构造方法按照固定的模式生成。下文就是用户声明的实例方法的相关指令了。

## 字节码生成

到上个步骤为止，得到了文本形式表示的指令，但这还不能用于JVM使之运行，还需要将这些指令转换成“二进制版”。这一步骤我们用到了开源界的一些工作。

> Jasmin is an assembler for the Java Virtual Machine. It takes ASCII descriptions of Java classes, written in a simple assembler-like syntax using the Java Virtual Machine instruction set. It converts them into binary Java class files, suitable for loading by a Java runtime system.

按照官方对其的定义，这是一个“汇编器”，能将已经生成的文件变成JVM真正可用的字节码。

## 实例分析

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