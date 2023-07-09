package context;


import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import static java.util.Map.entry;  
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


class RegisterManager
{
    private Map<String, BitSet> tmpRegs = Map.ofEntries
    (
        entry("t0", new BitSet(2)),
        entry("t1", new BitSet(2)),
        entry("t2", new BitSet(2)),
        entry("t3", new BitSet(2)),
        entry("t4", new BitSet(2)),
        entry("t5", new BitSet(2)),
        entry("t6", new BitSet(2)),
        entry("t7", new BitSet(1)), // t7 to t11 only exist for float registers
        entry("t8", new BitSet(1)),
        entry("t9", new BitSet(1)),
        entry("t10", new BitSet(1)),
        entry("t11", new BitSet(1))
    );


    private Map<String, BitSet> argRegs = Map.ofEntries
    (
        entry("a0", new BitSet(2)),
        entry("a1", new BitSet(2)),
        entry("a2", new BitSet(2)),
        entry("a3", new BitSet(2)),
        entry("a4", new BitSet(2)),
        entry("a5", new BitSet(2)),
        entry("a6", new BitSet(2)),
        entry("a7", new BitSet(2))
    );


    private Map<String, BitSet> savedRegs = Map.ofEntries
    (
        entry("s1", new BitSet(2)),
        entry("s2", new BitSet(2)),
        entry("s3", new BitSet(2)),
        entry("s4", new BitSet(2)),
        entry("s5", new BitSet(2)),
        entry("s6", new BitSet(2)),
        entry("s7", new BitSet(2)),
        entry("s8", new BitSet(2)),
        entry("s9", new BitSet(2)),
        entry("s10", new BitSet(2)),
        entry("s11", new BitSet(2))
    );

    public String getZero()
    {
        return "zero";
    }

    public String getReturn()
    {
        return "ra";
    }

    public String getStackPointer()
    {
        return "sp";
    }

    public String getFramePointer()
    {
        return "s0";
    }

    public Map<String, BitSet> getMap(String rgType)
    {
        if (rgType == "t")
            return this.tmpRegs;
        else if (rgType == "a")
            return this.argRegs;
        else if (rgType == "s")
            return this.savedRegs;
        else
            return null;
    }

    public int getMapIndex(String symbolType)
    {
        if (symbolType == "int" || symbolType == "unsigned" || symbolType == "char")
            return 0;
        else if (symbolType == "float" || symbolType == "double")
            return 1;
        else
            return -1;
    }


    public void setUsed(String reg)
    {
        int index = (reg.charAt(0) == 'f') ? 1 : 0;
        if (index == 1)
            reg = reg.substring(1);
        
        Map<String, BitSet> rgMap = getMap(reg.substring(0, 1));
        if (rgMap == null)
            throw new IllegalArgumentException("Invalid register type");
        
        rgMap.get(reg).set(index);
    }

    public String getReg(String rgType, String symbolType)
    {
        Map<String, BitSet> rgMap = getMap(rgType);
        if (rgMap == null)
            throw new IllegalArgumentException("Invalid register type");

        int index = getMapIndex(symbolType);        
        if (index == -1)
            throw new IllegalArgumentException("Invalid symbol type");
        
        for (Map.Entry<String, BitSet> entry : rgMap.entrySet())
        {
            if (rgType == "t" && (index == 0) && (Integer.parseInt(String.valueOf(entry.getKey().charAt(1))) > 6 || entry.getKey().length() == 3))
            {
                continue;
            }

            if (!entry.getValue().get(index))
            {   
                entry.getValue().set(index);
                return (index == 1) ? "f"+entry.getKey() : entry.getKey();
            }
        }

        return "-1"; // no avialable registers
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
