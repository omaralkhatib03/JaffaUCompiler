
double foo(int x)
{
  double y = 0;
  while (x != 1)
  {
    if (x % 2 == 0)
    {
      x = x / 2;
    }
    else
    {
      x = 3 * x + 1;
    }
    y = y + 1;
  }
  return y;
}