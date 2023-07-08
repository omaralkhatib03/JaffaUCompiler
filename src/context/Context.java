package context;


import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

class CommonSymbol
{
    protected String _id;
    protected String _type;

    public CommonSymbol(String id, String type)
    {
        this._id = id;
        this._type = type;
    }

    public String getId()
    {
        return this._id;
    }

}

class Scope extends CommonSymbol
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


public class Context 
{
    // publix
    public Map <String, String> headerStringsMap = new HashMap<String, String>(); // ArrayList: {headerString, offset, freeBytes}
    public Map <String, String> bodyStringsMap = new HashMap<String, String>(); // body strings for compiled code


    // privates
    private Stack<String> _functionScopeStack = new Stack<String>(); // stack of function names
    private Map <String, Integer> _functionFreeBytes = new HashMap<String, Integer>();
    private Map <String, Integer> _functionOffsets = new HashMap<String, Integer>();
    private Map <String, Scope> _FunctionSymbolTable = new HashMap<String, Scope>(); // symbol table
    private Map <String, CommonSymbol> _GlobalSymbolTable = new HashMap<String, CommonSymbol>(); // symbol table

    public void allocateMemory(int allocateMemory) // allocates memory to the stack for the current FunctionContext
    {
        String currentFunction = _functionScopeStack.peek();
        int functionOffset = _functionOffsets.get(currentFunction);
        int freeBytes = _functionFreeBytes.get(currentFunction);
        int requiredMemory = freeBytes - allocateMemory; // required memory = freeBytes - allocateMemory

        if (requiredMemory >= 0) // if we need more memory
        {
            int memoryToAdd = (requiredMemory % 16 == 0) ? (requiredMemory % 16) + requiredMemory + 16 : (requiredMemory % 16) + requiredMemory; // memoryToAdd = (requiredMemory % 16) + requiredMemory // rounds up to nearest larger multiple of 16
            int newFreeBytes = (requiredMemory % 16 == 0) ? 16 : (requiredMemory % 16); // newFreeBytes = freeBytes - memoryToSet
            setFunctionFreeBytes(currentFunction, newFreeBytes); // set new free bytes
            setFunctionOffset(currentFunction, functionOffset + memoryToAdd); // set new offset
        }
        else // if we dont need more memory
        {
            setFunctionFreeBytes(currentFunction, freeBytes - allocateMemory); // set new free bytes
        }
    }


    private void setFunctionFreeBytes( String id, int freeBytes )
    {
        _functionFreeBytes.put(id, freeBytes);
    }

    private void setFunctionOffset( String id, int offset )
    {
        _functionOffsets.put(id, offset);
    }

    public void addScope(String id, String type, boolean isFunction)
    {
        Scope scope = new Scope(id, type, isFunction);
        _FunctionSymbolTable.put(id, scope);
        _GlobalSymbolTable.put(id, scope);
        _functionFreeBytes.put(id, 0);
        _functionOffsets.put(id, 16); 
        _functionScopeStack.push(id); // set current function context
    }

    public Scope getFunction(String id)
    {
        return _FunctionSymbolTable.get(id);
    }

    public CommonSymbol getGlobalSymbol(String id)
    {
        return _GlobalSymbolTable.get(id);
    }

    public int getFunctionOffset(String id)
    {
        return _functionOffsets.get(id);
    }


    //////////////////////////////////////////////////
    ////////////////     Printers     ////////////////
    //////////////////////////////////////////////////

    public void printFunctionSymbolTable()
    {
        for (String i : _FunctionSymbolTable.keySet()) 
        {
            System.out.printf("Function Name: %s\n", i);
        }
    }

}
