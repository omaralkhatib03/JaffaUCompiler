package symbols;

import java.util.Map;
import java.util.HashMap;

public class Scope extends CommonSymbol
{
    Boolean isFunction;
    public Map <String, CommonSymbol> symbolTable = new HashMap<String, CommonSymbol>(); // symbol table for scope

    public Scope(String id, String type, boolean isFunction)
    {
        super(id, type);
        this.isFunction = isFunction;
    }

    public boolean isFunction()
    {
        return this.isFunction;
    }

}