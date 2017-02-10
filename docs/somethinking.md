# Some Thinking about the compiler

这里主要是记录在编译器实现过程中的一些思考，无论本编译器的选择是好是坏，在此有所
体现。

该文章目录如下：

[BadThings](#bad_things)

+ [In Language Grammar](#in_language_grammar)

+ [In Parser](#in_parser)

+ [In Semantic](#in_semantic)

+ [In Code Generation](#in_code_generation)

+ [Method Overload](#method_overload)

[Interesting Things](#interesting_things)

+ [Currying](#currying)

+ [Closure](#closure)

+ [Lambda Expression](#lambda_expression)

+ [Local Method](#local_method)

+ [Type Inference](@type_inference)

+ [Overload Resolution](#overload_resolution)

## <a id="bad_things">Bad Things</a>

### <a id="in_language_grammar">In Language Grammar</a>

#### 问题描述

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

#### 一些思考

要搞定这个问题，显然，要重新思考方法的文法定义。我们之前对于方法的定义过于武断，要实现以上代码所示的功能，我们不应当把`return Exp;`抽离出来，虽然这样做其实大大简化了后面几个模块。我们应该将之当作某个类型的语句(即，应当有`Statement -> return Exp;`)

好的，假如我们把它的定义修改成这样，那么考虑对我们这个程序的影响。

对词法分析来说，实际上没有任何影响。

接下来便是语法分析阶段，我们需要将之输出成特定的`Stm.Return`类型的对象，由于`return`是一个关键字，因此这里其实是比较简单的。但是我们应当注意将之加入到该方法的`ReturnStms`集合中，用于接下来的语义分析阶段。

语义分析，这个阶段是最难的，也是影响最大的。源程序如果能通过这个阶段，就说明接受的源程序没有任何错误，能成功编译成可执行程序了，这个阶段容不得半点差错。首先应当对所有返回语句的表达式部分进行类型检查，这个倒是与目前没有太大差别，然后检查所有返回语句实际返回的类型是否与方法声明是否匹配，然后要检查是否存在无返回的情况，此外还要检查是否存在提前返回，导致有代码不会被执行到等等情况。

优化，简单优化不会影响，复杂优化暂不考虑。

代码生成，对该阶段无影响。

这样整理似乎并没有太棘手，但应当注意到，这里涉及到了对于全局都在用的AST的结构修改，而且是比较大的修改，由于我们之前是严格按照文法在做，因此返回语句的表达式直接定义在了方法层级里，要将之修改到语句层级里。棘手程度可想而知，这也体现了设计合理的重要性。

### <a id="in_parser">~~In Parser~~</a>

#### 问题描述

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

#### 可能的解决思路

参考`InputStream`中`mark`/`reset`做法，在某处做一标记然后必要的时候能跳回去。

未参考实际实现，猜想是在`mark`的位置开始，记录下后继所有的字节，当`reset`时，优先输出已记录的字节，内部记录输出完毕之后再输出

#### 解决方式

如果要给`Lexer`类实现`mark`/`reset`，那么`Parser`将总是要求`Lexer`提前开始`mark`，这样的话引入太多副作用，对于当前Token过于频繁的存储/删除对性能影响会比较大。

最终结果是给`Parser`实现`mark`/`reset`，对于某些Token的存储/回溯/删除自行决定，`Lexer`只负责流式给出下一个Token。

### <a id="in_semantic">In Semantic</a>

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

#### 最终决定

观察 `C#`/`Java` 这两个主流语言的主要编译器的行为如下

| 对字段的赋值情况 | C# (Roslyn) | Java (javac.exe) |
|:---:|:---:|:---:|
| 对字段无赋值行为 | 给出警告 | 无任何提示|
| 对字段有赋值行为，未提前调用 | 无任何提示 | 无任何提示 |
| 对字段赋值行为取决于外界 | 无任何提示 | 无任何提示 |

对于 `C#` 未测试其旧版编译器 `csc.exe`

看来这个问题确实无解，或者说，由于处在“外界”，有很多方式能对其产生副作用，编译器根本没办法、没有足够可靠的信息确定每个字段的赋值状态，只有把程序运行一下才能确定，但那是运行时的事情了，编译期根本无从下手。这就取决于程序员自己的把握，编译器并没有那么强的能力，只能尽可能的做检查，给程序员尽可能多的提示，作为检查/修改/重构代码的依据。


### <a id="in_code_generation">In Code Generation</a>

Java Virtual Machine Specification - 2.3.4 The `boolean` type
> Although the Java Virtual Machine defines a boolean type, it only provides
very limited support for it. There are no Java Virtual Machine instructions solely
dedicated to operations on boolean values. Instead, expressions in the Java
programming language that operate on boolean values are compiled to use values
of the Java Virtual Machine int data type.

我们这个程序加入了`boolean`这一类型，然后强调了它与`int`类型之间的区别，编译器拒绝了双方任何的直接互操作，但是到了代码生成阶段，两个又变成了同一个类型`int`。虽然这里放在了badthings里，但是个人认为这并不是个错误的决定，看上去绕了一个大圈子，但是这样对程序的安全合法做了最大的保证，类型检查能通过的，就一定是合法的。如果允许`boolean`类型和`int`类型之间的互操作，就要引入一些的前提/隐式声明/约定之类的东西，增加的语言复杂度，对程序安全也带来了隐患。

### <a id="method_overload">Method Overload</a>

我们的程序支持了的方法/字段的同名覆盖，但是这样的话我们应当给出相应的提示/警告，告知用户

此外，同时应该修改方法的签名方式，用于支持之后要扩展出的方法重载


## <a id="interesting_things">Interesting Things</a>

如下所述是发觉很有意思而且添加难度不是特别特别大的东西

### <a id="currying">Currying</a>

当前业界比较潮流的是各种函数式编程，因此关注到了很多函数式的，有意思的特性，Curring便是其中之一

按照函数式定义，一个**数学函数**接受**一个参数**，返回**一个值**，那么当有一个函数，要接受多个参数的时候要怎么做？这就要Currying了。任意一个多参数的函数，都可以被重写成一个单参数的函数串。举例如下：

```fsharp
// F# (Functional programming language)
// declare a method, its name is func, accept two parameters "x" and "y", return the sum of them
let func x y = x + y;
// or in lambda expression way
// "fun" is just a keyword, it declares that the expression after it is a lambda expression
let func = fun x y -> x + y;
```

当这段代码被编译的时候，就会被自动重写成这种形式

```fsharp
let func = fun x -> fun y -> x + y;
```

在函数式程序里面只有值的概念，这个值主要分两类，值和函数值。被重写后， `func` 变成了接受一个参数 `x` ，返回一个函数，返回的函数接受一个参数 `y` ，把 `x + y` 的结果作为返回值。

当然在本程序中，我们完全不考虑应用Currying，因为Currying最大的用武之处在于函数的分步应用（即调用函数时，并不是必须一次性给出所有参数的值，而是每次只给出部分参数值，需要最终求值时给出剩余的全部，其优势需单独讨论）。

我们面向的目标平台是JVM，如果要发挥Currying的全部优势，我们需要调用`java.util.funciton`包内直接支持的`Funciton<T, R>` `BiFunction<T, U, R>`等各个函数式编程接口，这个的前提是完整的泛型支持、匿名类对象、接口及其类对其的实现，步子太大。如果只是编译器做支持，内部转换成对泛型、接口的应用，那么编译器需要做强度很大的类型推断，比如有可能推断出`Funciton<Integer, Function<Integer,Function<Integer, Integer>>>` 这样超长嵌套的类型名，而这，仅仅是个3参数的方法而已，这个可以视时间是否足够再考虑支持。

我们提到Currying的主要目的是，考虑支持在Currying过程中很好用的Lambda表达式，~~以及刻意Curry化的表达式写法~~。

#### 修补

刻意Curry化的表达式写法这不应该是程序员的工作，这是为了数学统一化而让编译器自动完成的工作，而不应当让程序员来迎合编译器而去做这项工作。尤其是这个工作编译器不是不能自行完成，因此添加这个工作实在是，画蛇添足。

### <a id="closure">Closure</a>

闭包的相关知识前提，具体可参考下文Local Method后方的相关介绍。

### <a id="lambda_expression">Lambda Expression</a>

对我们这个程序而言，这个行为是在添加语法糖。

首先观察主流的面向对象语言中Lambda表达式的使用情况。

```csharp
// C#: lambda expression compile to an anonymous method, then assign to a delegate
// the type of the func is Func<int, int, int>
// implicit type declaration
var func = (x, y) => x + y;

// usage of the "func"
int result1 = func.Invoke(10, 20);
var result2 = func(10, 20);


// C#: lambda expression as the method body
// explicit type declaration
int func(int x, int y) => x + y;

// C#: method declaration
// explicit type declaration
int func(int x, int y)
{
    return x + y;
}

// usage the two methods above,
int result3 = func(10, 20);
int result4 = this.func(10, 20); // because we don't declare that they are static methods
```

```java
// java: lambda expression compile to an anonymous class object
// this class impliments the interface BiFunction<Integer, Integer, Integer>
// In java, the type int is primary type, not a class extends object, so we must use its wrapped class
BiFunction<Integer, Integer, Integer> func = (x, y) -> x + y;

// usage of the func, the parameters are boxed automatically
int result = func.accept(10, 20);

// java: method declaration
int func(int x, int y)
{
    return x + y;
}
```

通过以上这两段简单的程序，很明显，至少对于类型声明来说，C#中写法是很优雅很清晰的，Java中的写法，非常啰嗦，这里体现了静态类型推断的工作成果。此外C#中还扩展了Lambda表达式的使用，简单函数体可以用Lambda表达式简化。

我们计划为本程序扩展Lambda表达式支持,用于方法声明

```csharp

// original method declaration way
int Func(int x, int y)
{
    x = x + 1;
    y = y + 1;
    return x + y;
}

// case 1
// lambda expression is the method
int Func = (int x, int y) =>
{
    x = x + 1;
    y = y + 1;
    return x + y;
}

// case 2
// if only return expression, we simplify it
int Func(int x, int y) => x + y;

// case 3
// we simplify the type declaration
int Func = (x, y) =>
{
    x = x + 1;
    y = y + 1;
    return x + y;
}

// special case
// the type of parameter "y" need declaration?
//int Func = (x, SomeClass y) => x + y.field;

// case 4
// if there is only one parameter, the parens can be simplified
int Func = x =>
{
    x = x + 1;
    return x;
}

// case 5
// this is a simplest method
int Func = x => x + 1;
```

以上几个表达式，最终都将编译成一个普通的实例方法，但是我们应当注意到，没有类型声明的地方，我们需要进行类型推断，如下所述

### <a id="local_method">Local Method</a>

考虑添加本地方法的语法扩展，编译结果为实例方法

关于本地方法，考虑是否添加捕获外部变量的语法扩展，如果确认添加该扩展，内部应转换成内部类的实质，或者是匿名类的概念，之后为之生成单独的完善定义。

那么此处我们先给出两处例子作为考虑的状况

```csharp
// local method whitout catching variables
int Compute(int x)
{
    // local method normal way
    bool IsPositive(int num)
    {
        return num > 0;
    }
    // in lambda way
    bool IsPositive_lambda = num => num > 0?

    if(IsPositive(x))
        return x;
    else return -x;
}
```

关于以上这种本地方法，我们最终可以编译成为当前类的一个实例/静态方法，然后在内部转变成为对于实例/静态方法的调用。

而它的编译后的“去糖化”表示将是

```csharp
int Compute(int x)
{
    if ($local_IsPositive(x))
        return x;
    else return -1;
}

// this method can be static
// the method name contains special character to avoid confilicting with user's method name
private (static) bool $local_IsPositive(int num)
{
    return num > 0?
}
```

但是我们也可以允许该本地方法捕获本地变量，当然这样也扩展了上文Lambda表达式的使用场景。例如我们给出这样一个例子

```csharp
class MainClass
{
    int Compute(int[] nums, int index)
    {
        // local method in normal way
        bool IsOutOfBound(int _index)
        {
            return _index >= nums.Length;
        }
        // in lambda way
        bool IsOutOfBound_lambda = _index => _index >= nums.Length;

        if (IsOutOfBound(index))
            return 0;
        else return nums[index];
    }
}
```

仔细观察以下发现，本地方法中间出现了一个我们没有声明过的变量`nums`，而且很显然，它必定是一个数组类型的。这个变量从哪里来？显然是在外部捕获的，那么该如何捕获？这似乎是一个问题。

那么我们给出一种可能的编译后的“去糖化”结果，从而进行分析

```csharp
class MainClass
{
    int Compute(int[] nums, int index)
    {
        $InternalCalss obj = new $InternalCalss();
        obj.nums = nums;
        if (obj.IsOutOfBound(index))
            return 0;
        else return nums[index];
    }
}

internal class $InternalCalss
{
    public int[] nums;
    bool IsOutOfBound(int _index)
    {
        return _index >= nums.Length;
    }
}
```

很明显了，这个语法去糖化之后，转变成了对于外部类实例实例方法调用。其中，“带糖”版本的本地方法和其捕获的变量，构成了一个“Closure(闭包)”。

#### 一些思考

这里我们应当注意到，要给当前程序添加其他对象字段的访问能力，扩充文法：`assign -> obj.field = exp;`

### <a id="type_inference">Type Inference</a>

类型推断，说起来其实很简单，从已知推定未知。

目前见过做的最强的类型推断，是F#(F# 4.0)的超强的类型推断，支持任意方向的类型推断，程序里所有的类型信息都拿来用于推断未知。但是由于代码是完全不同的风格，此处暂且搁置不讨论。

下面从两个相似的例子来讨论C#(C# 6)和Java(Java 8)中的类型推断。

```csharp
class Person
{
    public string Name {get; private set;}
    public int Age {get; private set;}
    public double Score {get; set;}

    public Person(string name, int age, int score = 0)
    {
        this.Name = name;
        this.Age = age;
        this.Score = score;
    }
}
class MainClass
{
    public static void Main(string[] args)
    {
        // Define a set of Person and fill it with really instances
        var persons = new List<Person>(); // the type of persons is List<Person>

        // Style 1:-------------------
        // We want to select the person's name and score, util age is little than 10
        // and print them
        // Linq Expression style
        // the type of List is IEnumerable<'a>
        // 'a is auto created, when compile ends, it will have a real name
        var list = from p in persons // the type of p is Person
                   where p.Age > 10
                   select new { p.Name, p.Score}; // the type of new {} is anonymous type 'a
                                                  // 'a has two properties named Name, Score
        // Now we print the results
        foreach(var item in list) // the type of item is a', same with above
        {
            // every item has two properties as we said above
            Console.WriteLine("{item.Name}:{item.Score}");
        }
        // or in this way
        list.ForEach(item => Console.WriteLine("{item.Name}:{item.Score}"));

        // Style 2:-------------------
        // we don't need any temp variable
        (from p in persons
         where p.Age > 10
         select new {p.Name, p.Score})
         .ForEach(p => Console.WriteLine("{p.Name}:{p.Score}"));

        // Style 3:-------------------
        // Now let's rewrite the code above in one line style
        // cast the Linq Expression into extension method
        // it is same with the linq way
        persons.Where(p => p.Age > 10)
               .Select(p => new { p.Name, p.Score})
               .ForEach(item => Console.WriteLine("{item.Name}:{item.Score}"));
    }
}
```

```java
/*
 * class Person same with above
 */

class MainClass
{
    public static void main(String[] args)
    {
        // declare the temp type for result
        class TemClass
        {
            String name;
            double score;
            public TemClass(String name, double score)
            {
                this.name = name;
                this.score = score;
            }

            @Override
            public String toString()
            {
                return this.name + ":" +this.score;
            }
        }
        // in java 6, we should write new ArrayList<Person>()
        // but it's really type inference?
        List<Person> persons = new ArrayList<>();
        List<TemClass> list = new ArrayList();
        for(Person p : persons)
        {
            if (p.Age > 10)
                list.add(new TemClass(p.Name, p.Score)); // java hasn't anonymous type
        }
        for(Person p : list)
        {
            System.out.println(p.Name+":"+p.Score);
        }

        // in java 8, we can use stream api
        // and it can be rewritten in one line style
        persons.stream()
               .filter(p -> p.Age > 10)
               .map(p -> new TemClass(p.Name, p.Score))
               .forEach(System.out::println); // method reference
    }
}
```

以上两段代码其实都在做同一件事，C#由于特性较多因此介绍了较长篇幅，我们写了啰嗦版本的，也写了一行搞定的。我们说C#类型推断比java好一点，其主要依据是：给`var`关键字声明的变量推定类型，从赋给它的值推断类型；给LINQ中临时的匿名类型生成、推定类型(此处我们只讨论语言特性)。双方都具有的是从委托声明/目标类型/函数式接口声明推定lambda表达式参数的类型等。

当然，我们也会看到，到底C#中的匿名类型和Java内部类的`TemClass`到底有什么意义，是不是在画蛇添足？在此处当然是这样，因为我们为了尽可能展示语言特性。但是实际中，有时候我们只想序列化某个类的几个属性，然后在互联网上传输，当然我们可以通过参数指定，然后反射访问，再去决定是否对其序列化，更好的是抽取成单独的类型和对象，进行序列化，这样只反射一次就足够了，能大大提升效率。

此外，Java8中扩展出的stream api非常地functionally，流式操作，惰性求值。

### <a id="overload_resolution">Overload Resolution</a>

重载决策，首先，应当支持函数重载，再来考虑重载决策。

支持函数重载，简单来说，修改编译器内部对于方法的签名方式即可。我们目前的做法是使用方法名作为签名，只需修改此处，使用方法名和各参数类型作为签名，就能实现目前主流面向对象编程语言的重载策略。

至于具体的重载决策方式，考完再考虑………………
