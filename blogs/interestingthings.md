# Interesting Things

如下所述是发觉很有意思而且添加难度不是特别特别大的东西

## Currying

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

### 修补

刻意Curry化的表达式写法这不应该是程序员的工作，这是为了数学统一化而让编译器自动完成的工作，而不应当让程序员来迎合编译器而去做这项工作。尤其是这个工作编译器不是不能自行完成，因此添加这个工作实在是，画蛇添足。

## Closure

闭包的相关知识前提。


## Lambda Expression

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

## Local Method

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

## Type Inference

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

## Overload Resolution

重载决策，首先，应当支持函数重载，再来考虑重载决策。

支持函数重载，简单来说，修改编译器内部对于方法的签名方式即可。我们目前的做法是使用方法名作为签名，只需修改此处，使用方法名和各参数类型作为签名，就能实现目前主流面向对象编程语言的重载策略。

至于具体的重载决策方式，考完再考虑………………
