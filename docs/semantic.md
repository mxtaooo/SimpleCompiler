# Semantic

语义分析器工作于语法分析器输出的抽象语法树，通过对该语法树的分析回答一个问题：源程序是否符合语义规则。如果确实存在问题，要给出尽可能准确的报错信息。语义分析中最重要的工作便是类型检查，当然，还会有一些其他的检查，例如应当声明变量再使用等。

## Type Checking

类型检查是语义分析的重点所在，如果此处的检查没有通过，那么这个程序一定不能运行。下面通过几个例子来展示类型检查的工作细节。

### Expression

对于表达式而言，类型检查主要分为以下几类情况

+ operator

    本源程序语言支持一个一元操作符 `!`，五个二元操作符 `+`/`-`/`*`/`&&`/`<`

    对这种类型的表达式，类型检查的步骤是：先确认两侧/单侧的表达式类型，然后确认两侧表达式类型是否匹配，最后确认当前的操作符能否对该类型进行操作。全部没有问题的话，才认为该表达式通过了检查，并确认该表达式的值类型。

    例如：

  + `10 + true`

    很显然两侧类型不匹配，类型检查的给出的信息是

    `Error: Line 1 Add expression: the type of left is @int, but the type of right is @boolean`

  + `true < false`

    两侧类型是一致的，但很显然，这个比较没有任何意义，因此这个表达式也是个错误

    `Error: Line 1 only integer numbers can be compared.`

  + `!200`

    只有布尔值才能取非，因此这个表达式显然也是非法的。

    `Error: Line 1 the exp cannot calculate to a boolean.`

+ method invoke

    本程序语言仅支持实例方法调用，不支持静态方法、方法重载等。

    对于方法调用表达式，类型检查主要关注形参及实参。首先确认形参和实参数量是相等的，然后确认参数的类型是一一对应的。通过检查后，表达式的值类型被设定成为该方法的返回值类型。

    假定有方法 `int Compute(int a, int b)`，对它的两种错误调用如下

  + `this.Compute(10)`

    很明显，这并不能通过第一步：对于参数个数的检查。

    `Error: Line 1 the count of arguments is not match.`

  + `this.Compute(10, false)`

    显然，第二个参数的类型不是匹配的。

    `Error: Line 32 the parameter 2 needs a int, but got a boolean`

  + <span id="printexp">`print(exp)`</span>

    应当注意到，在本程序中我们认为print是一个语句，而不是函数调用表达式。这样做的目的是为了简化该编译器开发，但其本质依旧是函数调用，因此对于它的类型检查等同函数调用。此外，本程序仅支持每行输出一个整型数字，因此exp最终应当能求得一个整型数字。

+ return expression

    这里是确认方法声明处的返回类型和实际的返回类型是匹配的。考虑这样的一个源程序：

    ```java
    boolean DoSomething(int num)
    {
        // Some VarDecls
        // Some Statements
        return 10;
    }
    ```

    很明显，实际返回类型和声明的返回类型不一致，我们应当给出相应的错误提示。

    `Error: Line 3 the return expression's type is not match the method "DoSomething" declared.`

+ identifier / literal / `this` / `new id()`

    这里是讨论剩余的几类特殊的表达式，变量/字段引用，字面量，this关键字，实例化对象表达式。

  + <span id="identifier">identifier</span>

    查找标识符大致分为两步，首先在参数列表/本地变量表查找该标识符，若查找失败，再去类/基类字段声明列表中尝试查找。若最终查找失败，则报告一个错误。

    `Error: Line 1 you should declare "x" before use it.`

  + literal / `this`

    这个种类主要包括整型数字，`true`/`false`/`this`关键字。应当特别注意`this`关键字，它指代当前类的实例，它的类型自然是该关键字所处的类的类型。

  + <span id="classnotfound">`new id()`</span>

    应当注意到本程序不支持构造函数，因此每个类只有一个形式上的无参构造器。除主类外，对于其他普通类的声明顺序不做要求。如果尝试实例化一个不存在的类，也会报告错误。

        `Error: Line 1 cannot find the declaration of class "XXX".`

### Statement

有了前面表达式级的类型检查作为基础，做语句的类型检查就很方便快捷了。

+ `print`

    [上文](#printexp)已给出解释

+ `if` / `while`

    对于这种类型的语句，只需要检查是否符合对应的规则即可。例如条件判断处必须是个布尔类型的表达式

+ `{Statement*}`

    这种类型的表达式，按顺序轮流进行检查即可

+ `id = exp;`

    赋值语句，只要等号两侧的类型是互相匹配的，那就允许赋值。

### Special Attention

应当注意到，我们之前提到的一直是“类型匹配”，而不是“类型相等”。隐含的，我们允许类型的隐式转换，这样也将部分地支持了“多态”。如果可能的话，对于类类型，我们还可以实现支持“变体(协变/向上转型)”这一概念。

## Other Checking

在本程序里，我们还做了另外两个对于标识符的检查：[变量/字段标识符](#identifier)，[类名标识符](#classnotfound)。由于这两类标识符存在于不同的符号表里面，因此本程序可以声明类似 `Class Class;` 这样类型和名称一样的变量/字段，这种声明是合法的，在使用时具体的意义取决于这个符号所在位置的语义。