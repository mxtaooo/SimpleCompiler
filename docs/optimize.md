# Optimization

优化是一个比较大，比较严肃，要求功底比较深的话题。

下面首先给出优化的主要分类

+ 前端优化 (局部的，流不敏感的)
  + 常量折叠
  + 代数化简
  + 不可达代码删除
+ 中期优化 (全局的，流敏感的)
  + 常量传播
  + 拷贝传播
  + 死代码删除
  + 公共子表达式删除
+ 后期优化 (汇编代码级进行)
  + 寄存器分配
  + 指令调度
  + 窥孔优化

此外有一点，我们将删除声明未使用变量，停止为其在本地变量表分配空间，在我们这个编译器里将之作为“优化”对待。

## Delete Unused Variables

删除声明未使用变量，实质来说，这个算不得什么了不得的“优化”，这么做目前能看到的唯一的收益就是节省了本地变量表的空间。

总体实现思路是通过上下文分析，扫描该变量的使用情况，对于声明了但是没有使用的本地变量，那么就删除该声明语句。并且在该过程中给出警告。

此外应当注意一点，我们也扫描了方法的参数使用情况，但是我们并没有将未使用的方法参数删除之。因为我们没有去分析该方法的调用情况，一旦此处删除了，那么可能导致后期方法调用失败。因此，对于方法参数未使用情况，我们仅仅是给出了警告。

但是应当注意如下情况。

```java
int Compute(int num, boolean state)
{
    int x;
    int total;
    total = num * 10;
    return 1;
}
```

对于以上所述的这个方法，我们目前能做到的是，删除`int x`语句，对`boolean state`声明给出警告。其实很显然，对于`total`的声明，以及下面的赋值，是能执行的，也是没错误的，但是对于整个方法来说，是没有任何意义的，因此`num`的存在也是没有意义的。
关于这个问题的讨论，我们将在下方“死代码删除”进行。

## Constant Folding

常量折叠，基本思想是：在编译期计算常量表达式的值。

例如：对于`a=3+5; b=false&&true;`，其实我们没有必要将这种表达式放到运行时求值，编译时就可以求值，然后使程序暴露更多的优化机会。比如不可达代码删除等。

基本的实现似乎是对于每个语句进行考察，对表达式部分尝试进行求值，能求值便求值，不能求值便放弃掉。

应该注意，此处我们并没有对于`a=x-4+y-5-6;`进行调整运算顺序并求值，因为这里涉及到了语义考察，应当仔细思考之后再完善。

经过对于以上的考虑，我们最终决定放弃这个折叠优化，因为存在这样的情况，如果被我们优化成`a=x+y-15`，这样就打破了隐含规定的从左到右的计算顺序，可能会造成程序行为的改变。

但是这个情况并不是完全没有优化机会，比如我们对于后方的两个常量可以进行折叠，而这样不会造成任何语义改变。

## Delete Unreachable Code

删除不可达代码，这里应当首先注意与下文的“删除死代码”做出正确区分。

所谓“不可达代码”指的是永远不会有机会执行到的代码，例如：

```java
if (true)
  // branch 1
else
  // branch 2
```

对于以上一段代码，很显然，分支2永远不会执行到，因此我们可以将其删除，而且不会引发任何副作用，这种做法是安全地。

当然，可能我们在考虑这么做的意义，到底这样的优化在实际中会有多大作用。哈，存在必有道理，此处先不考虑了。不过结合常量折叠/常量传播，应该还是有很大作用的。

## Constant Propagation and Copy Propagation

常量传播和拷贝传播，这是两个思路相似的优化方式。例如以下一段代码：

```java
int Compute(int num)
{
  int a;
  int b;
  a = num;
  b = 10;
  return a + b;
}
```

根据以上一个简单的例子，很容易就能看出来，变量`a`其实就是复制了`num`的值，变量`b`仅仅是数字10，最后的返回语句能优化成`return num + 10;`再经过死代码删除等优化，其实这个最终可以优化成一个简单的返回语句。

但是我们也要注意到，我们应当做好“到达定义分析”，不然很容易改变程序语义。例如

```java
int ConstantProp(int num)
{
  int a;
  int b;
  a = 5;
  b = num;
  // 插入类似如下两个语句
  while (a < 10) // 其实这个while语句是“死代码”，但是此处先不将其优化掉
      a = a + 1;
  if (b < 10)
      a = 10;
  else
      a = 11;

  return a + b;
}
```

此时，变量`a`的定义`5`就不能到达最终的返回语句；但是`b`的定义`num`还是可以到达的。

我们采用的方式是顺序遍历所有语句，到达定义分析和优化共同进行，根据分析得到的静态保守信息，对当前的语句进行优化。

## Delete Dead Code

死代码删除，这里我们应当注意，这里的死代码应当很严格地与上文的不可达代码区分开。

这里的死代码指的是，对于程序的执行，没有任何收益的代码，例如

```java
int SomeDeadCode(int num, boolean state)
{
  if (state)
    num = num + 10;
  else
    num = num - 10;
  return 20;
}
```

对于以上这段代码，我们很简单就能看见，`if-else`语句块是没有错误的，但是有一个问题，它的执行对于这个方法没有任何收益，也不存在任何“副作用”，因此我们可以删除这个语句。但是对于如下这个例子：

```java
int SomeDeadCode(int num, boolean state)
{
  if (state)
    this.field = num;
  else
    print(num);
  return 20;
}
```

那么对如这个情况，对于`then`分支，这个分支的语句执行影响了外部的字段的值，它产生了“副作用”，而对于`else`分支，这个似乎没用影响任何值的变化，但是它的执行能产生输出，这也是对外界产生了影响，因此我们优化掉这两种语句是不可以的，就会影响到程序的可观测行为。(当然如果`state`是个常量，某个分支就会变成不可达代码，优化掉永远不会执行到的地方是安全的。)

我们采取的方法是逆序遍历所有语句，从返回语句出发，考察每一句对于程序的收益，找出对于当前程序没有意义的代码，并删除之。

## 后期优化

关于后期优化，目前来看，那已经不再是我们的工作了(或者说我们并没有办法去做)，由于我们是面向栈式计算机"jvm"生成字节码，不涉及寄存器式计算机的相关内容，因此没必要做了。