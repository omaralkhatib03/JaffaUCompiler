package symbols;

import java.util.Map;
import java.util.HashMap;

public class Scope extends CommonSymbol
{
    Boolean isFunction;
    Boolean returningContext;
    public Map <String, CommonSymbol> symbolTable = new HashMap<String, CommonSymbol>(); // symbol table for scope

    public Scope(String id, String type, boolean isFunction)
    {
        super(id, type);
        this.isFunction = isFunction;
        this.returningContext = false; // when a function is declared, it cannot return
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

}