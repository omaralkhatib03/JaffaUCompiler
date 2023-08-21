
double foo(int x);

int main()
{
  if (foo(4) != 2) return 1;  
  if (foo(3) != 7) return 1;  
  if (foo(12) != 9) return 1;  
  if (foo(10) != 6) return 1;  
  return 0;
}