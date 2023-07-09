package symbols;

import java.util.Map;
import java.util.HashMap;

public class Scope extends CommonSymbol
{
    Boolean isFunction;
    Boolean returningContext;
    public Map <String, CommonSymbol> symbolTable = new HashMap<String, CommonSymbol>(); // symbol table for scope
    int currentOffset;


    public Scope(String id, String type, boolean isFunction)
    {
        super(id, type);
        this.isFunction = isFunction;
        this.returningContext = false; // when a function is declared, it cannot return
        this.currentOffset = -20; // the first allowed variable is offset by -20 from s0 initially (always i think)
    }

    public boolean isFunction()
    {
        return this.isFunction;
    }

    public boolean isReturning()
    {
        return this.returningContext;
    }

    public void setReturning(boolean returningContext)
    {
        this.returningContext = returningContext;
    }

    public void addSymbol(CommonSymbol symbol)
    {
        this.symbolTable.put(symbol.getId(), symbol);
    }

    public CommonSymbol getSymbol(String id)
    {
        return this.symbolTable.get(id);
    }

    public int getCurrentOffset()
    {
        return this.currentOffset;
    }

    public void setCurrentOffset(int offset)
    {
        this.currentOffset = offset;
    }

    public void decrementOffset(int offset)
    {
        this.currentOffset -= offset;
    }

    public void printSymbolTable()
    {
        System.out.println("Symbol table for scope " + this.getId() + ":");
        for (Map.Entry<String, CommonSymbol> entry : this.symbolTable.entrySet())
        {
            System.out.println(entry.getValue().getId() + " " + entry.getValue().getType() + " " + entry.getValue().getOffset());
        }
    }

}