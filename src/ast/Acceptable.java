package ast;

/**
 * Created by Mengxu on 2017/1/7.
 */
public interface Acceptable
{
    void accept(Visitor v);
}
