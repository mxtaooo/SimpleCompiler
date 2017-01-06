/**
 * Created by Mengxu on 2017/1/4.
 */
public class SimpleCompiler
{
    public static void main(String[] args)
    {
        if (args.length == 0)
        {
            System.out.println("Hello, this is a simple compiler!");
            System.out.println("Please input the file name which you want to compile");
        }
        else
            System.out.println(args[0]);
    }
}
