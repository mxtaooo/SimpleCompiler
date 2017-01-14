# Bad Things

## ~~In Parser~~

### 问题描述

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

### 可能的解决思路

参考`InputStream`中`mark`/`reset`做法，在某处做一标记然后必要的时候能跳回去。

未参考实际实现，猜想是在`mark`的位置开始，记录下后继所有的字节，当`reset`时，优先输出已记录的字节，内部记录输出完毕之后再输出

### 解决方式

如果要给`Lexer`类实现`mark`/`reset`，那么`Parser`将总是要求`Lexer`提前开始`mark`，这样的话引入太多副作用，对于当前Token过于频繁的存储/删除对性能影响会比较大。

最终结果是给`Parser`实现`mark`/`reset`，对于某些Token的存储/回溯/删除自行决定，`Lexer`只负责流式给出下一个Token。