# SimpleCompiler

A simple compiler, generate bytecode for JVM

+ 选取 Java 语言的子集，支持简单的面向对象
+ 词法/语法分析手动实现，未借助现有工具
+ 面向 JVM 生成字节码
+ 已实现的编译优化：简易的常量折叠、不可达代码删除，基于到达定义分析的常量/拷贝传播，基于活性分析的死代码删除优化