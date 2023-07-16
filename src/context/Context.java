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

    private SortedMap<String, BitSet> tmpRegs;

    private SortedMap<String, BitSet> argRegs;

    private SortedMap<String, BitSet> savedRegs;

    public RegisterManager()
    {
        this.tmpRegs = new TreeMap<>(Map.ofEntries 
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

        this.argRegs = new TreeMap<>(Map.ofEntries
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


        this.savedRegs = new TreeMap<>(Map.ofEntries
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
    }

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

        return null; // no avialable registers
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

    public void printBitSet(BitSet bs)
    {
        for( int i = 0; i <= bs.length();  i++ )
        {
            System.out.printf("%d", ((bs.get(i)) ? 1 : 0));
        }
        System.out.printf("\n");
    }

    public void printRegMaps()
    {
        System.out.println("tmpRegs: ");
        for (Map.Entry<String, BitSet> entry : this.tmpRegs.entrySet())
        {
            System.out.print(entry.getKey() + ": ");
            printBitSet(entry.getValue());
        }

        System.out.println("argRegs: ");
        for (Map.Entry<String, BitSet> entry : this.argRegs.entrySet())
        {
            System.out.print(entry.getKey() + ": ");
            printBitSet(entry.getValue());
        }

        System.out.println("savedRegs: ");
        for (Map.Entry<String, BitSet> entry : this.savedRegs.entrySet())
        {
            System.out.print(entry.getKey() + ": ");
            printBitSet(entry.getValue());
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
    private Stack <String> registerStack = new Stack<String>();
    private String declarationMode = "";
    private Queue <CommonSymbol> _inittializerQueue = new LinkedList<CommonSymbol>();
    private Stack <Scope> _scopeStack = new Stack<Scope>(); // stack of scopes
    private int _makeUnq = 0;
    private Map <String, String> _returnLabels = new HashMap<String, String>(); // symbol table
    private Stack<String> _breakStack = new Stack<String>();
    private Stack<String> _continueStack = new Stack<String>();

    public void allocateMemory(int allocateMemory, String fid) // allocates memory to the stack for the current FunctionContext
    {
        Function currentFunction = _FunctionSymbolTable.get(fid); 
        int functionStackSize = currentFunction.getStackSize();
        int freeBytes = currentFunction.getFreeBytes();
        int requiredMemory = allocateMemory - freeBytes; // required memory = allocateMemory - freeBytes

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

    public void addScope(String id, String type, boolean isFunction, boolean isGlobal)
    {
        if (isFunction)
        {
            Function function = new Function(id, type, isGlobal);
            _FunctionSymbolTable.put(id, function);
            _GlobalSymbolTable.put(id, function);
            _functionScopeStack.push(function); // set current function context
            _scopeStack.push(function); // push scope to stack
            return;
        }
        Scope scope = new Scope(id);
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
        if (onStack && regOut != null)
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
        if (getTopScope().getSymbol(symbol.getId()) == null)
            getTopScope().addSymbol(symbol); // add symbol to current scope
        else
            throw new IllegalArgumentException("Symbol already exists in scope");
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
        return this._inittializerQueue.poll();
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
    

    public boolean symbolExistsInScope(String id)
    {
        
        // System.out.printf("%s\n", this._GlobalSymbolTable.get(id).getId());
        for (Scope i : this._scopeStack)
        {
            if (i.getSymbol(id) != null)
                return true;
        }
        return ((this._GlobalSymbolTable.containsKey(id)) || this._FunctionSymbolTable.containsKey(id) || this.getTopScope().getSymbol(id) != null); // does the symbol exist on the correct scope ?
    }

    @SuppressWarnings("unchecked")
    public CommonSymbol getSymbol(String id)
    {
        Stack<Scope> tmpStack = (Stack<Scope>) this._scopeStack.clone();
        
        
        while (!tmpStack.isEmpty())
        {
            Scope currentScope = tmpStack.pop();
            CommonSymbol symbol = currentScope.getSymbol(id);
            if (symbol != null)
                return symbol;
        }

        if (this._GlobalSymbolTable.containsKey(id))
            return this._GlobalSymbolTable.get(id);

        return null;
    }

    public void clearStack(boolean verbose)
    {
        while (!this.registerStack.empty())
        {
            if (verbose)
                System.out.printf("clearing: %s\n", clearTopOfStack());
            else
                clearTopOfStack();
        }
    }

    public boolean getRegStatus(String reg)
    {
        return this.regManager.getRegStatus(reg);
    }


    public String makeUnqiueLabel(String label)
    {
        return "_" + label + "_" + (this._makeUnq++);
    }

    public void setReturnLabel(String id)
    {
        this._returnLabels.put(id, makeUnqiueLabel("RETURN"));
    }

    public String getReturnLabel(String id)
    {
        return this._returnLabels.get(id);
    }

    public void removeTopScope()
    {
        this._scopeStack.pop();
    }

    public boolean isRegStackEmpty()
    {
        return this.registerStack.empty();
    }

    public void pushToBreakStack(String label)
    {
        this._breakStack.push(label);
    }

    public String getTopOfBreakStack()
    {
        return this._breakStack.peek();
    }

    public String popFromBreakStack()
    {
        return this._breakStack.pop();
    }

    public void pushToContinueStack(String label)
    {
        this._continueStack.push(label);
    }

    public String getTopOfContinueStack()
    {
        return this._continueStack.peek();
    }

    public String popFromContinueStack()
    {
        return this._continueStack.pop();
    }

    public void setFunctionType(String id, String type)
    {
        this._FunctionSymbolTable.get(id).setType(type);
    }

    public boolean isFloatReg(String reg)
    {
        return (reg.charAt(0) == 'f');
    }

    public boolean isTopOfRegStack(String reg)
    {
        return this.registerStack.peek().equals(reg);
    }

    public void pushRegisterStack(String reg)
    {
        this.registerStack.push(reg);
    }


    // public void replaceRegOnStack()
    // {
        // 
    // }
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

    @SuppressWarnings("unchecked")
    public void printRegisterStackStatus() // ONLY USE THIS AT THE END OF THE PROGRAM
    {
        System.out.printf("Register Stack Size: %s\n", this.registerStack.size());
        Stack<String> tmp = (Stack<String>) this.registerStack.clone();

        while (!tmp.empty())
        {
            System.out.printf("%s \n", tmp.pop());
        }
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

    @SuppressWarnings("unchecked")
    public void printScopeStack()
    {
        Stack<Scope> tmp = (Stack<Scope>) this._scopeStack.clone();
        while(!tmp.empty())
        {
            System.out.printf("Scope: %s\n", tmp.pop().getId());
        }
    }
}
