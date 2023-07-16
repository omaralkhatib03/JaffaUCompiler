int f(float a, float b, int z)
{
    unsigned x = 1;
    if (z)
    {
        return a < x;
    }
    return a < b;
}