package context;

import symbols.*;
import java.util.BitSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.SortedMap;
import java.util.Stack;
import java.util.TreeMap;

import static java.util.Map.entry;

import java.util.ArrayList;  


class RegisterManager
{
    private int paremeterizationMode = 0; // 0 = no parameterization, 1 = parameterizing

    private SortedMap<String, BitSet> tmpRegs = new TreeMap<>(Map.ofEntries 
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
    ));


    private SortedMap<String, BitSet> argRegs = new TreeMap<>(Map.ofEntries
    (
        entry("a0", new BitSet(2)),
        entry("a1", new BitSet(2)),
        entry("a2", new BitSet(2)),
        entry("a3", new BitSet(2)),
        entry("a4", new BitSet(2)),
        entry("a5", new BitSet(2)),
        entry("a6", new BitSet(2)),
        entry("a7", new BitSet(2))
    ));


    private SortedMap<String, BitSet> savedRegs = new TreeMap<>(Map.ofEntries
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
    ));

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


    public void setParameterizing()
    {
        this.paremeterizationMode = 1;
    }

    public void setNonParameterizing()
    {
        this.paremeterizationMode = 0;
    }

    public boolean isParameterizing()
    {
        return this.paremeterizationMode == 1;
    }

    public void clearAllRegisters()
    {
        for (Map.Entry<String, BitSet> entry : tmpRegs.entrySet())
        {
            entry.getValue().set(0, false);
            entry.getValue().set(1, false);
        }

        for (Map.Entry<String, BitSet> entry : argRegs.entrySet())
        {
            entry.getValue().set(0, false);
            entry.getValue().set(1, false);
        }

        for (Map.Entry<String, BitSet> entry : savedRegs.entrySet())
        {
            entry.getValue().set(0, false);
            entry.getValue().set(1, false);
        }
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
    private Stack <Function> _functionScopeStack = new Stack<Function>(); // stack of functions
    private Map <String, Function> _FunctionSymbolTable = new HashMap<String, Function>(); // symbol table
    private Map <String, CommonSymbol> _GlobalSymbolTable = new HashMap<String, CommonSymbol>(); // symbol table
    private RegisterManager regManager = new RegisterManager();
    private Stack <String> registerStack = new Stack<>();
    private String declarationMode = "";
    private Queue <CommonSymbol> _inittializerQueue = new LinkedList<CommonSymbol>();
    private Stack <Scope> _scopeStack = new Stack<Scope>(); // stack of scopes

    public void allocateMemory(int allocateMemory, String fid) // allocates memory to the stack for the current FunctionContext
    {
        Function currentFunction = _FunctionSymbolTable.get(fid); 
        int functionStackSize = currentFunction.getStackSize();
        int freeBytes = currentFunction.getFreeBytes();
        int requiredMemory = allocateMemory - freeBytes; // required memory = freeBytes - allocateMemory

        if (requiredMemory >= 0) // if we need more memory
        {
            int memoryToAdd = (16 - (requiredMemory % 16)) + requiredMemory;
            int newFreeBytes = memoryToAdd - requiredMemory;
            currentFunction.setFreeBytes(newFreeBytes);    
            currentFunction.setStackSize(functionStackSize + memoryToAdd);
        }
        else // if we dont need more memory
        {
            currentFunction.setFreeBytes(freeBytes - allocateMemory);
        }
    }

    public void addScope(String id, String type, boolean isFunction)
    {
        if (isFunction)
        {
            Function function = new Function(id, type);
            _FunctionSymbolTable.put(id, function);
            _GlobalSymbolTable.put(id, function);
            _functionScopeStack.push(function); // set current function context
            _scopeStack.push(function); // push scope to stack
            return;
        }
        Scope scope = new Scope();
        _scopeStack.push(scope); // push scope to stack
    }

    public int getFunctionStackSize(String id)
    {
        return this._FunctionSymbolTable.get(id).getStackSize();
    }

    public Function getFunction(String id)
    {
        return _FunctionSymbolTable.get(id);
    }

    public CommonSymbol getGlobalSymbol(String id)
    {
        return _GlobalSymbolTable.get(id);
    }

    public ArrayList<CommonSymbol> getFunctionParameters(String id)
    {
        return _FunctionSymbolTable.get(id).getParameters();
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

    public Function getCurrentFunction()
    {
        return _functionScopeStack.peek();
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

    public void setParameterizing()
    {
        this.regManager.setParameterizing();
    }

    public void setNonParameterizing()
    {
        this.regManager.setNonParameterizing();
    }

    public boolean isParameterizing()
    {
        return this.regManager.isParameterizing();
    }

    public void clearAllRegisters()
    {
        this.regManager.clearAllRegisters();
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
