int f(float a, float b, int z);


int main()
{

    if (f(1.0, 2, 0)==0) return 1;
    if (f(1.0, 1, 0)==1) return 1;
    if (f(1.0, 0, 0)==1) return 1;
    if (f(-1.2, 0, 0)==0) return 1;
    if (f(1.0, 5.0, 0)==0) return 1;
    if (f(0.5, 1, 1)==0) return 1;
    if (f(2.5, 0, 1)==1) return 1;
    
    return 0;
}