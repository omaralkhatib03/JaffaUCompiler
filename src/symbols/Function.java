package symbols;

import java.util.ArrayList;

public class Function extends Scope 
{
    Boolean returningContext;
    public ArrayList<CommonSymbol> parameters = new ArrayList<CommonSymbol>(); // parameters for function
    int currentSymbolOffset;
    int freeBytes;
    int stackSize;

    public Function(String id, String type)
    {
        super(id, type);
        this.returningContext = false; // when a function is declared, it cannot return
        this.currentSymbolOffset = -20; // the first allowed variable is offset by -20 from s0 initially (always i think)
        this.freeBytes = 0; // ra: (12 -> 16), s0: (16 -> 20) 
        this.stackSize = 16;  
    }    

    public boolean isReturning()
    {
        return this.returningContext;
    }

    public void setReturning(boolean returningContext)
    {
        this.returningContext = returningContext;
    }

    public void addParameter(CommonSymbol parameter)
    {
        this.parameters.add(parameter);
        this.symbolTable.put(parameter.getId(), parameter);
    }

    public ArrayList<CommonSymbol> getParameters()
    {
        return this.parameters;
    }

    public int getCurrentOffset()
    {
        return this.currentSymbolOffset;
    }

    public void setcurrentSymbolOffset(int offset)
    {
        this.currentSymbolOffset = offset;
    }

    public int getStackSize()
    {
        return this.stackSize;
    }

    public void setFreeBytes(int freeBytes)
    {
        this.freeBytes = freeBytes;
    }

    public void setStackSize(int newStackSize)
    {
        this.stackSize = newStackSize;
    }

    public void decrementSymbolOffset(int size)
    {
        this.currentSymbolOffset -= size;
    }

    public int getFreeBytes()
    {
        return this.freeBytes;
    }

}


