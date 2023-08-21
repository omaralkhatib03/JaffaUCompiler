double foo(float x, unsigned y);

int main()
{
    if (foo(1.2, 4) != 3.0) return 1;
    if (foo(3.4, 4) != 1.0) return 1;
    if (foo(-1.2, 3) != 3.0) return 1;
    if (foo(23, 3) != 1.0) return 1;
    
    return 0;
}
