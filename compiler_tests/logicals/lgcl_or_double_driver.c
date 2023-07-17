// float baa(float x, double y) 
// {
    // return x || y;
// }
#include <stdio.h>

double baa(double x, float y);

int main()
{
    if( (baa(0x0F,0xF0)!=1) ) 
    {
        printf("baa(0x0F,0xF0)=%f\n",baa(0x0F,0xF0));
        return 1;
    }
    if( (baa(0x00,0xF0)!=1) )
    {
        printf("baa(0x00,0xF0)=%f\n",baa(0x00,0xF0));
        return 1;
    }
    if( (baa(0x0F,0x00)!=1) )
    {
        printf("baa(0x0F,0x00)=%f\n",baa(0x0F,0x00));
        return 1;
    } 
    if( (baa(0x00,0x00)!=0) )
    {
        printf("baa(0x00,0x00)=%f\n",baa(0x00,0x00));
        return 1;    
    }
    return 0;
}

