// double foo(double x, unsigned y)
// {
    // return x!=y;
// }

double foo(double x, unsigned y);


int main()
{

    if (foo(1.0, 1) != 0.0) return 1;
    if (foo(1.0, 2) != 1.0) return 1;
    if (foo(1.0, 3) != 1.0) return 1;
    if (foo(1.0, 4) != 1.0) return 1;
    if (foo(-1, -1) != 1.0) return 1;
    if (foo(-1, -2) != 1.0) return 1;
    
    return 0;
}
