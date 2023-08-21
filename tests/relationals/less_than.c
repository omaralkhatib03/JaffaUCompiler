int f(int x, int y, int z)
{
    int lo = 1;
    int hi = 1000;
    if (z)
    {
        return x < y;
    }
    return (lo+1) < hi;
}
