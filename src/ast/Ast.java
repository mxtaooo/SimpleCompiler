package ast;

/**
 * Created by Mengxu on 2017/1/7.
 */
public class Ast
{
    public static class Type
    {
        public static abstract class T implements Acceptable
        {}

        public static class Boolean extends T
        {
            public Boolean(){}

            @Override
            public String toString()
            {
                return "@boolean";
            }

            @Override
            public void accept(Visitor v)
            {
                v.visit(this);
            }
        }

        public static class ClassType extends T
        {
            public String id;

            public ClassType(String id)
            {
                this.id = id;
            }

            @Override
            public String toString()
            {
                return this.id;
            }

            @Override
            public void accept(Visitor v)
            {
                v.visit(this);
            }
        }

        public static class Int extends T
        {
            public Int(){}

            @Override
            public String toString()
            {
                return "@int";
            }

            @Override
            public void accept(Visitor v)
            {
                v.visit(this);
            }
        }

    }

    public static class Dec
    {
        public static abstract class T implements ast.Acceptable
        {
            public int lineNum;
        }

        public static class DecSingle extends T
        {
            public Type.T type;
            public String id;

            public DecSingle(Type.T type, String id, int lineNum)
            {
                this.type = type;
                this.id = id;
                this.lineNum = lineNum;
            }

            @Override
            public void accept(Visitor v)
            {
                v.visit(this);
            }
        }
    }

    public static class Exp
    {
        public static abstract class T implements ast.Acceptable
        {
            public int lineNum;
        }

        public static class Add extends T
        {
            public T left, right;

            public Add(T left, T right, int lineNum)
            {
                this.left = left;
                this.right = right;
                this.lineNum = lineNum;
            }

            @Override
            public void accept(Visitor v)
            {
                v.visit(this);
            }
        }

        public static class And extends T
        {
            public T left, right;

            public And(T left, T right, int lineNum)
            {
                this.left = left;
                this.right = right;
                this.lineNum = lineNum;
            }

            @Override
            public void accept(Visitor v)
            {
                v.visit(this);
            }
        }

        public static class Call extends T
        {
            public T exp;
            public String id;
            public java.util.LinkedList<T> args;
            public String type; // type of first field "exp"
            public java.util.LinkedList<Type.T> at; // arg's type

            public Call(T exp, String id, java.util.LinkedList<T> args, int lineNum)
            {
                this.exp = exp;
                this.id = id;
                this.args = args;
                this.type = null;
                this.lineNum = lineNum;
            }

            @Override
            public void accept(Visitor v)
            {
                v.visit(this);
            }
        }

        public static class False extends T
        {
            public False(int lineNum)
            {
                this.lineNum = lineNum;
            }

            @Override
            public void accept(Visitor v)
            {
                v.visit(this);
            }
        }

        public static class Id extends T
        {
            public String id; // name of the id
            public Type.T type; // type of the id
            public boolean isField; // whether or not a field

            public Id(String id, int lineNum)
            {
                this.id = id;
                this.lineNum = lineNum;
            }

            public Id(String id, Type.T type, boolean isField, int lineNum)
            {
                this.id = id;
                this.type = type;
                this.isField = isField;
                this.lineNum = lineNum;
            }

            @Override
            public void accept(Visitor v)
            {
                v.visit(this);
            }
        }

        public static class LT extends T
        {
            public T left, right;

            public LT(T left, T right, int lineNum)
            {
                this.left = left;
                this.right = right;
                this.lineNum = lineNum;
            }

            @Override
            public void accept(Visitor v)
            {
                v.visit(this);
            }
        }
        public static class NewObject extends T
        {
            public String id;

            public NewObject(String id, int lineNum)
            {
                this.id = id;
                this.lineNum = lineNum;
            }

            @Override
            public void accept(Visitor v)
            {
                v.visit(this);
            }
        }

    }
}
