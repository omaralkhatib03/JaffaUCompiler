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
    return x;
}
