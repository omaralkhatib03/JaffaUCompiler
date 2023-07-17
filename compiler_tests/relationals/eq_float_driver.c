// float foo(float x, char y)
// {
    // return x==y;
// }


float foo(float x, char y);


int main()
{
    if (foo(1.0, 1) != 1.0) return 1;
    if (foo(1.0, 2) != 0.0) return 1;
    if (foo(1.0, 3) != 0.0) return 1;
    return 0;
}
