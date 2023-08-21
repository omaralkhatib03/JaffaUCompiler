int f(double a, float b);

int main()
{

    if (f(1.0, 2)==0) return 1;
    if (f(1.0, 1)==1) return 1;
    if (f(1.0, 0)==1) return 1;
    if (f(-1.2, 0)==0) return 1;
    if (f(1.0, 5.0)==0) return 1;
    return 0;
}