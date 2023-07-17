// unsigned foo(float x, unsigned y)
// {
    // return x&&y;
// }

unsigned foo(float x, unsigned y);


int main()
{
    if( (foo(0x0F,0xF0)!=1) ) return 1;
    if( (foo(0x00,0xF0)!=0) ) return 1;
    if( (foo(0x0F,0x00)!=0) ) return 1;
    if( (foo(0x00,0x00)!=0) ) return 1;
    return 0;
}
