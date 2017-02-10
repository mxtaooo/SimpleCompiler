# Implementation for `SomeThinking.md`

要实现我们在 `interesting things` 里面提到的各种花式语法糖，目前的重中之重是扩展文法定义，使我们的程序支持下面几种形式。

```csharp
this.field =  10;
obj.field = this.SomeMethod();
SomeMethod();
new SomeClass().SomeMethod();
```

情况很显然了，我们需要扩展的文法定义不多，

```text
Exp -> Exp.Id   // 新增文法，用于支持显式字段访问
    -> Id(FormalList) // 新增文法，用于支持隐式本类方法调用
    -> Exp.Id(FormalList) // 原有文法，用于支持显式方法调用
    -> Id // 原有文法，用于支持本类字段/变量/参数

Stm -> Exp; // 新增文法
```

这样的话似乎我们又可以考虑加入`void`返回类型了，但是这样要修改的地方太多了