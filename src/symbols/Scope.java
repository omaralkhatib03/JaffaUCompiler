package symbols;

import java.util.Map;
import java.util.HashMap;

public class Scope extends CommonSymbol
{
    public Map <String, CommonSymbol> symbolTable = new HashMap<>();
; // symbol table for scope
    
    public Scope()
    {}


    public Scope(String id)
    {
        this._id = id;
    } 

    public Scope(String id, String type, boolean isGlobal)
    {
        super(id, type, isGlobal);
    }


    public void addSymbol(CommonSymbol symbol)
    {
        this.symbolTable.put(symbol.getId(), symbol);
    }

    public CommonSymbol getSymbol(String id)
    {
        return this.symbolTable.get(id);
    }

    public void printSymbolTable()
    {
        System.out.println("Symbol table for scope " + ((this instanceof Function) ? ((Function) this).getId() : " ") + ":");
        for (Map.Entry<String, CommonSymbol> entry : this.symbolTable.entrySet())
        {
            System.out.println(entry.getValue().getId() + " " + entry.getValue().getType() + " " + entry.getValue().getOffset());
        }
    }


}