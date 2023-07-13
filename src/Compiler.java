import context.*;
import grammar.*;
import grammar.cLexer;
import grammar.cParser;
import symbols.CommonSymbol;
import symbols.Function;
import symbols.Pointer;
import symbols.Struct;
import symbols.Variable;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Map;

import javax.management.RuntimeErrorException;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;


public class Compiler extends cBaseVisitor<String>
{
    public static final Map<String, Integer> typeSizeMap = Map.of(
        "char", 1,
        "int", 4,
        "unsigned", 4,
        "float", 4,
        "double", 8
    );

    protected static FileWriter writer;
    protected Context ctx = new Context();
    private boolean verbose = false;
    protected String heapString = ""; // an entire program can share a single heap string

    public Compiler(boolean verbose, String outputPath) throws IOException
    {
        this.verbose = verbose;
        try {
            writer = new FileWriter(outputPath);        
        } catch (IOException e) {
            System.err.printf("Error: Cannot open output file\n%s\n", e.toString());
        }
    }

    /////////////////////////////////////////////////////////////////////
    ////////////////////////   Initialization   /////////////////////////
    /////////////////////////////////////////////////////////////////////

    @Override
    public String visitInitDeclarator(cParser.InitDeclaratorContext ctx)
    {
        if (ctx.declarator()!=null)
            visit(ctx.declarator());
        if (ctx.initializer() != null)
            visit(ctx.initializer());
        return "";
    }

    public void storeSymbol(CommonSymbol symbol, String reg)
    {
        switch (symbol.getType()) 
        {
            case "char":
            {
                
            }
            break;
            case "double":
            {
                // TODO: handle double variable init
            }
            break;
            case "float":
            {
                // TODO: handle float variable init
            }
            break;
            case "unsigned":
            {
                // TODO: handle unsigned variable init
            }
            break;
            default: // int
            {
                this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeSwLwInstruction("sw", reg, symbol.getOffset(), "s0") + "\n");
            }
            break;
        }
    }

    @Override
    public String visitInitializer(cParser.InitializerContext ctx)
    {
        CommonSymbol symbol = this.ctx.getFrontOfInitQueue();
        // String reg = this.ctx.getReg("t", symbol.getType(), true);
        visit(ctx.assignmentExpression()); // compile into the register
        storeSymbol(symbol, this.ctx.getTopReg());
        this.ctx.clearTopOfStack();
        return "";
    }
    /////////////////////////////////////////////////////////////////////
    /////////////////////   Initialization END   ////////////////////////
    /////////////////////////////////////////////////////////////////////

    
    /////////////////////////////////////////////////////////////////////
    ////////////////////////   Declarations   ///////////////////////////
    /////////////////////////////////////////////////////////////////////

    @Override
    public String visitDeclaration(cParser.DeclarationContext ctx)
    {
        String type = getType(ctx.declarationSpecifiers());
                
        if (!typeSizeMap.keySet().contains(type))
            throw new RuntimeException("Error: Invalid type");

        this.ctx.setDeclarationMode(type); // we are not declaring symbols of this type
        if (ctx.initDeclaratorList() != null)
            visit(ctx.initDeclaratorList());
        else 
            visit(ctx.declarationSpecifiers()); // we are declaring a single variable
        this.ctx.setDeclarationMode(""); // we have finished declaring the symbols
        
        return "";
    }

    private CommonSymbol declareVariable(String id, String type, boolean addToQueue, boolean isGlobal)
    {
        if (isGlobal)
        {
            // TODO: handle global variables
            return null;
        }
        Variable var = new Variable(id, type, this.ctx.getCurrentFunction().getCurrentOffset(), isGlobal); // create variable
        this.ctx.getCurrentFunction().decrementSymbolOffset(typeSizeMap.get(type)); // decrement offset by size of variable
        this.ctx.allocateMemory(typeSizeMap.get(type), this.ctx.getCurrentFunction().getId());
        if (verbose) System.out.printf("Creating Variable: %s, type: %s offset: %d \n", var.getId(), var.getType(), var.getOffset());
            
        if (addToQueue)
            this.ctx.addInitializer(var); // add variable to initializer queue
        return var;
    } 

    private String declareFunction(cParser.DeclaratorContext ctx, String id, String returnType)
    {
        if (this.verbose)
        {
            System.out.printf("###################### Function Declaration ################### \n");
            System.out.printf("Function Name: %s\n", id);
            // System.out.printf("Return Type: %s\n", functionType);
            // System.out.printf("Function Header: \n%s\n", functionHeaderString);
            // System.out.printf("Function Body: \n%s\n", functionBodyString);
        }

        this.ctx.addScope(id, this.ctx.getDeclarationMode(), true, true);
        this.ctx.setReturnLabel(id);
        
        // get parameters ready
        if (ctx.directDeclarator().param != null)
            prepareParameter(ctx.directDeclarator().param);
        if (this.verbose) System.out.printf("###################### Function Declaration END ################### \n");
        return id;
    }

    @Override
    public String visitDeclarator(cParser.DeclaratorContext ctx)
    {
        // get id
        String id = visit(ctx.directDeclarator()); // should return the id of the symbol

        // do some error checking here
        if (id.equals(""))
            throw new RuntimeException("Error: Cannot find id");

        if (this.ctx.getDeclarationMode().equals("")) // we are not declaring symbols
            throw new RuntimeException("Error: Cannot declare symbol outside of declaration, type not found");

        // declare the symbol accordingly
        CommonSymbol symbol = null;
        
        if (ctx.pointer() != null) // if there is a pointer, we need to add it to the type
        {
            // TODO: declare pointer
        }
        else if (ctx.directDeclarator().arrayDecl != null)
        {
            // TODO: declare array
        } 
        else if (ctx.directDeclarator().varDecl != null)
        {
            symbol = declareVariable(id, this.ctx.getDeclarationMode(), true, this.ctx.isGlobalScope());
        }
        else // if its not any other declarator then its a function declare it here
        {
            return declareFunction(ctx, id, this.ctx.getDeclarationMode());
        }   


        // add declared symbol to the correct scope
        if (this.ctx.isGlobalScope())
        {
            // TODO: implement global scope
        }
        else
        {
            this.ctx.addLocalSymbol(symbol); // adding global symbol
        }

        return id;
    }


    @Override 
    public String visitDirectDeclarator(cParser.DirectDeclaratorContext ctx)
    {
        // return id
        String id = "";
        if (ctx.directDeclarator() != null)
            id = visit(ctx.directDeclarator());
        else
            id = ctx.Identifier().getText();

        return id;
    }

    @Override
    public String visitTypedefName(cParser.TypedefNameContext ctx)
    {
        if (this.ctx.getDeclarationMode().equals(""))
        {
            // TODO: Implement actual typedef here with early return instead of throw 
            throw new RuntimeException("Error: Cannot declare symbol outside of declaration, type not found");
        }
    
        String id = ctx.Identifier().getText();
        if (id == null)
            throw new RuntimeException("Error: Cannot find id");
        
        // only variables can be allocates this way, no need to insert a switch statement here
        Variable symbol = new Variable(id, this.ctx.getDeclarationMode(), this.ctx.getCurrentFunction().getCurrentOffset(), this.ctx.isGlobalScope()); // create variable
        this.ctx.getCurrentFunction().decrementSymbolOffset(typeSizeMap.get(this.ctx.getDeclarationMode())); // decrement offset by size of variable
        this.ctx.allocateMemory(typeSizeMap.get(this.ctx.getDeclarationMode()), this.ctx.getCurrentFunction().getId());
        if (verbose) System.out.printf("Creating Variable: %s, type: %s offset: %d \n", symbol.getId(), symbol.getType(), symbol.getOffset());
        if (this.ctx.isGlobalScope())
        {
            // TODO: implement global scope
        }
        else
        {
            this.ctx.addLocalSymbol(symbol); // adding global symbol
        }
        
        return "";
    }

    /////////////////////////////////////////////////////////////////////
    ////////////////////////   Declarations END   ///////////////////////
    /////////////////////////////////////////////////////////////////////
    
    @Override
    public String visitParameterDeclaration(cParser.ParameterDeclarationContext ctx)
    {
        String id = visit(ctx.declarator().directDeclarator());
        
        if (id == null || id.equals(""))
            throw new RuntimeException("Error: Cannot find parameter id");
        
        String type = getType(ctx.declarationSpecifiers());
        
        if (!typeSizeMap.keySet().contains(type))
            throw new RuntimeException("Error: Invalid parameter type");

        cParser.DeclaratorContext declarator = ctx.declarator(); // picked up declarator, this has to exist
        
        if (declarator == null)        
            throw new RuntimeException("Error: Cannot find declarator, check syntax");

        CommonSymbol symbol = null;


        if (declarator.pointer() != null) // if there is a pointer, we need to add it to the type
        {
            // TODO: prepare pointer parameter
        }
        else if (declarator.directDeclarator().arrayDecl != null)
        {
            // TODO: prepare array parameter (with size) (see if u can treat both cases with pointers)
        } 
        else if (declarator.directDeclarator().arrayParam != null)
        {
            // TODO: prepare array parameter (without size) (see if u can treat both cases with pointers)
        }
        else if (declarator.directDeclarator().varDecl != null)
        {
            symbol = declareVariable(id, type,  false, this.ctx.isGlobalScope());
        }
        else
        {
            throw new RuntimeException("Error: Cannot prepare paramter symbol, check syntax");
            // return "";
        }
        
        if (verbose) System.out.printf("Prepared parameter: %s of type: %s\n", id, type);

        // add parameter to function list
        this.ctx.getCurrentFunction().addParameter(symbol);
        return "";
    }


    private void prepareParameter(cParser.ParameterTypeListContext ctx)
    {
        visitChildren(ctx); // visit all children
    }


    @Override 
    public String visitFunctionDefinition(cParser.FunctionDefinitionContext ctx) 
    { 

        System.out.printf("Got here\n");
        String functionType = getType(ctx.declarationSpecifiers());
        
        this.ctx.setDeclarationMode(functionType);
        String id = visit(ctx.declarator());
        this.ctx.setDeclarationMode("");

        if (ctx.compoundStatement() != null)
        {
            String functionHeaderString = String.format(".text\n.globl %s\n\n%s:\n", id, id);
            String functionBodyString = "";
            this.ctx.headerStringsMap.put(id, functionHeaderString);
            this.ctx.bodyStringsMap.put(id, functionBodyString);            
        }

        if (!typeSizeMap.keySet().contains(functionType))
            throw new RuntimeException("Error: Invalid function type");

        visit(ctx.compoundStatement());        

        return "";
    }

    @Override
    public String visitCompilationUnit(cParser.CompilationUnitContext ctx)
    {
        visitChildren(ctx);
        if (verbose) System.out.printf("###################### Compilation Unit ################### \n");
        assert(this.ctx.isRegStackEmpty() == true);

        try 
        {
            for (String i : this.ctx.headerStringsMap.keySet()) 
            {
                if (verbose) System.out.printf("Fid: %s, StackSize: %s \n", i, this.ctx.getFunctionStackSize(i));
                String stackDecStr = writeStackDecrement(this.ctx.getFunctionStackSize(i));
                if (verbose) System.out.printf("Called in Compilation unit, ENSURE THAT REGISTERS ARE EMPTY \n");
                if (verbose) this.ctx.printRegMaps();
                String parameters = writeParameters(this.ctx.getFunctionParameters(i));
                String stackIncStr = writeStackIncrement(this.ctx.getFunctionStackSize(i), i);
                writer.write(this.ctx.headerStringsMap.get(i) + stackDecStr + parameters);
                writer.write(this.ctx.bodyStringsMap.get(i) + stackIncStr);
                writer.write("\n");
            }    

            writer.close();

        } 
        catch (Exception e) 
        {
            System.err.printf("Error: Cannot write to output file\n%s\n", e.toString());
            return "";
        }    

        if (this.verbose)
        {
            System.out.printf("###################### Compilation Unit END ################### \n");
            this.ctx.printFunctionSymbolTable();
            this.ctx.printRegisterStackStatus();
            this.ctx.printAllSymbolTables();
        }


        return  "";
    }


    ///////////////////////////////////////////////////////////////////////
    //////////////////         Jump Statements       //////////////////////
    ///////////////////////////////////////////////////////////////////////
    
    private void intReturn(String valueReg, cParser.JumpStatementContext ctx, String exprType)
    {
        if (typeSizeMap.containsKey(exprType))
            this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeSwLwInstruction("lw", this.ctx.getTopReg(), 0, this.ctx.getTopReg()) + "\n");

        this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeMvInstruction("a0", valueReg) + "\n");
    }

    private void floatReturn(String valueReg, cParser.JumpStatementContext ctx)
    {
        ; // TODO: implement float return
    } 

    @Override
    public String visitJumpStatement(cParser.JumpStatementContext ctx)
    {
        String jType = ctx.jtype.getText();
        
        if (verbose)
        {
            System.out.printf("###################### Jump Statement ################### \n");
            System.out.printf("jump type: %s\n", jType);
            System.out.printf("Current return type: %s\n", this.ctx.getCurrentFunction().getType());
        }


        switch (jType) {
            case "return":
            {
                this.ctx.setUsed("a0", false); // set a0 as used and push it on the stack 
                this.ctx.setFunctionReturn(this.ctx.getCurrentFunction().getId(), true);
                String exprType = visit(ctx.expression());
                if (verbose) System.out.printf("Expression returned type: %s\n", exprType);
                this.ctx.setFunctionReturn(this.ctx.getCurrentFunction().getId(), false);

                switch (this.ctx.getCurrentFunction().getType()) {
                    case "double":
                    {
                        // TODO: implement double return
                    }
                    break;
                    case "float":
                    {
                        floatReturn(this.ctx.getTopReg(), ctx);
                    }
                    break;
                    case "char":
                    {
                        // TODO: implement char return
                    }
                    break;
                    case "unsigned":
                    {
                        intReturn(this.ctx.getTopReg(), ctx, exprType); // all the relevant expressions will use the correct unsigned instructions, the return move remains the same   
                    }
                    break;
                    default: // int 
                    {
                        intReturn(this.ctx.getTopReg(), ctx, exprType);
                    }
                    break;
                }

                this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), "j " + this.ctx.getReturnLabel(this.ctx.getCurrentFunction().getId()) + "\n");
                this.ctx.clearReg("a0");
                this.ctx.clearTopOfStack(); 
            }
            break;
            case "break":
            {
                this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), "j " + this.ctx.getTopOfBreakStack() + "\n");
            }
            break;
            case "continue":
            {
                this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), "j " + this.ctx.getTopOfContinueStack() + "\n");// Not Implemented
            }
            break;
            default: // Not Implemented
                throw new RuntimeErrorException(null, "Go to not implemented \n");
        }
        if (verbose) System.out.printf("###################### Jump Statement END ################### \n");

        return "";
    }

    ///////////////////////////////////////////////////////////////////////
    //////////////////       Jump Statements END      /////////////////////
    ///////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////
    //////////////////      Primary EXPRESSIONS      //////////////////////
    ///////////////////////////////////////////////////////////////////////

    public String primaryExpressionConstant(TerminalNode constantNode)
    {
        Function currFunction = this.ctx.getCurrentFunction();
        String currFunctionBody = this.ctx.bodyStringsMap.get(currFunction.getId());
        String constant = constantNode.getText();
        String intRegex = "O*[0-9]+$|^0x[0-9a-fA-F]+"; // integer regex String    
        String floatRegex = "[0-9]+\\.[0-9]+|[0-9]+\\.[0-9]+\\(e[+][0-9]+\\)?|0x[0-9]+\\.[0-9]+p-[0-9]+"; // float regex String
        String charRegex = "\'[a-zA-Z0-9]\'"; // char regex String
        String type = "";
        if (verbose) System.out.printf("const: %s, regexMatch: %b\n", constant, constant.matches(intRegex));
        if (constant.matches(intRegex))
        {
            type = "constant int";
            String reg; 
            reg = this.ctx.getReg("t", "int", true);
            
            if (reg == null)
                reg = this.ctx.getReg("a", "int", true);

            if (reg == null) // sorta illegal but not really cz i will store all saved regs on the stack later on when implementing recursion
                reg = this.ctx.getReg("s", "int", true);
                
            if (reg == null)
                throw new RuntimeException("Error: Cannot find register to load constant into");


            this.ctx.bodyStringsMap.put(currFunction.getId(), currFunctionBody + writeLiInstruction(reg, constant) + "\n");
        
        }
        else if (constant.matches(floatRegex))
        {
            type = "constant double"; // treat decimals as double by default 
            // TODO: implement double/float constant
        }
        else if (constant.matches(charRegex))
        {
            type = "constant char";
            // TODO: implement char constant
        }

        if (type.equals("")) // could not identify type
        {
            System.err.printf("Error: Invalid constant %s\n", constant);
            throw new RuntimeException();
        }


        return type;
    }

    public String variablePrimaryExpression(Variable symbol)
    {
        String type = symbol.getType();
        String id = symbol.getId();

        if (verbose)
        {
            System.out.printf("###################### Identifier Variable ####################### \n");
            System.out.printf("id: %s, type: %s, offset: %s \n", id, type, symbol.getOffset());
            System.out.printf("###################### Primary Expression END ################### \n");            
        }
        // this will always load a pointer into the register at the top of the stack
        String topReg = this.ctx.getReg("t", type, true);
        if (symbol.isGlobal())
        {
            // TODO: implement global symbol
        }
        else
        {
            this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeImmediateInstruction("addi", topReg, "s0", this.ctx.getSymbol(id).getOffset()) + "\n"); // loads pointer to symbol
        }
        
        return type;
    }

    public String primaryExpressionId(TerminalNode idNode)
    {
        String id = idNode.getText();
        String type = "";
        
        if (!this.ctx.symbolExistsInScope(id))
            throw new RuntimeException(String.format("Error: Symbol %s does not exist in scope", id));

        CommonSymbol symbol = this.ctx.getSymbol(id);
        if (symbol instanceof Variable)
        {
            return variablePrimaryExpression((Variable) symbol);
        }
        else if (symbol instanceof Function)
        {
            return symbol.getId(); // return the name of the function we are calling
        }
        else if (symbol instanceof Pointer)
        {
            // TODO: implement pointer primary expression
        }
        else if (symbol instanceof Struct)
        {
            // TODO: implement struct primary expression
        }
        else if (symbol instanceof symbols.Array)
        {
            // TODO: implement array primary expression
        }

        throw new RuntimeException("Error: Invalid symbol type");
    }

    @Override
    public String visitPrimaryExpression(cParser.PrimaryExpressionContext ctx)
    {
        if (ctx.Identifier() != null)
        {
            // if (this.ctx.getCurrentFunction().isReturning()); // TODO: handle returning identifiers
            return primaryExpressionId(ctx.Identifier());
        }
        else if (ctx.Constant() != null)
        {
            // if (this.ctx.getCurrentFunction().isReturning())
                // return primaryExprReturning(ctx);
            
            return primaryExpressionConstant(ctx.Constant());
        }
        else if (ctx.str != null)
        {
            // TODO: implement primary expression string literal
        }
        else /*(ctx.expr != null)*/
        {
            return visit(ctx.expression());
        }

        return "";
    }

    ///////////////////////////////////////////////////////////////////////
    //////////////////      Primary EXPRESSIONS END     ///////////////////
    ///////////////////////////////////////////////////////////////////////


    ///////////////////////////////////////////////////////////////////////
    //////////////////         EXPRESSIONS           //////////////////////
    ///////////////////////////////////////////////////////////////////////


    // TODO: implement types for expressions, ltype and rtype are avialable at each expression node, figure out logic

    private void writeMultiplication(String rega, String regb)
    {
        this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeRegInstruction("mul", rega, rega, regb) + "\n");
    }

    private void writeDivision(String rega, String regb)
    {
        this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeRegInstruction("div", rega, rega, regb) + "\n");
    }

    private void writeModulo(String rega, String regb)
    {
        this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeRegInstruction("rem", rega, rega, regb) + "\n");
    }

    @Override
    public String visitMultiplicativeExpression(cParser.MultiplicativeExpressionContext ctx)
    {
        if (ctx.getChildCount() == 1)
            return visit(ctx.getChild(0));
        String[] lhsType = visit(ctx.lhs).split("\\s+");
        String lReg = this.ctx.getTopReg();
        unloadCheckpoint(lhsType, this.ctx.getCurrentFunction().getId(), lReg, lReg);
        String[] rhsType = visit(ctx.rhs).split("\\s+"); // returns rtype
        unloadCheckpoint(rhsType, this.ctx.getCurrentFunction().getId(), this.ctx.getTopReg(), this.ctx.getTopReg());
        String op = ctx.op.getText();
        switch (op) 
        {
            case "*":
            {
                writeMultiplication(lReg, this.ctx.getTopReg());
            }
            break;
            case "/":
            {
                writeDivision(lReg, this.ctx.getTopReg());
            }
            break;
            case "%":
            {
                writeModulo(lReg, this.ctx.getTopReg());                
            }
            break;
        }

        this.ctx.clearTopOfStack(); //clears r reg
        // this.ctx.clearTopOfStack(); //clears l reg, dont need to clear it because it will be overwritten by the result of the operation

        return "Multiplicative " + lhsType; // to inform parent that a multiplicative expression was evaluated, hence the value in lreg would not be treated as a pointer
    }

    private void writeAddition(String rega, String regb)
    {
        this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeRegInstruction("add", rega, rega, regb) + "\n");
    }

    private void writeSubtraction(String rega, String regb)
    {
        this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeRegInstruction("sub", rega, rega, regb) + "\n");
    }

    @Override
    public String visitAdditiveExpression(cParser.AdditiveExpressionContext ctx)
    {
        if (ctx.getChildCount() == 1)
            return visit(ctx.getChild(0));
        String[] lhsType = visit(ctx.lhs).split("\\s+");
        String lReg = this.ctx.getTopReg();
        unloadCheckpoint(lhsType, this.ctx.getCurrentFunction().getId(), lReg, lReg);
        String[] rhsType = visit(ctx.rhs).split("\\s+"); // returns rtype
        unloadCheckpoint(rhsType, this.ctx.getCurrentFunction().getId(), this.ctx.getTopReg(), this.ctx.getTopReg());
        String op = ctx.op.getText();

        switch (op) 
        {
            case "+":
            {
                writeAddition(lReg, this.ctx.getTopReg());

            }
            break;
            case "-":
            {
                writeSubtraction(lReg, this.ctx.getTopReg());
            }
            break;
        }
        
        this.ctx.clearTopOfStack(); //clears r reg

        return "Additive " + lhsType; // to inform parent that a additive expression was evaluated, hence the value in lreg would not be treated as a pointer
    }

    private void writeShiftLeft(String rega, String regb)
    {
        this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeRegInstruction("sll", rega, rega, regb) + "\n");
    }

    private void writeShiftRight(String rega, String regb)
    {
        this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeRegInstruction("sra", rega, rega, regb) + "\n");
    }

    @Override
    public String visitShiftExpression(cParser.ShiftExpressionContext ctx)
    {
        if (ctx.getChildCount() == 1)
            return visit(ctx.getChild(0));
        String[] lhsType = visit(ctx.lhs).split("\\s+");
        String lReg = this.ctx.getTopReg();
        unloadCheckpoint(lhsType, this.ctx.getCurrentFunction().getId(), lReg, lReg);
        String[] rhsType = visit(ctx.rhs).split("\\s+"); // returns rtype
        unloadCheckpoint(rhsType, this.ctx.getCurrentFunction().getId(), this.ctx.getTopReg(), this.ctx.getTopReg());
        String op = ctx.op.getText();

        switch (op) 
        {
            case "<<":
            {
                writeShiftLeft(lReg, this.ctx.getTopReg());
            }
            break;
            case ">>":
            {
                writeShiftRight(lReg, this.ctx.getTopReg());
            }
            break;
        }

        this.ctx.clearTopOfStack(); //clears r reg


        return "Shift " + lhsType; // to inform parent that a additive expression was evaluated, hence the value in lreg would not be treated as a pointer

    }

    private void writeLt(String rega, String regb)
    {
        this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeRegInstruction("slt", rega, rega, regb) + "\n");
        this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeImmediateInstruction("andi", rega, rega, 0xff) + "\n");
    }

    private void writeGt(String rega, String regb)
    {
        this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeRegInstruction("sgt", rega, rega, regb) + "\n");
        this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeImmediateInstruction("andi", rega, rega, 0xff) + "\n");
    }

    private void writeLtEq(String rega, String regb)
    {
        this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeRegInstruction("slt", rega, regb, rega) + "\n");
        this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeSeqzInstruction("seqz", rega, rega) + "\n");
        this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeImmediateInstruction("andi", rega, rega, 0xff) + "\n");
    }

    private void writeGtEq(String rega, String regb)
    {
        this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeRegInstruction("sgt", rega, regb, rega) + "\n");
        this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeSeqzInstruction("seqz", rega, rega) + "\n");
    }

    @Override
    public String visitRelationalExpression(cParser.RelationalExpressionContext ctx)
    {
        if (ctx.getChildCount() == 1)
            return visit(ctx.getChild(0));
        String[] lhsType = visit(ctx.lhs).split("\\s+");
        String lReg = this.ctx.getTopReg();
        unloadCheckpoint(lhsType, this.ctx.getCurrentFunction().getId(), lReg, lReg);
        String[] rhsType = visit(ctx.rhs).split("\\s+"); // returns rtype
        unloadCheckpoint(rhsType, this.ctx.getCurrentFunction().getId(), this.ctx.getTopReg(), this.ctx.getTopReg());
        String op = ctx.op.getText();

        switch (op) 
        {
            case "<":
            {
                writeLt(lReg, this.ctx.getTopReg());
            }
            break;
            case ">":
            {
                writeGt(lReg, this.ctx.getTopReg());
            }
            break;
            case "<=":
            {
                writeLtEq(lReg, this.ctx.getTopReg());
            }
            break;
            case ">=":
            {
                writeGtEq(lReg, this.ctx.getTopReg());
            }
            break;
        }

        this.ctx.clearTopOfStack();

        return "Relational " + lhsType ; // to inform parent that a additive expression was evaluated, hence the value in lreg would not be treated as a pointer
    }

    private void writeEq(String rega, String regb)
    {
        this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeRegInstruction("sub", rega, rega, regb) + "\n");
        this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(),  writeSeqzInstruction("seqz", rega, regb) + "\n");
    }

    @Override
    public String visitEqualityExpression(cParser.EqualityExpressionContext ctx)
    {
        if (ctx.getChildCount() == 1)
            return visit(ctx.getChild(0));
        String[] lhsType = visit(ctx.lhs).split("\\s+");
        String lReg = this.ctx.getTopReg();
        unloadCheckpoint(lhsType, this.ctx.getCurrentFunction().getId(), lReg, lReg);
        String[] rhsType = visit(ctx.rhs).split("\\s+"); // returns rtype
        unloadCheckpoint(rhsType, this.ctx.getCurrentFunction().getId(), this.ctx.getTopReg(), this.ctx.getTopReg());
        String op = ctx.op.getText();

        switch (op) 
        {
            case "==":
            {
                writeEq(lReg, this.ctx.getTopReg());
            }
            break;
            case "!=":
            {
                this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeRegInstruction("sne", lReg, lReg, this.ctx.getTopReg()) + "\n");
            }
            break;
        }

        this.ctx.clearTopOfStack();

        return "Equality " + lhsType;
    }

    @Override
    public String visitAndExpression(cParser.AndExpressionContext ctx)
    {
        if (ctx.getChildCount() == 1)
            return visit(ctx.getChild(0));
        String[] lhsType = visit(ctx.lhs).split("\\s+");
        String lReg = this.ctx.getTopReg();
        unloadCheckpoint(lhsType, this.ctx.getCurrentFunction().getId(), lReg, lReg);
        String[] rhsType = visit(ctx.rhs).split("\\s+"); // returns rtype
        unloadCheckpoint(rhsType, this.ctx.getCurrentFunction().getId(), this.ctx.getTopReg(), this.ctx.getTopReg());

        this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeRegInstruction("and", lReg, lReg, this.ctx.getTopReg()) + "\n");
        
        this.ctx.clearTopOfStack();

        return "Anded " + lhsType;
    }

    @Override
    public String visitExclusiveOrExpression(cParser.ExclusiveOrExpressionContext ctx)
    {
        if (ctx.getChildCount() == 1)
            return visit(ctx.getChild(0));
        String[] lhsType = visit(ctx.lhs).split("\\s+");
        String lReg = this.ctx.getTopReg();
        unloadCheckpoint(lhsType, this.ctx.getCurrentFunction().getId(), lReg, lReg);
        String[] rhsType = visit(ctx.rhs).split("\\s+"); // returns rtype
        unloadCheckpoint(rhsType, this.ctx.getCurrentFunction().getId(), this.ctx.getTopReg(), this.ctx.getTopReg());

        this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeRegInstruction("xor", lReg, lReg, this.ctx.getTopReg()) + "\n");

        this.ctx.clearTopOfStack();

        return "Xored " + lhsType;
    }

    @Override
    public String visitInclusiveOrExpression(cParser.InclusiveOrExpressionContext ctx)
    {
        if (ctx.getChildCount() == 1)
            return visit(ctx.getChild(0));
        String[] lhsType = visit(ctx.lhs).split("\\s+");
        String lReg = this.ctx.getTopReg();
        unloadCheckpoint(lhsType, this.ctx.getCurrentFunction().getId(), lReg, lReg);
        String[] rhsType = visit(ctx.rhs).split("\\s+"); // returns rtype
        unloadCheckpoint(rhsType, this.ctx.getCurrentFunction().getId(), this.ctx.getTopReg(), this.ctx.getTopReg());

        this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeRegInstruction("or", lReg, lReg, this.ctx.getTopReg()) + "\n");
        this.ctx.clearTopOfStack();

        return "Ored " + lhsType;
    }

    private void writeLogicalAnd(String rega, String regb)
    {
        String funcId = this.ctx.getCurrentFunction().getId();
        String labelOne = this.ctx.makeUnqiueLabel("AND"); //
        String labelTwo = this.ctx.makeUnqiueLabel("AND"); //
        this.ctx.writeBodyString(funcId, writeBranchInstruction("beq", rega, "zero", labelOne) + "\n");
        this.ctx.writeBodyString(funcId, writeBranchInstruction("beq", regb, "zero", labelOne) + "\n");
        this.ctx.writeBodyString(funcId, writeLiInstruction(rega, "1") + "\n");
        this.ctx.writeBodyString(funcId, "j " + labelTwo + "\n");
        this.ctx.writeBodyString(funcId, labelOne + ":\n");
        this.ctx.writeBodyString(funcId, writeLiInstruction(rega, "0") + "\n");
        this.ctx.writeBodyString(funcId, labelTwo + ":\n");
    }

    @Override
    public String visitLogicalAndExpression(cParser.LogicalAndExpressionContext ctx)
    {
        if (ctx.getChildCount() == 1)
            return visit(ctx.getChild(0));
        String[] lhsType = visit(ctx.lhs).split("\\s+");
        String lReg = this.ctx.getTopReg();
        unloadCheckpoint(lhsType, this.ctx.getCurrentFunction().getId(), lReg, lReg);
        String[] rhsType = visit(ctx.rhs).split("\\s+"); // returns rtype
        unloadCheckpoint(rhsType, this.ctx.getCurrentFunction().getId(), this.ctx.getTopReg(), this.ctx.getTopReg());
        
        writeLogicalAnd(lReg, this.ctx.getTopReg());

        this.ctx.clearTopOfStack();

        return "LogicalAnded " + lhsType;
    }

    private void writeLogicalOr(String rega, String regb)
    {
        String funcId = this.ctx.getCurrentFunction().getId();
        String labelOne = this.ctx.makeUnqiueLabel("OR"); //
        String labelTwo = this.ctx.makeUnqiueLabel("OR"); //
        this.ctx.writeBodyString(funcId, writeBranchInstruction("bne", rega, "zero", labelOne) + "\n");
        this.ctx.writeBodyString(funcId, writeBranchInstruction("beq", regb, "zero", labelTwo) + "\n");
        this.ctx.writeBodyString(funcId, labelOne + ":\n");
        this.ctx.writeBodyString(funcId, writeLiInstruction(rega, "1") + "\n");
        this.ctx.writeBodyString(funcId, "j " + labelTwo + "\n");
        this.ctx.writeBodyString(funcId, writeLiInstruction(rega, "0") + "\n");
        this.ctx.writeBodyString(funcId, labelTwo + ":\n");
    }

    @Override
    public String visitLogicalOrExpression(cParser.LogicalOrExpressionContext ctx)
    {
        if (ctx.getChildCount() == 1)
            return visit(ctx.getChild(0));
        String[] lhsType = visit(ctx.lhs).split("\\s+");
        String lReg = this.ctx.getTopReg();
        unloadCheckpoint(lhsType, this.ctx.getCurrentFunction().getId(), lReg, lReg);
        String[] rhsType = visit(ctx.rhs).split("\\s+"); // returns rtype
        unloadCheckpoint(rhsType, this.ctx.getCurrentFunction().getId(), this.ctx.getTopReg(), this.ctx.getTopReg());

        writeLogicalOr(lReg, this.ctx.getTopReg());
        this.ctx.clearTopOfStack();

        return "LogicalOred " + lhsType;
    }



    private void writeAssign(String funcId, String rhsReg, String type, String instr, String lhsReg)
    {
        switch (type) 
        {
            case "float":
            {
                // TODO: implement float parameter write assign 
            }
            break;
            case "double":
            {
                // TODO: implement double parameter write assign 
            }
            break;
            case "char":
            {
                // TODO: implement char parameter write assign
            }
            break;
            case "unsigned":
            {
                // TODO: implement unsigned parameter write assign 
            }
            break;
            default: // int default
            {
                String tmpReg = this.ctx.getReg("t", type, false);
                this.ctx.writeBodyString(funcId, writeSwLwInstruction("lw", tmpReg, 0, lhsReg) + "\n");
                this.ctx.writeBodyString(funcId, writeRegInstruction(instr, tmpReg, tmpReg, rhsReg) + "\n");
                this.ctx.clearReg(tmpReg);
            }
            break;
        }
    } 

    @Override
    public String visitAssignmentExpression(cParser.AssignmentExpressionContext ctx)
    {
        if (ctx.getChildCount() == 1)
        {
            return visit(ctx.getChild(0));
        }
        if (verbose)
        {
            System.out.printf("#########################################    Assignment Expression    #########################################\n");
            System.out.printf("Assignment Expression: %s\n", ctx.getText());
        }
        String rhsType[] = visit(ctx.rightHandSide).split("\\s+"); // all expressions will return the type of variable they evaluated, i.e whats the type of the variable at the top of the stack
        String rhsReg = this.ctx.getTopReg();
        unloadCheckpoint(rhsType, this.ctx.getCurrentFunction().getId(), rhsReg, rhsReg);       
       
        // now top reg holds a value
        String lhsType = visit(ctx.leftHandSide); // get the pointer to the left hand side, a new register is put onto the stack
        String op = ctx.assOp.getText(); // get the operator
        String funcId = this.ctx.getCurrentFunction().getId();
        switch (op)  // TODO: If statemenets for types within the cases
        {
            case "=":
            {
                this.ctx.writeBodyString(funcId, writeSwLwInstruction("sw", rhsReg, 0, this.ctx.getTopReg()) + "\n");
            }
            break;
            case "*=":
            {
                writeAssign(funcId, rhsReg, lhsType, "mul", this.ctx.getTopReg());
            }
            break;
            case "/=":
            {
                writeAssign(funcId, rhsReg, lhsType, "div", this.ctx.getTopReg());
            }
            break;
            case "%=":
            {
                writeAssign(funcId, rhsReg, lhsType, "rem", this.ctx.getTopReg());
            }
            break;
            case "+=":
            {
                writeAssign(funcId, rhsReg, lhsType, "add", this.ctx.getTopReg());
            }
            break;
            case "-=":
            {
                writeAssign(funcId, rhsReg, lhsType, "sub", this.ctx.getTopReg());
            }
            break;
            case "<<=":
            {
                writeAssign(funcId, rhsReg, lhsType, "sll", this.ctx.getTopReg());
            }
            break;
            case ">>=":
            {
                writeAssign(funcId, rhsReg, lhsType, "sra", this.ctx.getTopReg());
            }
            break;
            case "&=":
            {
                writeAssign(funcId, rhsReg, lhsType, "and", this.ctx.getTopReg());
            }
            break;
            case "^=":
            {
                writeAssign(funcId, rhsReg, lhsType, "xor", this.ctx.getTopReg());
            }
            break;
            case "|=":
            {
                writeAssign(funcId, rhsReg, lhsType, "or", this.ctx.getTopReg());
            }
            break;
        }

        this.ctx.clearStack(verbose);
        if (verbose) System.out.printf("#########################################    Assignment Expression END #####################################\n");

        return lhsType;
    }


    ///////////////////////////////////////////////////////////////////////
    //////////////////       EXPRESSIONS END       ////////////////////////
    ///////////////////////////////////////////////////////////////////////
    

    ///////////////////////////////////////////////////////////////////////
    //////////////////        GeneralStatements        ////////////////////
    ///////////////////////////////////////////////////////////////////////

    @Override
    public String visitStatement(cParser.StatementContext ctx)
    {
        String stmntReturn = visitChildren(ctx);
        if (!this.ctx.isRegStackEmpty())
            this.ctx.clearStack(verbose);
        return stmntReturn;
    }

    ///////////////////////////////////////////////////////////////////////
    //////////////////        GeneralStatements END    ////////////////////
    ///////////////////////////////////////////////////////////////////////
        
    ///////////////////////////////////////////////////////////////////////
    //////////////////        CompoundStatement        ////////////////////
    ///////////////////////////////////////////////////////////////////////

    // TODO: implement scopes here
    @Override 
    public String visitCompoundStatement(cParser.CompoundStatementContext ctx)
    {
        this.ctx.addScope(this.ctx.makeUnqiueLabel("COMPOUND"), "No Type", false, false);
        if (verbose)
        {
            System.out.printf("#########################################    Compound Statement    #########################################\n");
            System.out.printf("New Scope Added\n");
            this.ctx.printScopeStack();
            System.out.printf("#########################################    Compound Statement END #####################################\n");
        }
        visitChildren(ctx);
        this.ctx.removeTopScope(); // remove the scope
        return "";
    }


    ///////////////////////////////////////////////////////////////////////
    //////////////////        CompoundStatement END    ////////////////////
    ///////////////////////////////////////////////////////////////////////


    ///////////////////////////////////////////////////////////////////////
    //////////////////        IterationStatement        ///////////////////
    ///////////////////////////////////////////////////////////////////////

    private void writeCheck(String funcId, String type, String label)
    {
        switch (type) {
            case "float":
            {
                // TODO: implement float for loop
            }
            break;
            case "double":
            {
                // TODO: implement double for loop
            }
            break;
            case "unsigned":
            {
                // TODO: implement unsigned for loop
            }
            break;
            case "char":
            {
                // TODO: implement char for loop
            }
            break;
            default: // default int
            {
                this.ctx.writeBodyString(funcId, writeBranchInstruction("beq", this.ctx.getTopReg(), "zero", label) + "\n");
            }
            break;
        }
    }

    @Override
    public String visitExpressionStatement(cParser.ExpressionStatementContext ctx)
    {
        if (ctx.expression() != null)
           return visit(ctx.expression());
        return "";
    }

    private void writeIteration(cParser.IterationStatementContext ctx, String beginLabel, String endLabel, boolean isFor)
    {
        String funcId = this.ctx.getCurrentFunction().getId();
        if (isFor) visit(ctx.forInit);
        this.ctx.writeBodyString(funcId, beginLabel + ":\n");
        String[] condType = visit((isFor) ? ctx.forCond : ctx.whileCond).split("\\s+"); // compiles into a register       
        unloadCheckpoint(condType, funcId, this.ctx.getTopReg(), this.ctx.getTopReg());

        if (verbose)
        {
            System.out.printf("#########################################    For2 Loop    #########################################\n");
            System.out.printf("Condition Type: %s\n", (condType.length >= 2) ? condType[0] : condType[condType.length-1]);
            System.out.printf("#########################################    For2 Loop END #####################################\n");
        }
        writeCheck(funcId, condType[condType.length-1], endLabel);
        visit(ctx.statement()); // compiler whats inside
        if (isFor) visit(ctx.forExpr);
        this.ctx.clearStack(verbose); // for saftey lets clear the stack  after the expression, incase some idiot uses a stray expression here instead of a useful one
        this.ctx.writeBodyString(funcId, "j " + beginLabel + "\n");
        this.ctx.writeBodyString(funcId, endLabel + ":\n");
    }

    @Override
    public String visitIterationStatement(cParser.IterationStatementContext ctx)
    {
        if (verbose)
        {
            System.out.printf("#########################################    Iteration Statement    #########################################\n");
        }

        String beginLabel = "";
        String endLabel = "";

        if (ctx.While() != null)
        {
            beginLabel = this.ctx.makeUnqiueLabel("WHILE");
            endLabel = this.ctx.makeUnqiueLabel("ENDWHILE");
            this.ctx.pushToBreakStack(endLabel);
            this.ctx.pushToContinueStack(beginLabel);   
            writeIteration(ctx, beginLabel, endLabel, false);
        }
        else if (ctx.Do() != null)
        {
            // TODO: implement do while loop
        }
        else if (ctx.for1_token != null)
        {
            // TODO: implement for1 loop
        }
        else if (ctx.for2_token != null)
        {
            beginLabel = this.ctx.makeUnqiueLabel("FOR");
            endLabel = this.ctx.makeUnqiueLabel("ENDFOR");
            this.ctx.pushToBreakStack(endLabel);
            this.ctx.pushToContinueStack(beginLabel); 
            writeIteration(ctx, beginLabel, endLabel, true);
        }

        this.ctx.popFromBreakStack();
        this.ctx.popFromContinueStack();

        return "";
    }

    ///////////////////////////////////////////////////////////////////////
    //////////////////        IterationStatement END    ///////////////////
    ///////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////
    //////////////////        SelectionStatement    ///////////////////////
    ///////////////////////////////////////////////////////////////////////

    private void writeIfNoElse(cParser.SelectionStatementContext ctx)
    {
        String funcId = this.ctx.getCurrentFunction().getId();
        String[] condType = visit(ctx.expression()).split("\\s+"); 
        if (verbose)
        {
            System.out.printf("#########################################    If No Else    #########################################\n");
            System.out.printf("Condition Type: %s\n", (condType.length >= 2) ? condType[0] : condType[condType.length-1]);
            System.out.printf("#########################################    If No Else END #####################################\n");
        }
        unloadCheckpoint(condType, funcId, this.ctx.getTopReg(), this.ctx.getTopReg());
        String endIfLabel = this.ctx.makeUnqiueLabel("ENDIF");
        switch ((condType.length >= 2) ? condType[0] : condType[condType.length-1]) {
            case "float":
            {
                // TODO: implement float for if no else
            }
            break;
            case "double":
            {
                // TODO: implement double if no else
            }
            break;
            case "unsigned":
            {
                // TODO: implement unsigned if no else
            }
            break;
            case "char":
            {
                // TODO: implement char if no else
            }
            default: // default int
            {
                this.ctx.writeBodyString(funcId, writeBranchInstruction("beq", this.ctx.getTopReg(), "zero", endIfLabel) + "\n");
            }
            break;
        }
        visit(ctx.ifStatement);
        this.ctx.writeBodyString(funcId, endIfLabel + ":\n");
    }

    private void writeIfWithElse(cParser.SelectionStatementContext ctx)
    {
        String funcId = this.ctx.getCurrentFunction().getId();
        String[] condType = visit(ctx.expression()).split("\\s+"); 
        if (verbose)
        {
            System.out.printf("#########################################    If No Else    #########################################\n");
            System.out.printf("Condition Type: %s\n",(condType.length >= 2) ? condType[0] : condType[condType.length-1]);
            System.out.printf("#########################################    If No Else END #####################################\n");
        }
        unloadCheckpoint(condType, funcId, this.ctx.getTopReg(), this.ctx.getTopReg());
        String endIfLabel = this.ctx.makeUnqiueLabel("ENDIF");
        String elseLabel = this.ctx.makeUnqiueLabel("ELSE");
        switch (condType[condType.length-1]) { // shoof il ta3aseh **Face Palm**
            case "float":
            {
                // 
            }
            break;
            case "double":
            {
                // TODO: implement double if with else
            }
            break;
            case "unsigned":
            {
                // TODO: implement unsigned if with else
            }
            break;
            case "char":
            {
                // TODO: implement char if with else
            }
            default: // default int
            {
                this.ctx.writeBodyString(funcId, writeBranchInstruction("beq", this.ctx.getTopReg(), "zero", elseLabel) + "\n");
            }
            break;
        }
        visit(ctx.ifStatement);
        this.ctx.writeBodyString(funcId, "j " + endIfLabel + "\n");
        this.ctx.writeBodyString(funcId, elseLabel + ":\n");
        visit(ctx.elseStatement);
        this.ctx.writeBodyString(funcId, endIfLabel + ":\n");
    }

    @Override
    public String visitSelectionStatement(cParser.SelectionStatementContext ctx)
    {
        if (ctx.ifNoElse != null)
        {
            writeIfNoElse(ctx);
            return "";
        }
        else if (ctx.ifWithElse != null)
        {
            writeIfWithElse(ctx);
            return "";
        }
        else if (ctx.Switch() != null)
        {
            // TODO: implement switch statement
        }
        
        throw new RuntimeException("Unkown selection statement");
    }

    ///////////////////////////////////////////////////////////////////////
    //////////////////        SelectionStatement END    ///////////////////
    ///////////////////////////////////////////////////////////////////////


    ///////////////////////////////////////////////////////////////////////
    //////////////////        PostfixExpressions        ///////////////////
    ///////////////////////////////////////////////////////////////////////

    private String writeIncDecPostfixExpression(cParser.PostfixExpressionContext ctx, String type, String funcId, int incDec)
    {
        switch (type) {
            case "float":
            {
                // TODO: implement float IncDec postfix expression
            }
            break;
            case "double":
            {
                // TODO: TODO: implement double IncDec postfix expression
            }
            break;
            case "unsigned":
            {
                // TODO: implement unsigned IncDec postfix expression
            }
            break;
            case "char":
            {
                // TODO: implement char unsigned IncDec postfix expression
            }
            default: // default int
            {
                this.ctx.writeBodyString(funcId, writeImmediateInstruction("addi", this.ctx.getTopReg(), this.ctx.getTopReg(), incDec) + "\n");
                String tmpReg = this.ctx.getTopReg();
                visit(ctx.primaryExpression()); // get the location agian
                this.ctx.writeBodyString(funcId, writeSwLwInstruction("sw", tmpReg, 0, this.ctx.getTopReg()) + "\n");
                this.ctx.clearTopOfStack(); // clears the pointer to the location
                return "incDec " + type;
            }
        }

        return "";
    }

    @Override
    public String visitPostfixExpression(cParser.PostfixExpressionContext ctx)
    {
        if (ctx.getChildCount() == 1)
            return visit(ctx.getChild(0));
        
        if (ctx.arrayExpr != null)
        {
            // TODO: implement multi-dimensional array access
        }
        else if (ctx.objAccessOp != null)
        {
            // TODO: implement object access
        }
        else if (ctx.ptrAccessOp != null)
        {
            // TODO: implement pointer access
        }
        else if (ctx.incOp != null || ctx.decOp != null)
        {
            // TODO: implement increment and decrement
            String funcId = this.ctx.getCurrentFunction().getId();
            String[] type = visit(ctx.primaryExpression()).split("\\s+");
            unloadCheckpoint(type, funcId, this.ctx.getTopReg(), this.ctx.getTopReg());
            return writeIncDecPostfixExpression(ctx, type[type.length-1], funcId, (ctx.incOp != null) ? 1 : -1); // value loaded
        }
        else // function calls by def
        {
            System.out.printf("Function Call %s\n", ctx.argumentExpressionList());

            if (ctx.argumentExpressionList().toArray().length == 0)
            {
                String funcId = this.ctx.getCurrentFunction().getId();
                String symbolId = visit(ctx.primaryExpression());
                this.ctx.writeBodyString(funcId, "call " + symbolId + "\n");
                String type = this.ctx.getFunction(symbolId).getType();
                
                switch (type) 
                {
                    case "float":
                    {
                    }
                    break;
                    case "double":
                    {
                    }
                    break;
                    case "unsigned":
                    {
                    }
                    break;
                    case "char":
                    {
                    }
                    break;
                    default: // default int
                    {
                        this.ctx.getReg("t", type, true); // put the return value in a register
                        this.ctx.writeBodyString(funcId, writeMvInstruction(this.ctx.getTopReg(), "a0") + "\n"); // move the data out of a0 and return
                    }
                    break;
                }
                System.out.printf("callType: %s\n", type);
                return "FunctionCall " + type;
            }
          //  TODO: implement function call, with parameters            
        }

        throw new RuntimeException("Unkown postfix expression");
    }

    ///////////////////////////////////////////////////////////////////////
    //////////////////        PostfixExpressions END    ///////////////////
    ///////////////////////////////////////////////////////////////////////


    ///////////////////////////////////////////////////////////////////////
    //////////////////        ExternalDeclarations        /////////////////
    ///////////////////////////////////////////////////////////////////////

    @Override
    public String visitExternalDeclaration(cParser.ExternalDeclarationContext ctx)
    {
        if (ctx.functionDefinition() != null)
            return visit(ctx.functionDefinition());
        else if (ctx.declaration() != null)
            return visit(ctx.declaration());

        return "";
    }

    ///////////////////////////////////////////////////////////////////////
    //////////////////      ExternalDeclarations END      /////////////////
    ///////////////////////////////////////////////////////////////////////

    public static void main(String[] args) throws IOException, NoSuchFileException 
    {
        boolean verbose = false;
        String outputPath = "";
        CharStream input = null;

        // try to open the file, complain if u cant find it
        try 
        {
            if (args[0].equals("-S"))
                input =  CharStreams.fromFileName(args[1]);
            else 
                throw new Exception("Check the syntax of the input, -S not found");

            if (args[2].equals("-o"))
                outputPath = args[3];
            else
                throw new Exception("Check the syntax of the input, -o not found");
            
            if (args.length >= 5)
                if (args[4].equals("-v")) // verbose mode 
                    verbose = true;
                else
                    throw new Exception("Unkown option " + args[4]);
        } 
        catch (Exception e) 
        {
            System.err.printf("An Exception occured: %s\n", e.toString());
            System.exit(1);
        }

        Compiler compiler = new Compiler(verbose, outputPath);

        System.out.printf("%s\n", input.toString());

        cLexer lexer = new cLexer(input);
        CommonTokenStream tkSteam = new CommonTokenStream(lexer);
        cParser parser = new cParser(tkSteam);
        ParseTree tree = parser.compilationUnit();
        
        compiler.visit(tree); // generate Code

        System.out.printf("Compiled !\n");
    }
    

    private String writeStackDecrement(int offset)
    {
        String out = "";
        if (offset > 2032)
        {
            ; // TODO: handle stack decrement when offset is greater than 2032
        }
        else
        {   
            out = String.format("%s%s\n", out, writeImmediateInstruction("addi", "sp", "sp", -offset));
            out = String.format("%s%s\n", out, writeSwLwInstruction("sw", "ra", offset - 4, "sp"));
            out = String.format("%s%s\n", out, writeSwLwInstruction("sw", "s0", offset - 8, "sp"));
            out = String.format("%s%s\n", out, writeImmediateInstruction("addi", "s0", "sp", offset));
        }
        return out;
    }

    private String variableParameterWrite(Variable var)
    {
        switch (var.getType()) {
            case "float":
            {
                // TODO: implement float parameter write
            }
            break;
            case "double":
            {
                // TODO: implement double parameter write
            }
            break;
            case "char":
            {
                // TODO: implement char parameter write
            }
            break;
            case "unsigned":
            {
                // TODO: implement unsigned parameter write
            }
            break;
            default: // int default
            {
                String reg = this.ctx.getReg("a", var.getType(), false); // dont put on stack
                
                if (reg.equals("-1"))
                {
                    reg = this.ctx.getReg("a", "float", false); // dont put on stack

                }

                if (reg.equals("-1"))
                {
                    // TODO: implement parameter write when all int and float registers are full
                }
                
                return String.format("%s", writeSwLwInstruction("sw", reg, var.getOffset(), "s0")); // change s0 to sp when implementing stack all regs are full

            }
            // break; 
        }
        return "";
    }

    private String writeParameters(ArrayList<CommonSymbol> params)
    {
        if (params.size() == 0)
            return "";

        String out = "";
        
        this.ctx.setParameterizing(); // using regManager to organize and allocate parameters
        for (CommonSymbol i : params)
        {
            if (i instanceof Variable)
            {
                out = String.format("%s%s\n", out, variableParameterWrite((Variable)i));
            }
            else if (i instanceof symbols.Array)
            {
                // TODO: implement array parameter write
            }
            else if (i instanceof Struct)
            {
                // TODO: implement struct parameter write
            }
        }
        this.ctx.clearAllRegisters(); // clear all registers
        this.ctx.setNonParameterizing(); // done Parameterizing
        if (out.equals(""))
            throw new RuntimeException("writeParameters: out is empty");
        
        return out;
    }

    private String writeStackIncrement(int offset, String fid)
    {
        String out = "";
        if (offset > 2032)
        {
            ; // TODO: handle stack increment when offset is greater than 2032
        }
        else
        {   
            out = String.format("%s%s\n", out, this.ctx.getReturnLabel(fid) + ":");
            out = String.format("%s%s\n", out, writeSwLwInstruction("lw", "ra", offset - 4, "sp"));
            out = String.format("%s%s\n", out, writeSwLwInstruction("lw", "s0", offset - 8, "sp"));
            out = String.format("%s%s\n", out, writeImmediateInstruction("addi", "sp", "sp", offset));
            out = String.format("%s%s\n", out, "jr ra");
        }
        return out;
    }        

    private String writeImmediateInstruction(String instruction, String registera, String registerb, int immediate)
    {
        return String.format("%s %s, %s, %d", instruction, registera, registerb, immediate); 
    }

    private String writeSwLwInstruction(String instruction, String registera, int immediate, String registerb)
    {
        return String.format("%s %s, %d(%s)", instruction, registera, immediate, registerb); 
    }

    private String writeLiInstruction(String reg, String immediate)
    {
        return String.format("li %s, %s", reg, immediate);
    }

    private String writeMvInstruction(String regdst, String regsrc)
    {
        return String.format("mv %s, %s", regdst, regsrc);
    }

    private String writeRegInstruction(String instruction, String dst, String registera, String registerb)
    {
        return String.format("%s %s, %s, %s", instruction, dst, registera, registerb);
    }

    private String writeBranchInstruction(String instruction, String regA, String regB, String label)
    {
        return String.format("%s %s, %s, %s", instruction, regA, regB, label);
    }

    private String writeSeqzInstruction(String instruction, String rega, String regb)
    {
        return String.format("%s %s, %s", instruction, rega, rega);
    }

    private String unloadCheckpoint(String [] arr, String funcId, String regA, String regB)
    {
        if (arr.length == 1)
        {
            this.ctx.writeBodyString(funcId, writeSwLwInstruction("lw", regA, 0, regB) + "\n");
            return arr[0];
        }
        else
            return String.format("%s\n%s", arr[0], arr[1]);
    }

    private String getType(cParser.DeclarationSpecifiersContext ctx)
    {
        for (int i = 0; i < ctx.getChildCount(); i++)
        {
            if (ctx.getChild(i).getText().equals("int"))
                return "int";
            else if (ctx.getChild(i).getText().equals("float"))
                return "float";
            else if (ctx.getChild(i).getText().equals("double"))
                return "double";
            else if (ctx.getChild(i).getText().equals("char"))
                return "char";
            else if (ctx.getChild(i).getText().equals("unsigned"))
                return "unsigned";  
        }
        return "";
    }

}
