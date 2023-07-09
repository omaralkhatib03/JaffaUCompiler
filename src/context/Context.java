package context;

import symbols.*;
import java.util.BitSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;
import static java.util.Map.entry;  


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
        if (rgType.equals("t"))
            return this.tmpRegs;
        else if (rgType.equals("a"))
            return this.argRegs;
        else if (rgType.equals("s"))
            return this.savedRegs;
        else
            return null;
    }

    public int getMapIndex(String symbolType)
    {
        if (symbolType.equals("int") || symbolType.equals("unsigned") || symbolType.equals("char"))
            return 0;
        else if (symbolType.equals("float") || symbolType.equals("double"))
            return 1;
        else
            return -1;
    }

    public void setReg(String reg, Boolean value)
    {
        int index = (reg.charAt(0) == 'f') ? 1 : 0;
        if (index == 1)
        {
            reg = reg.substring(1);
        }
        
        Map<String, BitSet> rgMap = getMap(reg.substring(0, 1));
        
        if (rgMap == null)
            throw new IllegalArgumentException("Invalid register type");
        
        rgMap.get(reg).set(index, value);
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
                entry.getValue().set(index, true);
                return (index == 1) ? "f"+entry.getKey() : entry.getKey();
            }
        }

        return "-1"; // no avialable registers
    }

    public boolean getRegStatus(String reg)
    {
        int index = (reg.charAt(0) == 'f') ? 1 : 0;
        if (index == 1)
        {
            reg = reg.substring(1);
        }
        
        Map<String, BitSet> rgMap = getMap(reg.substring(0, 1));
        if (rgMap == null)
            throw new IllegalArgumentException("Invalid register type");
        
        return rgMap.get(reg).get(index);
    }

    ///////////////////////////////////////////////////////////////////
    ////////////////          Printer             /////////////////////
    ///////////////////////////////////////////////////////////////////


    public void printRegMaps()
    {
        System.out.println("tmpRegs: ");
        for (Map.Entry<String, BitSet> entry : tmpRegs.entrySet())
        {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }

        System.out.println("argRegs: ");
        for (Map.Entry<String, BitSet> entry : argRegs.entrySet())
        {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }

        System.out.println("savedRegs: ");
        for (Map.Entry<String, BitSet> entry : savedRegs.entrySet())
        {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }
    }

}


public class Context 
{
    // publix
    public Map <String, String> headerStringsMap = new HashMap<>(); // ArrayList: {headerString, offset, freeBytes}
    public Map <String, String> bodyStringsMap = new HashMap<>(); // body strings for compiled code


    // privates
    private Stack <String> _functionScopeStack = new Stack<String>(); // stack of function names
    private Map <String, Integer> _functionFreeBytes = new HashMap<String, Integer>();
    private Map <String, Integer> _functionOffsets = new HashMap<String, Integer>();
    private Map <String, Scope> _FunctionSymbolTable = new HashMap<String, Scope>(); // symbol table
    private Map <String, CommonSymbol> _GlobalSymbolTable = new HashMap<String, CommonSymbol>(); // symbol table
    private RegisterManager regManager = new RegisterManager();
    private Stack <String> registerStack = new Stack<>();
    private String declarationMode = "";
    private Queue <CommonSymbol> _inittializerQueue = new LinkedList<CommonSymbol>();
    private Stack <Scope> _scopeStack = new Stack<Scope>(); // stack of scopes

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
        _scopeStack.push(scope); // push scope to stack
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

    public String getReg(String regType, String symbolType, boolean onStack)
    {
        String regOut = this.regManager.getReg(regType, symbolType);
        if (onStack)
            this.registerStack.push(regOut);
        return regOut;
    }

    public void setUsed(String reg, boolean onStack)
    {
        this.regManager.setReg(reg, true);
        if (onStack)
            this.registerStack.push(reg);
    }

    public String getTopReg()
    {
        return this.registerStack.peek();
    }

    public Scope getCurrentFunction()
    {
        return _FunctionSymbolTable.get(_functionScopeStack.peek());
    }

    public void setFunctionReturn(String id, boolean isReturning)
    {
        _FunctionSymbolTable.get(id).setReturning(isReturning);
    }

    public String popRegisterStack()
    {
        return this.registerStack.pop();
    }

    public String clearReg(String reg)
    {
        this.regManager.setReg(reg, false);
        return reg;
    }

    public String clearTopOfStack()
    {
        String tmp = this.registerStack.pop();
        if (!regManager.getRegStatus(tmp))
            throw new IllegalArgumentException("Register not in use");
        this.regManager.setReg(tmp, false);
        return tmp;
    }


    public void writeBodyString(String id, String body)
    {
        this.bodyStringsMap.put(id, this.bodyStringsMap.get(id) + body);
    }


    public String getDeclarationMode()
    {
        return this.declarationMode;
    }

    public void setDeclarationMode(String mode)
    {
        this.declarationMode = mode;
    }

    public Scope getTopScope()
    {
        return this._scopeStack.peek();
    }

    public void addLocalSymbol(CommonSymbol symbol)
    {
        getTopScope().addSymbol(symbol); // add symbol to current scope
    }

    public boolean isGlobalScope()
    {
        return _scopeStack.isEmpty();
    }


    public void addInitializer(CommonSymbol symbol)
    {
        this._inittializerQueue.add(symbol);
    }

    public CommonSymbol getFrontOfInitQueue()
    {
        return this._inittializerQueue.peek();
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

    public void printGlobalSymbolTable()
    {
        for (String i : _GlobalSymbolTable.keySet()) 
        {
            System.out.printf("Global Name: %s\n", i);
        }
    }

    public void printRegMaps()
    {
        this.regManager.printRegMaps();
    }

    public void printRegisterStackStatus()
    {
        System.out.printf("Register Stack Size: %s\n", this.registerStack.size());
    }

    public void printAllSymbolTables()
    {
        System.out.println("Printing Function Symbol Table");
        printFunctionSymbolTable();
        System.out.println("Printing Global Symbol Table");
        printGlobalSymbolTable();
        for (String i : _FunctionSymbolTable.keySet()) 
        {
            System.out.printf("Printing Local Symbol Table for Function: %s\n", i);
            _FunctionSymbolTable.get(i).printSymbolTable();
        }
    }
}
