int f();
int g();

int foo(int x)
{
    int i;
    for (i = 0; i < 5; i++)
    {
        if (i==3)
            break;
        else
            x++;
    }
    f();
    return g();
    // return 3;
}
