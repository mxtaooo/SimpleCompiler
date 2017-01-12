Bad Things
===

在 Parser 中
---

对于如下状况

```java
class AClass
{
    AClass field;   // 1. -> Id Id Semi
    AClass method() // 2. -> Id Id Lparen Rparen
    {
        AClass local;           // 3. -> Id Id Semi
        local = new AClass();   // 4. -> Id Assign New Id Lparen Rparen Semi

        // Some Statements
    }
}
```

对于如上所述的情况，记号流1与2，3与4，没办法马上做出区分，要到第二个甚至第三个才能区分当前行到底是在做什么，过程中还是发生了“回溯”，本编译器采用的实现非常不优雅
