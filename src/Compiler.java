import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Map;

import javax.management.RuntimeErrorException;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import context.Context;
import grammar.cBaseVisitor;
import grammar.cLexer;
import grammar.cParser;
import symbols.CommonSymbol;
import symbols.Function;
import symbols.Pointer;
import symbols.Struct;
import symbols.Variable;


enum instructionType
{
    DOUBLE,
    FLOAT,
    INT
}

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
                // TODO: handle char variable init
            }
            break;
            case "double":
            {
                this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeSwLwInstruction("s", reg, String.valueOf(symbol.getOffset()), "s0", instructionType.DOUBLE) + "\n");
            }
            break;
            case "float":
            {
                this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeSwLwInstruction("sw", reg, String.valueOf(symbol.getOffset()), "s0", instructionType.FLOAT) + "\n");
            }
            break;
            case "unsigned":
            {
                // TODO: handle unsigned variable init
            }
            break;
            default: // int
            {
                this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeSwLwInstruction("sw", reg, String.valueOf(symbol.getOffset()), "s0", instructionType.INT) + "\n");
            }
            break;
        }
    }

    @Override
    public String visitInitializer(cParser.InitializerContext ctx)
    {
        CommonSymbol symbol = this.ctx.getFrontOfInitQueue();
        // String reg = this.ctx.getReg("t", symbol.getType(), true);
        String []rType = visit(ctx.assignmentExpression()).split("\\s+"); // compile into the register
        
        System.out.print("################################  INITIALIZER  ################################\n");
        System.out.printf("rType: %s, symbolType: %s\n", rType[rType.length-1], symbol.getType());
        System.out.print("################################  END INITIALIZER  ############################\n");

        if (!rType[rType.length-1].equals(symbol.getType()))
            castType(this.ctx.getCurrentFunction().getId(), this.ctx.getTopReg(), symbol.getType(), rType[rType.length-1]); // cast the expression to the correct type
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
        // this.ctx.getCurrentFunction().decrementSymbolOffset(typeSizeMap.get(type)); // decrement offset by size of variable
        this.ctx.allocateMemory(typeSizeMap.get(type), this.ctx.getCurrentFunction().getId());
        Variable var = new Variable(id, type, this.ctx.getCurrentFunction().getCurrentOffset(), isGlobal); // create variable
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
        // this.ctx.getCurrentFunction().decrementSymbolOffset(typeSizeMap.get(this.ctx.getDeclarationMode())); // decrement offset by size of variable
        this.ctx.allocateMemory(typeSizeMap.get(this.ctx.getDeclarationMode()), this.ctx.getCurrentFunction().getId());
        Variable symbol = new Variable(id, this.ctx.getDeclarationMode(), this.ctx.getCurrentFunction().getCurrentOffset(), this.ctx.isGlobalScope()); // create variable
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
                if (verbose) this.ctx.printRegisterStackStatus();
                if (verbose) this.ctx.printRegMaps();
                String parameters = writeParameters(this.ctx.getFunctionParameters(i));
                String stackIncStr = writeStackIncrement(this.ctx.getFunctionStackSize(i), i);
                writer.write(this.ctx.headerStringsMap.get(i) + stackDecStr + parameters);
                writer.write(this.ctx.bodyStringsMap.get(i) + stackIncStr);
                writer.write("\n");
                
                // writer.write(".data: \n");
                writer.write(this.heapString); // write heap string
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
    // TODO: implement return convertion when neccessary
    private void intReturn(String valueReg, cParser.JumpStatementContext ctx)
    {
        this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeMvInstruction("a0", valueReg, instructionType.INT) + "\n");
    }

    private void floatReturn(String valueReg, cParser.JumpStatementContext ctx)
    {
        this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeMvInstruction("fa0", valueReg, instructionType.FLOAT) + "\n");; 
    } 
    
    private void doubleReturn(String valueReg, cParser.JumpStatementContext ctx)
    {
        this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeMvInstruction("fa0", valueReg, instructionType.DOUBLE) + "\n");; 
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
                String[] exprType = visit(ctx.expression()).split("\\s+");
                instructionType insType = (exprType[exprType.length-1].equals("double")) ? instructionType.DOUBLE : (exprType[exprType.length-1].equals("float") ? instructionType.FLOAT : instructionType.INT);
                unloadCheckpoint(exprType, this.ctx.getCurrentFunction().getId(), this.ctx.getTopReg(), insType);
                if (verbose) System.out.printf("Expression returned type: %s\n", exprType[exprType.length-1]);
                this.ctx.setFunctionReturn(this.ctx.getCurrentFunction().getId(), false);
                String funcId = this.ctx.getCurrentFunction().getId();
                if (exprType[exprType.length - 1] != this.ctx.getCurrentFunction().getType())
                {
                    castType(funcId, this.ctx.getTopReg(), this.ctx.getCurrentFunction().getType(), exprType[exprType.length - 1]); // cast the expression to the correct type
                }

                switch (this.ctx.getCurrentFunction().getType()) {
                    case "double":
                    {
                        doubleReturn(this.ctx.getTopReg(), ctx);
                    }
                    break;
                    case "float":
                    {
                        floatReturn(this.ctx.getTopReg(), ctx);
                    }
                    break;
                    default: // int, char, unsigned
                    {
                        intReturn(this.ctx.getTopReg(), ctx);
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

    private String getConstRegister(String type, boolean onStack)
    {
        String reg = this.ctx.getReg("t", type, onStack);
        
        if (reg == null)
            reg = this.ctx.getReg("a", type, onStack);
        
        if (reg == null)
            reg = this.ctx.getReg("s", type, onStack);// sorta illegal but not really cz i will store all saved regs on the stack later on when implementing recursion
        
        if (reg == null)
            throw new RuntimeException("Error: Cannot find register to load constant into");
        
        return reg;
    }

    public String primaryExpressionConstant(TerminalNode constantNode)
    {
        Function currFunction = this.ctx.getCurrentFunction();
        String currFunctionBody = this.ctx.bodyStringsMap.get(currFunction.getId());
        String constant = constantNode.getText();
        String intRegex = "O*[0-9]+$|^0x[0-9a-fA-F]+"; // integer regex String    
        String doubleRegex = "[0-9]+\\.[0-9]+|[0-9]+\\.[0-9]+\\(e[+][0-9]+\\)?|0x[0-9]+\\.[0-9]+p-[0-9]+"; // float regex String
        String floatRegex = "[0-9]+\\.[0-9]+f?";
        String charRegex = "\'[a-zA-Z0-9]\'"; // char regex String
        String type = "";
        
        if (verbose) System.out.printf("const: %s, intRegexMatch: %b\n", constant, constant.matches(intRegex));
        if (verbose) System.out.printf("const: %s, doubleRegexMatch: %b\n", constant, constant.matches(doubleRegex));
        if (verbose) System.out.printf("const: %s, charRegexMatch: %b\n", constant, constant.matches(charRegex));
        if (verbose) System.out.printf("const: %s, floatRegexMatch: %b\n", constant, constant.matches(floatRegex));

        if (constant.matches(intRegex))
        {
            type = "constant int";
            String reg = getConstRegister("int", true);
            this.ctx.bodyStringsMap.put(currFunction.getId(), currFunctionBody + writeLiInstruction(reg, constant) + "\n");
        
        }
        // else if (constant.matches(doubleRegex))
        // {
        //     type = "constant double"; // treat decimals as double by default 
        //     // TODO: implement double/float constant
        // }
        else if (constant.matches(floatRegex))
        {
            type = "constant float";
            String floatLabel = this.ctx.makeUnqiueLabel("FLOAT");
            String reg = getConstRegister("int", false); // temporary address register
            this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeSwLwInstruction("lui", reg, "%hi", floatLabel, instructionType.INT) + "\n");
            writeAddition(reg, "%lo(" + floatLabel + ")", "int");
            String valueReg = this.ctx.getReg("t", "float", true); // ask for a value register to load in the correct value
            this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeSwLwInstruction("lw", valueReg, "0", reg, instructionType.FLOAT) + "\n");
            this.ctx.clearReg(reg); // no londer need this address reg
            float f = Float.parseFloat(constant);
            int i = Float.floatToIntBits(f);
            this.heapString += String.format("%s: .word %d\n", floatLabel, i);
        }
        else if (constant.matches(charRegex))
        {
            type = "constant char";
            String reg = getConstRegister("int", true);
            this.ctx.writeBodyString(currFunction.getId(), writeLiInstruction(reg, String.valueOf((int) constant.charAt(1))) + "\n");
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
        String topReg = this.ctx.getReg("t", "int", true); // pointers are always ints
        if (symbol.isGlobal())
        {
            // TODO: implement global symbol
        }
        else
        {
            this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeImmediateInstruction("addi", topReg, "s0", this.ctx.getSymbol(id).getOffset(), instructionType.INT) + "\n"); // loads pointer to symbol
        }
        
        return type;
    }

    public String primaryExpressionId(TerminalNode idNode)
    {
        String id = idNode.getText();
        
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

    private instructionType getPrecedentType(instructionType lefType, instructionType rightType) // TODO: implement type precedence for unsigned and char
    {
        if (lefType == instructionType.DOUBLE || rightType == instructionType.DOUBLE)
            return instructionType.DOUBLE;
        else if (lefType == instructionType.FLOAT || rightType == instructionType.FLOAT)
            return instructionType.FLOAT;
        else 
            return instructionType.INT;
    }

    private String getPrecedentTypeString(String leftType, String rightType, String funcId, String lreg, String rreg)
    {
        String out = "";
        if (leftType.equals("double") || rightType.equals("double"))
            out = "double";
        else if (leftType.equals("float") || rightType.equals("float"))
            out = "float";
        else if (leftType.equals("unsigned") || rightType.equals("unsigned"))
            out = "unsigned";
        else if (leftType.equals("int") || rightType.equals("int"))
            out = "int";
        else
            out = "char";

        return out;
    }

    // DANGEROUS OPERATION HERE, SWITCHING REGISTERS ON STACK
    /*
     * This function determines whether a cast is neccessary based on the type precedence.
     * If a cast is neccessary, it will cast the type of the register at the top of the stack.
     * and return the new register which has been casted.
     * This function should be used with care as it could replace the left register which is not at the top of the reg stack.
     * When this does occur its the job of the calling function to pick of the new left register and discard the old one.
     */
    private String[] precedentTypeCast(String precedentType, String leftType, String rightType, String funcId)
    {
        if (leftType != precedentType) 
        {
            String tmpReg = this.ctx.popRegisterStack(); // take right register off the stack
            castType(funcId, this.ctx.getTopReg(), precedentType, leftType); // cast left
            String newLreg = this.ctx.getTopReg(); // get new left register
            this.ctx.pushRegisterStack(tmpReg); // put right register back on the stack
            String[] arr = {"l", newLreg};
            return arr;
        }
        else if (rightType != precedentType)
            castType(funcId, this.ctx.getTopReg(), precedentType, rightType);// cast right
        String[] arr = {"r" ,this.ctx.getTopReg()};
        return arr;
    }


    // TODO: implement types for expressions, ltype and rtype are avialable at each expression node, figure out logic

    private void writeMultiplication(String rega, String regb, String precedentType)
    {
        switch (precedentType) 
        {
            case "double":
                this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeRegInstruction("fmul.d", rega, rega, regb, instructionType.DOUBLE) + "\n");                                  
            break;
            case "float":
                this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeRegInstruction("fmul.s", rega, rega, regb, instructionType.FLOAT) + "\n");                
            break;
            default:
                this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeRegInstruction("mul", rega, rega, regb, instructionType.INT) + "\n");
            break;
        }
    }

    private void writeDivision(String rega, String regb, String precedentType)
    {
        switch (precedentType) 
        {
            case "double":
                this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeRegInstruction("fdiv.d", rega, rega, regb, instructionType.DOUBLE) + "\n");                                  
            break;
            case "float":
                this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeRegInstruction("fdiv.s", rega, rega, regb, instructionType.FLOAT) + "\n");                
            break;
            default:
                this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeRegInstruction("div", rega, rega, regb, instructionType.INT) + "\n");
            break;
        }
    }

    private void writeModulo(String rega, String regb, String precedentType)
    {
        switch (precedentType) 
        {
            case "double":
                throw new RuntimeException("Error: Cannot perform modulo on double");
            case "float":
                throw new RuntimeException("Error: Cannot perform modulo on float");
            case "int":
                this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeRegInstruction("rem", rega, rega, regb, instructionType.INT) + "\n");
            break;
            default:
                this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeRegInstruction("remu", rega, rega, regb, instructionType.INT) + "\n");
            break;
        }
    }

    @Override
    public String visitMultiplicativeExpression(cParser.MultiplicativeExpressionContext ctx)
    {
        if (ctx.getChildCount() == 1)
            return visit(ctx.getChild(0));
        
        String[] lhsType = visit(ctx.lhs).split("\\s+");
        instructionType lType = (lhsType[lhsType.length-1].equals("double")) ? instructionType.DOUBLE : (lhsType[lhsType.length-1].equals("float") ? instructionType.FLOAT : instructionType.INT);
        unloadCheckpoint(lhsType, this.ctx.getCurrentFunction().getId(), this.ctx.getTopReg(), lType);
        String lReg = this.ctx.getTopReg();
        String[] rhsType = visit(ctx.rhs).split("\\s+"); // returns rtype
        instructionType rType = (rhsType[rhsType.length-1].equals("double")) ? instructionType.DOUBLE : (rhsType[rhsType.length-1].equals("float") ? instructionType.FLOAT : instructionType.INT);
        unloadCheckpoint(rhsType, this.ctx.getCurrentFunction().getId(), this.ctx.getTopReg(), rType);
        String precedentType = getPrecedentTypeString(lhsType[lhsType.length-1], rhsType[rhsType.length-1], this.ctx.getCurrentFunction().getId(), lReg, this.ctx.getTopReg());
        String[] newRegs = precedentTypeCast(precedentType, lhsType[lhsType.length-1], rhsType[rhsType.length-1], this.ctx.getCurrentFunction().getId());
        lReg = (newRegs[0].equals("l")) ? newRegs[1] : lReg;

        String op = ctx.op.getText();

        switch (op) 
        {
            case "*":
            {
                writeMultiplication(lReg, this.ctx.getTopReg(), precedentType);
            }
            break;
            case "/":
            {
                writeDivision(lReg, this.ctx.getTopReg(), precedentType);
            }
            break;
            case "%":
            {
                writeModulo(lReg, this.ctx.getTopReg(), precedentType);                
            }
            break;
        }

        this.ctx.clearTopOfStack(); //clears r reg
        // this.ctx.clearTopOfStack(); //clears l reg, dont need to clear it because it will be overwritten by the result of the operation
        lhsType[lhsType.length-1] = precedentType; // change the type of the lhs to the precedent type
        return "Multiplicative " + String.join(" ", lhsType); // to inform parent that a multiplicative expression was evaluated, hence the value in lreg would not be treated as a pointer
    }

    private void writeAddition(String rega, String regb, String precType)
    {
        switch (precType) 
        {
            case "double":
            {
                this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeRegInstruction("fadd.d", rega, rega, regb, instructionType.DOUBLE) + "\n");                            
            }
            break;
            case "float":
            {
                this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeRegInstruction("fadd.s", rega, rega, regb, instructionType.FLOAT) + "\n");                
            }
            break;
            default: // int
            {
                this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeRegInstruction("add", rega, rega, regb, instructionType.INT) + "\n");
            }
        }
    }

    private void writeSubtraction(String rega, String regb, String prectype)
    {
        switch (prectype) 
        {
            case "double":
            {
                this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeRegInstruction("fsub.d", rega, rega, regb, instructionType.DOUBLE) + "\n");                            
            }
            break;
            case "float":
            {
                this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeRegInstruction("fsub.s", rega, rega, regb, instructionType.FLOAT) + "\n");                
            }
            break;
            default: // int
            {
                this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeRegInstruction("sub", rega, rega, regb, instructionType.INT) + "\n");
            }
        }
    }

    @Override
    public String visitAdditiveExpression(cParser.AdditiveExpressionContext ctx)
    {
        if (ctx.getChildCount() == 1)
            return visit(ctx.getChild(0));
        
        String[] lhsType = visit(ctx.lhs).split("\\s+");
        instructionType lType = (lhsType[lhsType.length-1].equals("double")) ? instructionType.DOUBLE : (lhsType[lhsType.length-1].equals("float") ? instructionType.FLOAT : instructionType.INT);
        unloadCheckpoint(lhsType, this.ctx.getCurrentFunction().getId(), this.ctx.getTopReg(), lType);
        String lReg = this.ctx.getTopReg();
        String[] rhsType = visit(ctx.rhs).split("\\s+"); // returns rtype
        instructionType rType = (rhsType[rhsType.length-1].equals("double")) ? instructionType.DOUBLE : (rhsType[rhsType.length-1].equals("float") ? instructionType.FLOAT : instructionType.INT);
        unloadCheckpoint(rhsType, this.ctx.getCurrentFunction().getId(), this.ctx.getTopReg(), rType);
        String precedentType = getPrecedentTypeString(lhsType[lhsType.length-1], rhsType[rhsType.length-1], this.ctx.getCurrentFunction().getId(), lReg, this.ctx.getTopReg());
        String[] newRegs = precedentTypeCast(precedentType, lhsType[lhsType.length-1], rhsType[rhsType.length-1], this.ctx.getCurrentFunction().getId());
        lReg = (newRegs[0].equals("l")) ? newRegs[1] : lReg;

        String op = ctx.op.getText();
        switch (op) 
        {
            case "+":
            {
                writeAddition(lReg, this.ctx.getTopReg(), precedentType);

            }
            break;
            case "-":
            {
                writeSubtraction(lReg, this.ctx.getTopReg(), precedentType);
            }
            break;
        }
        
        this.ctx.clearTopOfStack(); //clears r reg
        lhsType[lhsType.length-1] = precedentType; // change the type of the lhs to the precedent type
        return "Additive " + String.join(" ", lhsType); // to inform parent that a additive expression was evaluated, hence the value in lreg would not be treated as a pointer
    }

    private void writeShiftLeft(String rega, String regb)
    {   
        this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeRegInstruction("sll", rega, rega, regb, instructionType.INT) + "\n");
    }

    private void writeShiftRight(String rega, String regb, String precType)
    {
        if (precType == "float" || precType == "double")
            throw new RuntimeException("Error: Cannot perform shift on float or double");
        if (precType == "unsigned")
            this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeRegInstruction("srl", rega, rega, regb, instructionType.INT) + "\n");
        else
            this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeRegInstruction("sra", rega, rega, regb, instructionType.INT) + "\n");
    }

    @Override
    public String visitShiftExpression(cParser.ShiftExpressionContext ctx)
    {
        if (ctx.getChildCount() == 1)
            return visit(ctx.getChild(0));
        
        String[] lhsType = visit(ctx.lhs).split("\\s+");
        instructionType lType = (lhsType[lhsType.length-1].equals("double")) ? instructionType.DOUBLE : (lhsType[lhsType.length-1].equals("float") ? instructionType.FLOAT : instructionType.INT);
        unloadCheckpoint(lhsType, this.ctx.getCurrentFunction().getId(), this.ctx.getTopReg(), lType);
        String lReg = this.ctx.getTopReg();
        String[] rhsType = visit(ctx.rhs).split("\\s+"); // returns rtype
        instructionType rType = (rhsType[rhsType.length-1].equals("double")) ? instructionType.DOUBLE : (rhsType[rhsType.length-1].equals("float") ? instructionType.FLOAT : instructionType.INT);
        unloadCheckpoint(rhsType, this.ctx.getCurrentFunction().getId(), this.ctx.getTopReg(), rType);
        
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
                writeShiftRight(lReg, this.ctx.getTopReg(), lhsType[lhsType.length - 1]);
            }
            break;
        }

        this.ctx.clearTopOfStack(); //clears r reg

        return "Shift " + String.join(" ", lhsType); // to inform parent that a additive expression was evaluated, hence the value in lreg would not be treated as a pointer

    }

    private String equalityRelationSwap(String lreg, String rreg) // VERY DESTRUCTIVE STACK MANIPULATION, USE WITH CAUTION
    {
        /*
         * This function assumes the following stack state:
         * -> rreg (fregs)
         *    lreg (fregs)
         * This function checks that the state is valid and changes the stack to the following state:
         * -> rreg (fregs)
         *    dstReg (iregs)
         * This function is used when computing inequality relations, where the result must be stored in an integer register
         */
        assert(rreg.equals(this.ctx.getTopReg()));
        String tmpRReg = this.ctx.popRegisterStack();
        assert(lreg.equals(this.ctx.getTopReg()));
        this.ctx.clearTopOfStack();
        String dstReg = this.ctx.getReg("t", "int", true);
        
        if (dstReg == null) 
        {
            dstReg = this.ctx.getReg("a", "int", true);    
        }
        
        if (dstReg == null) // illegal but because of the way recurions are done its ok
        {
            dstReg = this.ctx.getReg("s", "int", true);
        }
        this.ctx.pushRegisterStack(tmpRReg);        
        return dstReg;
    }

    private void writeLGT(String rega, String regb, String precType, boolean isLT)
    {
        String lg = (isLT) ? "l" : "g";
        switch (precType) {
            case "double":
            {
                String dstReg = equalityRelationSwap(rega, regb);
                this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeRegInstruction("f"+lg+"t.d", dstReg, rega, regb, instructionType.DOUBLE) + "\n");
                rega = dstReg;
                this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeSeqzInstruction("snez", dstReg, dstReg) + "\n");
            }
            break;
            case "float":
            {
                String dstReg = equalityRelationSwap(rega, regb);
                this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeRegInstruction("f"+lg+"t.s", dstReg, rega, regb, instructionType.DOUBLE) + "\n");
                rega = dstReg;
                this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeSeqzInstruction("snez", dstReg, dstReg) + "\n");
            }
            break;
            case  "int":
            {
                this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeRegInstruction("s"+lg+"t", rega, rega, regb, instructionType.INT) + "\n");
            }
            break;
            default: // int default 
            {
                this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeRegInstruction("s"+lg+"tu", rega, rega, regb, instructionType.INT) + "\n");
            }
            break;
        }
            this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeImmediateInstruction("andi", rega, rega, 0xff, instructionType.INT) + "\n");
    }

    private void writeLGTEq(String rega, String regb, String precType, boolean isLT)
    {
        String lg = (isLT) ? "l" : "g";
        String lgFD = (!isLT) ? "l" : "g";
        
        switch (precType) {
            case "double":
            {
                String dstReg = equalityRelationSwap(rega, regb);
                this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeRegInstruction("f"+lgFD+"e.d", dstReg, rega, regb, instructionType.DOUBLE) + "\n");
                rega = dstReg;
                this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeSeqzInstruction("snez", dstReg, dstReg) + "\n");
            }
            break;
            case "float":
            {
                String dstReg = equalityRelationSwap(rega, regb);
                this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeRegInstruction("f"+lgFD+"e.s", dstReg, rega, regb, instructionType.FLOAT) + "\n");
                rega = dstReg;
                this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeSeqzInstruction("snez", dstReg, dstReg) + "\n");
            }
            break;
            case  "int":
            {
                this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeRegInstruction("s"+lg+"t", rega, rega, regb, instructionType.INT) + "\n");
                this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeImmediateInstruction("xori", rega, rega, 1, instructionType.INT) + "\n");
            }
            break;
            default: // unsigned, char default 
            {
                this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeRegInstruction("s"+lg+"tu", rega, rega, regb, instructionType.INT) + "\n");
                this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeImmediateInstruction("xori", rega, rega, 1, instructionType.INT) + "\n");
            }
            break;
        }
            this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeImmediateInstruction("andi", rega, rega, 0xff, instructionType.INT) + "\n");
    }
    
    @Override
    public String visitRelationalExpression(cParser.RelationalExpressionContext ctx)
    {
        if (ctx.getChildCount() == 1)
            return visit(ctx.getChild(0));
        
        String[] lhsType = visit(ctx.lhs).split("\\s+");
        instructionType lType = (lhsType[lhsType.length-1].equals("double")) ? instructionType.DOUBLE : (lhsType[lhsType.length-1].equals("float") ? instructionType.FLOAT : instructionType.INT);
        unloadCheckpoint(lhsType, this.ctx.getCurrentFunction().getId(), this.ctx.getTopReg(), lType);
        String lReg = this.ctx.getTopReg();
        String[] rhsType = visit(ctx.rhs).split("\\s+"); // returns rtype
        instructionType rType = (rhsType[rhsType.length-1].equals("double")) ? instructionType.DOUBLE : (rhsType[rhsType.length-1].equals("float") ? instructionType.FLOAT : instructionType.INT);
        unloadCheckpoint(rhsType, this.ctx.getCurrentFunction().getId(), this.ctx.getTopReg(), rType);
        String precedentType = getPrecedentTypeString(lhsType[lhsType.length-1], rhsType[rhsType.length-1], this.ctx.getCurrentFunction().getId(), lReg, this.ctx.getTopReg());
        String[] newRegs = precedentTypeCast(precedentType, lhsType[lhsType.length-1], rhsType[rhsType.length-1], this.ctx.getCurrentFunction().getId());
        lReg = (newRegs[0].equals("l")) ? newRegs[1] : lReg;
        

        String op = ctx.op.getText();
        if (verbose) System.out.printf("precedentType: %s, lhstype: %s, areEqual: %b\n", precedentType, lhsType[lhsType.length-1], rhsType[rhsType.length-1], (precedentType.equals(lhsType[lhsType.length -1])));

        switch (op) 
        {
            case "<": writeLGT(lReg, this.ctx.getTopReg(), precedentType, true); break;
            case ">": writeLGT(lReg, this.ctx.getTopReg(), precedentType, false); break;
            case "<=": writeLGTEq(lReg, this.ctx.getTopReg(), precedentType, false); break;
            case ">=": writeLGTEq(lReg, this.ctx.getTopReg(), precedentType, true); break;
        }

        this.ctx.clearTopOfStack();
        lhsType[lhsType.length-1] = (precedentType.equals("float") || precedentType.equals("double")) ? "int" : precedentType; // change the type of the lhs to the precedent type
        return "Relational " + String.join(" ", lhsType); // to inform parent that a additive expression was evaluated, hence the value in lreg would not be treated as a pointer
    }

    private void writeEq(String rega, String regb, String precedentType, boolean isEq)
    {
        switch (precedentType) 
        {
            case "double":
            {
                String dstReg = equalityRelationSwap(rega, regb);
                this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeRegInstruction("feq.d", dstReg, rega, regb, instructionType.DOUBLE) + "\n");
                rega = dstReg;
                this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeSeqzInstruction((isEq) ? "snez" : "seqz", dstReg, dstReg) + "\n");
            }   
            break;
            case "float":
            {
                String dstReg = equalityRelationSwap(rega, regb);
                this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeRegInstruction("feq.s", dstReg, rega, regb, instructionType.FLOAT) + "\n");
                rega = dstReg;
                this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeSeqzInstruction((isEq) ? "snez" : "seqz", dstReg, dstReg) + "\n");
            }
            break;
            default:
            {
                this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeRegInstruction("sub", rega, rega, regb, instructionType.INT) + "\n");
                this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(),  writeSeqzInstruction((isEq) ? "seqz" : "snez", rega, regb) + "\n");
            }
            break;
        }
    }

    @Override
    public String visitEqualityExpression(cParser.EqualityExpressionContext ctx)
    {
        if (ctx.getChildCount() == 1)
            return visit(ctx.getChild(0));
        
        String[] lhsType = visit(ctx.lhs).split("\\s+");
        instructionType lType = (lhsType[lhsType.length-1].equals("double")) ? instructionType.DOUBLE : (lhsType[lhsType.length-1].equals("float") ? instructionType.FLOAT : instructionType.INT);
        unloadCheckpoint(lhsType, this.ctx.getCurrentFunction().getId(), this.ctx.getTopReg(), lType);
        String lReg = this.ctx.getTopReg();
        String[] rhsType = visit(ctx.rhs).split("\\s+"); // returns rtype
        instructionType rType = (rhsType[rhsType.length-1].equals("double")) ? instructionType.DOUBLE : (rhsType[rhsType.length-1].equals("float") ? instructionType.FLOAT : instructionType.INT);
        unloadCheckpoint(rhsType, this.ctx.getCurrentFunction().getId(), this.ctx.getTopReg(), rType);
        String precedentType = getPrecedentTypeString(lhsType[lhsType.length-1], rhsType[rhsType.length-1], this.ctx.getCurrentFunction().getId(), lReg, this.ctx.getTopReg());
        String[] newRegs = precedentTypeCast(precedentType, lhsType[lhsType.length-1], rhsType[rhsType.length-1], this.ctx.getCurrentFunction().getId());
        lReg = (newRegs[0].equals("l")) ? newRegs[1] : lReg;        
        String op = ctx.op.getText();
        writeEq(lReg, this.ctx.getTopReg(), precedentType, op.equals("=="));
        this.ctx.clearTopOfStack();
        lhsType[lhsType.length-1] = (precedentType.equals("float") || precedentType.equals("double")) ? "int" : precedentType; // change the type of the lhs to the precedent type
        return "Equality " + String.join(" ", lhsType);
    }

    private void writeAnd(String rega, String regb, instructionType opType)
    {
        this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeRegInstruction("and", rega, rega, this.ctx.getTopReg(), instructionType.INT) + "\n");
    }

    @Override
    public String visitAndExpression(cParser.AndExpressionContext ctx)
    {
        if (ctx.getChildCount() == 1)
            return visit(ctx.getChild(0));
        String[] lhsType = visit(ctx.lhs).split("\\s+");
        instructionType lType = (lhsType[lhsType.length-1].equals("double")) ? instructionType.DOUBLE : (lhsType[lhsType.length-1].equals("float") ? instructionType.FLOAT : instructionType.INT);
        unloadCheckpoint(lhsType, this.ctx.getCurrentFunction().getId(), this.ctx.getTopReg(), lType);
        String lReg = this.ctx.getTopReg();
        String[] rhsType = visit(ctx.rhs).split("\\s+"); // returns rtype
        instructionType rType = (rhsType[rhsType.length-1].equals("double")) ? instructionType.DOUBLE : (rhsType[rhsType.length-1].equals("float") ? instructionType.FLOAT : instructionType.INT);
        unloadCheckpoint(rhsType, this.ctx.getCurrentFunction().getId(), this.ctx.getTopReg(), rType);
        lType = getPrecedentType(lType, rType);
        if (lType == instructionType.FLOAT || lType == instructionType.DOUBLE)
            throw new RuntimeException("Error: Cannot perform bitwise and on float or double");
        writeAnd(lReg, this.ctx.getTopReg(), lType);
        this.ctx.clearTopOfStack();
        return "Anded " + String.join(" ", lhsType);
    }

    private void writeXor(String rega, String regb, instructionType opType)
    {
        this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeRegInstruction("xor", rega, rega, this.ctx.getTopReg(), instructionType.INT) + "\n");
    }


    @Override
    public String visitExclusiveOrExpression(cParser.ExclusiveOrExpressionContext ctx)
    {
        if (ctx.getChildCount() == 1)
            return visit(ctx.getChild(0));
        String[] lhsType = visit(ctx.lhs).split("\\s+");
        instructionType lType = (lhsType[lhsType.length-1].equals("double")) ? instructionType.DOUBLE : (lhsType[lhsType.length-1].equals("float") ? instructionType.FLOAT : instructionType.INT);
        unloadCheckpoint(lhsType, this.ctx.getCurrentFunction().getId(), this.ctx.getTopReg(), lType);
        String lReg = this.ctx.getTopReg();
        String[] rhsType = visit(ctx.rhs).split("\\s+"); // returns rtype
        instructionType rType = (rhsType[rhsType.length-1].equals("double")) ? instructionType.DOUBLE : (rhsType[rhsType.length-1].equals("float") ? instructionType.FLOAT : instructionType.INT);
        unloadCheckpoint(rhsType, this.ctx.getCurrentFunction().getId(), this.ctx.getTopReg(), rType);
        lType = getPrecedentType(lType, rType);
        if (lType == instructionType.FLOAT || lType == instructionType.DOUBLE)
            throw new RuntimeException("Error: Cannot perform bitwise and on float or double");

        writeXor(lReg, this.ctx.getTopReg(), lType);

        this.ctx.clearTopOfStack();

        return "Xored " + String.join(" ", lhsType);
    }

    private void writeOr(String rega, String regb, instructionType opType)
    {
        this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeRegInstruction("or", rega, rega, this.ctx.getTopReg(), instructionType.INT) + "\n");
    }

    @Override
    public String visitInclusiveOrExpression(cParser.InclusiveOrExpressionContext ctx)
    {
        if (ctx.getChildCount() == 1)
            return visit(ctx.getChild(0));
        
        String[] lhsType = visit(ctx.lhs).split("\\s+");
        instructionType lType = (lhsType[lhsType.length-1].equals("double")) ? instructionType.DOUBLE : (lhsType[lhsType.length-1].equals("float") ? instructionType.FLOAT : instructionType.INT);
        unloadCheckpoint(lhsType, this.ctx.getCurrentFunction().getId(), this.ctx.getTopReg(), lType);
        String lReg = this.ctx.getTopReg();

        String[] rhsType = visit(ctx.rhs).split("\\s+"); // returns rtype
        instructionType rType = (rhsType[rhsType.length-1].equals("double")) ? instructionType.DOUBLE : (rhsType[rhsType.length-1].equals("float") ? instructionType.FLOAT : instructionType.INT);
        unloadCheckpoint(rhsType, this.ctx.getCurrentFunction().getId(), this.ctx.getTopReg(), rType);
        lType = getPrecedentType(lType, rType);
        if (lType == instructionType.FLOAT || lType == instructionType.DOUBLE)
            throw new RuntimeException("Error: Cannot perform bitwise and on float or double");
        
        writeOr(lReg, this.ctx.getTopReg(), rType);

        this.ctx.clearTopOfStack();

        return "Ored " + String.join(" ", lhsType);
    }
    
    private void writeLogicalUnload(String reg, instructionType opType)
    {
        switch (opType) {
            case DOUBLE:
            {
                /* After putting tmpFreg on our stackl looks liek this
                 *  -> tmpFreg
                 *     reg (freg)
                 */   
                String tmpFreg = this.ctx.getReg("t", "double", true); 
                if (tmpFreg == null)
                    tmpFreg = this.ctx.getReg("a", "double", false);
                if (tmpFreg == null)
                    tmpFreg = this.ctx.getReg("s", "double", false);
                
                this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeCvtsInstruction("fcvt.d.w", tmpFreg, "x0") + "\n");
                writeEq(reg, tmpFreg, "double", false); 
                /* writeEq performs a register swap since it writes into an integer register: Stack looks like this
                 *  -> tmpFreg (freg)
                 *     resultInt Reg (reg)
                 * So we remove the tmp freg to return the register stack to its original state where only reg was on
                 */

                this.ctx.clearTopOfStack();
            }
            break;
            case FLOAT:
            {
                /* After putting tmpFreg on our stack looks liek this
                 *  -> tmpFreg
                 *     Lreg (freg)  
                 */   
                String tmpFreg = this.ctx.getReg("t", "float", true); 
                if (tmpFreg == null)
                    tmpFreg = this.ctx.getReg("a", "float", false);
                if (tmpFreg == null)
                    tmpFreg = this.ctx.getReg("s", "float", false);
                
                this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeCvtsInstruction("fmv.w.x", tmpFreg, "zero") + "\n");
                writeEq(reg, tmpFreg, "float", false); 
                /* writeEq performs a register swap since it writes into an integer register: Stack looks like this
                 *  -> tmpFreg (freg)
                 *     resultInt Reg (reg)
                 */
                this.ctx.clearTopOfStack();
            }
            break;  
            default:
            {

            }
            break;
        }
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
        instructionType lType = (lhsType[lhsType.length-1].equals("double")) ? instructionType.DOUBLE : (lhsType[lhsType.length-1].equals("float") ? instructionType.FLOAT : instructionType.INT);
        unloadCheckpoint(lhsType, this.ctx.getCurrentFunction().getId(), this.ctx.getTopReg(), lType);
        if (lhsType[lhsType.length - 1].equals("double") || lhsType[lhsType.length - 1].equals("float"))
            writeLogicalUnload(this.ctx.getTopReg(), lType);
        
        String lReg = this.ctx.getTopReg();

        String[] rhsType = visit(ctx.rhs).split("\\s+"); // returns rtype
        instructionType rType = (rhsType[rhsType.length-1].equals("double")) ? instructionType.DOUBLE : (rhsType[rhsType.length-1].equals("float") ? instructionType.FLOAT : instructionType.INT);
        unloadCheckpoint(rhsType, this.ctx.getCurrentFunction().getId(), this.ctx.getTopReg(), rType);
        if (rhsType[rhsType.length - 1].equals("double") || rhsType[rhsType.length - 1].equals("float"))
            writeLogicalUnload(this.ctx.getTopReg(), rType);

        writeLogicalAnd(lReg, this.ctx.getTopReg());

        this.ctx.clearTopOfStack();
        lhsType[lhsType.length-1] = (lhsType[lhsType.length-1].equals("float") || lhsType[lhsType.length-1].equals("double")) ? "int" : lhsType[lhsType.length-1]; // change the type of the lhs to the precedent type
        return "LogicalAnded " + String.join(" ", lhsType);
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
        instructionType lType = (lhsType[lhsType.length-1].equals("double")) ? instructionType.DOUBLE : (lhsType[lhsType.length-1].equals("float") ? instructionType.FLOAT : instructionType.INT);
        unloadCheckpoint(lhsType, this.ctx.getCurrentFunction().getId(), this.ctx.getTopReg(), lType);
        if (lhsType[lhsType.length - 1].equals("double") || lhsType[lhsType.length - 1].equals("float"))
            writeLogicalUnload(this.ctx.getTopReg(), lType);
        
        String lReg = this.ctx.getTopReg();

        String[] rhsType = visit(ctx.rhs).split("\\s+"); // returns rtype
        instructionType rType = (rhsType[rhsType.length-1].equals("double")) ? instructionType.DOUBLE : (rhsType[rhsType.length-1].equals("float") ? instructionType.FLOAT : instructionType.INT);
        unloadCheckpoint(rhsType, this.ctx.getCurrentFunction().getId(), this.ctx.getTopReg(), rType);
        if (rhsType[rhsType.length - 1].equals("double") || rhsType[rhsType.length - 1].equals("float"))
            writeLogicalUnload(this.ctx.getTopReg(), rType);
        
        writeLogicalOr(lReg, this.ctx.getTopReg());

        this.ctx.clearTopOfStack();
        lhsType[lhsType.length-1] = (lhsType[lhsType.length-1].equals("float") || lhsType[lhsType.length-1].equals("double")) ? "int" : lhsType[lhsType.length-1]; // change the type of the lhs to the precedent type
        
        return "LogicalOred " + String.join(" ", lhsType);
    }

    private void writeAssign(String funcId, String rhsReg, String type, String instr, String lhsReg)
    {
        switch (type) 
        {
            case "float":
            {
                String tmpReg = this.ctx.getReg("t", type, false);
                this.ctx.writeBodyString(funcId, writeSwLwInstruction("lw", tmpReg, "0", lhsReg, instructionType.FLOAT) + "\n");
                this.ctx.writeBodyString(funcId, writeRegInstruction(instr, tmpReg, tmpReg, rhsReg, instructionType.FLOAT) + "\n");
                this.ctx.clearReg(tmpReg);   
            }
            break;
            case "double":
            {
                String tmpReg = this.ctx.getReg("t", type, false);
                this.ctx.writeBodyString(funcId, writeSwLwInstruction("l", tmpReg, "0", lhsReg, instructionType.DOUBLE) + "\n");
                this.ctx.writeBodyString(funcId, writeRegInstruction(instr, tmpReg, tmpReg, rhsReg, instructionType.DOUBLE) + "\n");
                this.ctx.clearReg(tmpReg);   
            }
            break;
            case "int":
            {
                String tmpReg = this.ctx.getReg("t", type, false);
                this.ctx.writeBodyString(funcId, writeSwLwInstruction("lw", tmpReg, "0", lhsReg, instructionType.INT) + "\n");
                this.ctx.writeBodyString(funcId, writeRegInstruction(instr, tmpReg, tmpReg, rhsReg, instructionType.INT) + "\n");
                this.ctx.clearReg(tmpReg);
            }
            break;
            default: // char, unsigned by default
            {

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
        instructionType rType = (rhsType[rhsType.length-1].equals("double")) ? instructionType.DOUBLE : (rhsType[rhsType.length-1].equals("float") ? instructionType.FLOAT : instructionType.INT);

        unloadCheckpoint(rhsType, this.ctx.getCurrentFunction().getId(), rhsReg, rType);       
        // now top reg holds a value
        String[] lhsType = visit(ctx.leftHandSide).split("\\s+"); // get the pointer to the left hand side, a new register is put onto the stack
        instructionType lType = (lhsType[lhsType.length-1].equals("double")) ? instructionType.DOUBLE : (lhsType[lhsType.length-1].equals("float") ? instructionType.FLOAT : instructionType.INT);
        String funcId = this.ctx.getCurrentFunction().getId();

        /*
         * Dangerous Operation here: Register switch to reduce # of dead registers produced when casting a type
         */

        String lreg = this.ctx.popRegisterStack(); // pop the lhs register, still marked as used however

        if (lhsType[lhsType.length - 1] != rhsType[rhsType.length - 1])
            castType(funcId, rhsReg, lhsType[lhsType.length - 1], rhsType[rhsType.length - 1]); // now RHSreg is top of the stack

        rhsReg = this.ctx.getTopReg(); // update rhsReg to the new register on the top of the stack if it was changed
        // once the type is casted correctly, we can put lreg back on the stack
        this.ctx.pushRegisterStack(lreg); // push the lhs register back onto the stack

        String op = ctx.assOp.getText(); // get the operator
        switch (op)  // TODO: If statemenets for types within the cases
        {
            case "=":
            {
                if ((lhsType[lhsType.length-1] == "double"))
                {
                    this.ctx.writeBodyString(funcId, writeSwLwInstruction("s", rhsReg, "0", this.ctx.getTopReg(), lType) + "\n"); // IMPORTANT DONT LEAVE LIKE THIS, NEED TO HANDLE TYPES HERE // TODO: handle types here
                    break;
                }

                this.ctx.writeBodyString(funcId, writeSwLwInstruction("sw", rhsReg, "0", this.ctx.getTopReg(), lType) + "\n"); // IMPORTANT DONT LEAVE LIKE THIS, NEED TO HANDLE TYPES HERE // TODO: handle types here
            }
            break;
            case "*=":
            {
                writeAssign(funcId, rhsReg, lhsType[lhsType.length - 1], "mul", this.ctx.getTopReg());
            }
            break;
            case "/=":
            {
                writeAssign(funcId, rhsReg, lhsType[lhsType.length - 1], "div", this.ctx.getTopReg());
            }
            break;
            case "%=":
            {
                writeAssign(funcId, rhsReg, lhsType[lhsType.length - 1], "rem", this.ctx.getTopReg());
            }
            break;
            case "+=":
            {
                writeAssign(funcId, rhsReg, lhsType[lhsType.length - 1], "add", this.ctx.getTopReg());
            }
            break;
            case "-=":
            {
                writeAssign(funcId, rhsReg, lhsType[lhsType.length - 1], "sub", this.ctx.getTopReg());
            }
            break;
            case "<<=":
            {
                writeAssign(funcId, rhsReg, lhsType[lhsType.length - 1], "sll", this.ctx.getTopReg());
            }
            break;
            case ">>=":
            {
                writeAssign(funcId, rhsReg, lhsType[lhsType.length - 1], "sra", this.ctx.getTopReg());
            }
            break;
            case "&=":
            {
                writeAssign(funcId, rhsReg, lhsType[lhsType.length - 1], "and", this.ctx.getTopReg());
            }
            break;
            case "^=":
            {
                writeAssign(funcId, rhsReg, lhsType[lhsType.length - 1], "xor", this.ctx.getTopReg());
            }
            break;
            case "|=":
            {
                writeAssign(funcId, rhsReg, lhsType[lhsType.length - 1], "or", this.ctx.getTopReg());
            }
            break;
        }

        this.ctx.clearStack(verbose);
        if (verbose) System.out.printf("#########################################    Assignment Expression END #####################################\n");

        return "Assigned " + String.join(" ",lhsType);
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
        if (isFor && ctx.forInit != null) visit(ctx.forInit);
        this.ctx.writeBodyString(funcId, beginLabel + ":\n");
        String[] condType = visit((isFor) ? ctx.forCond : ctx.whileCond).split("\\s+"); // compiles into a register       
        instructionType type = (condType[condType.length-1].equals("double")) ? instructionType.DOUBLE : (condType[condType.length-1].equals("float") ? instructionType.FLOAT : instructionType.INT);
        unloadCheckpoint(condType, funcId, this.ctx.getTopReg(), type);

        if (verbose)
        {
            System.out.printf("#########################################    For2 Loop    #########################################\n");
            System.out.printf("Condition Type: %s\n", (condType.length >= 2) ? condType[0] : condType[condType.length-1]);
            System.out.printf("#########################################    For2 Loop END     #####################################\n");
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

        if (ctx.While() != null)
        {
            compileIteration(ctx, "WHILE", false);
        }
        else if (ctx.Do() != null)
        {
            // TODO: implement do while loop
        }
        else if (ctx.for1_token != null || ctx.for2_token != null)
        {
            compileIteration(ctx, "FOR", true);
        }

        this.ctx.popFromBreakStack();
        this.ctx.popFromContinueStack();

        return "";
    }

    private void compileIteration(cParser.IterationStatementContext ctx, String labelName, boolean isFor) {
        String beginLabel;
        String endLabel;
        beginLabel = this.ctx.makeUnqiueLabel(labelName);
        endLabel = this.ctx.makeUnqiueLabel("END" + labelName);
        this.ctx.pushToBreakStack(endLabel);
        this.ctx.pushToContinueStack(beginLabel); 
        writeIteration(ctx, beginLabel, endLabel, isFor);
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
        instructionType type = (condType[condType.length-1].equals("double")) ? instructionType.DOUBLE : (condType[condType.length-1].equals("float") ? instructionType.FLOAT : instructionType.INT);
        unloadCheckpoint(condType, funcId, this.ctx.getTopReg(), type);
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
        instructionType type = (condType[condType.length-1].equals("double")) ? instructionType.DOUBLE : (condType[condType.length-1].equals("float") ? instructionType.FLOAT : instructionType.INT);
        unloadCheckpoint(condType, funcId, this.ctx.getTopReg(), type);
        String endIfLabel = this.ctx.makeUnqiueLabel("ENDIF");
        String elseLabel = this.ctx.makeUnqiueLabel("ELSE");
        switch (condType[condType.length-1]) { // shoof il ta3aseh **Face Palm**
            case "float":
            {
                // TODO: implement float if with else
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
                // make a label and unload the '1' value from the heap
                String incDecLabel = this.ctx.makeUnqiueLabel("INCDEC");
                this.heapString += String.format("%s: .word %d\n",incDecLabel, Float.floatToIntBits( (float) incDec));
                String incDecReg = this.ctx.getReg("t", "int", false);
                incDecReg = (incDecReg  == null) ? this.ctx.getReg("a", "int", false) : incDecReg;
                incDecReg = (incDecReg  == null) ? this.ctx.getReg("s", "int", false) : incDecReg; // Kinda illegal but were gna store s regs                 
                this.ctx.writeBodyString(funcId, writeSwLwInstruction("lui", incDecReg, "%hi", incDecLabel, instructionType.INT) + "\n");
                writeAddition(incDecReg, "%lo(" + incDecLabel + ")", "int");

                // get a tmp register to store the 1 value in it
                String tmpReg = this.ctx.getReg("t", type, false); // at the top of stack and put them back when we implement recursion
                tmpReg = (tmpReg  == null) ? this.ctx.getReg("a", type, false) : tmpReg;
                tmpReg = (tmpReg  == null) ? this.ctx.getReg("s", type, false) : tmpReg; // Kinda illegal but were gna store s regs

                // write the actual addition and clear the tmp register which had the 1 in it
                this.ctx.writeBodyString(funcId, writeSwLwInstruction("lw", tmpReg, "0", incDecReg, instructionType.FLOAT) + "\n");
                writeAddition(tmpReg, this.ctx.getTopReg(), "float");

                visit(ctx.primaryExpression()); // get the location agian

                this.ctx.writeBodyString(funcId, writeSwLwInstruction("s", tmpReg, "0", this.ctx.getTopReg(), instructionType.FLOAT) + "\n");
                this.ctx.clearTopOfStack(); // clears the pointer to the location
                this.ctx.clearReg(incDecReg); // clears the pointer to the location
                this.ctx.clearReg(tmpReg); // clears the tmp register
                
                return "incDec " + type;          
            }
            case "double":
            {

                // make a label and unload the '1' value from the heap
                String incDecLabel = this.ctx.makeUnqiueLabel("INCDEC");
                this.heapString += String.format("%s: .word %d\n",incDecLabel, Float.floatToIntBits( (float) incDec));
                String incDecReg = this.ctx.getReg("t", "int", false);
                incDecReg = (incDecReg  == null) ? this.ctx.getReg("a", "int", false) : incDecReg;
                incDecReg = (incDecReg  == null) ? this.ctx.getReg("s", "int", false) : incDecReg; // Kinda illegal but were gna store s regs                 
                this.ctx.writeBodyString(funcId, writeSwLwInstruction("lui", incDecReg, "%hi", incDecLabel, instructionType.INT) + "\n");
                writeAddition(incDecReg, "%lo(" + incDecLabel + ")", "int");

                // get a tmp register to store the 1 value in it
                String tmpReg = this.ctx.getReg("t", type, false);                                            // at the top of stack and put them back when we implement recursion
                tmpReg = (tmpReg  == null) ? this.ctx.getReg("a", type, false) : tmpReg;
                tmpReg = (tmpReg  == null) ? this.ctx.getReg("s", type, false) : tmpReg; // Kinda illegal but were gna store s regs

                // write the actual addition and clear the tmp register which had the 1 in it
                this.ctx.writeBodyString(funcId, writeSwLwInstruction("l", tmpReg, "0", incDecReg, instructionType.DOUBLE) + "\n");
                writeAddition(tmpReg, this.ctx.getTopReg(), "double");

                visit(ctx.primaryExpression()); // get the location agian

                this.ctx.writeBodyString(funcId, writeSwLwInstruction("s", tmpReg, "0", this.ctx.getTopReg(), instructionType.DOUBLE) + "\n");
                this.ctx.clearTopOfStack(); // clears the pointer to the location
                this.ctx.clearReg(incDecReg); // clears the pointer to the location
                this.ctx.clearReg(tmpReg); // clears the tmp register
                
                return "incDec " + type;

            }
            default: // default int, char, unsigned; 
            {
                // get a tmp register to store the addition into it
                String tmpReg = this.ctx.getReg("t", type, false);                                            // at the top of stack and put them back when we implement recursion
                tmpReg = (tmpReg  == null) ? this.ctx.getReg("a", type, false) : tmpReg;
                tmpReg = (tmpReg  == null) ? this.ctx.getReg("s", type, false) : tmpReg; // Kinda illegal but were gna store s regs

                // perform the addition
                this.ctx.writeBodyString(funcId, writeImmediateInstruction("addi", tmpReg, this.ctx.getTopReg(), incDec, instructionType.INT) + "\n");

                visit(ctx.primaryExpression()); // get the location agian
                
                this.ctx.writeBodyString(funcId, writeSwLwInstruction("sw", tmpReg, "0", this.ctx.getTopReg(), instructionType.INT) + "\n");
                this.ctx.clearTopOfStack(); // clears the pointer to the location
                this.ctx.clearReg(tmpReg); // clears the tmp register
                return "incDec " + type;
            }
        }

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
            String[] primaryExprType = visit(ctx.primaryExpression()).split("\\s+");
            instructionType type = (primaryExprType[primaryExprType.length-1].equals("double")) ? instructionType.DOUBLE : (primaryExprType[primaryExprType.length-1].equals("float") ? instructionType.FLOAT : instructionType.INT);

            unloadCheckpoint(primaryExprType, funcId, this.ctx.getTopReg(), type);
            return writeIncDecPostfixExpression(ctx, primaryExprType[primaryExprType.length-1], funcId, (ctx.incOp != null) ? 1 : -1); // value loaded
        }
        else // function calls by default
        {
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
                        this.ctx.getReg("t", type, true); // put the return value in a register
                        this.ctx.writeBodyString(funcId, writeMvInstruction(this.ctx.getTopReg(), "fa0", instructionType.FLOAT) + "\n"); // move the data out of a0 and return
                    }
                    break;
                    case "double":
                    {
                        this.ctx.getReg("t", type, true); // put the return value in a register
                        this.ctx.writeBodyString(funcId, writeMvInstruction(this.ctx.getTopReg(), "fa0", instructionType.DOUBLE) + "\n"); // move the data out of a0 and return
                    }
                    break;
                    // case "unsigned": // not 100% sure about this TODO: check this again
                    // {
                    //     // TODO: implement unsigned function call No args
                    // }
                    // break;
                    // case "char":
                    // {
                    //     // TODO: implement char function call No args
                    // }
                    // break;
                    default: // default int, char, unsigned CHECK THIS AGAIN
                    {
                        this.ctx.getReg("t", type, true); // put the return value in a register
                        this.ctx.writeBodyString(funcId, writeMvInstruction(this.ctx.getTopReg(), "a0", instructionType.INT) + "\n"); // move the data out of a0 and return
                    }
                    break;
                }
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
    //////////////////        UnaryExpressions        /////////////////////
    ///////////////////////////////////////////////////////////////////////

    private String writeIncDecUnaryExpression(cParser.UnaryExpressionContext ctx, int incDec)
    {
        String funcId = this.ctx.getCurrentFunction().getId();
        String[] primaryExprType = visit(ctx.postfixExpression()).split("\\s+");
        instructionType insType = (primaryExprType[primaryExprType.length-1].equals("double")) ? instructionType.DOUBLE : (primaryExprType[primaryExprType.length-1].equals("float") ? instructionType.FLOAT : instructionType.INT);
        unloadCheckpoint(primaryExprType, funcId, this.ctx.getTopReg(), insType); // unloaded the value

        switch (insType) {
            case DOUBLE: // TODO: implement double unary
            case FLOAT: // TODO: implement float unary
            default: // int default 
            {

                this.ctx.writeBodyString(funcId, writeImmediateInstruction("addi", this.ctx.getTopReg(), this.ctx.getTopReg(), incDec, instructionType.INT) + "\n");
                String tmpReg = this.ctx.getTopReg();
                this.ctx.clearTopOfStack(); // clears the pointer to the location
                visit(ctx.postfixExpression()); // get the location agian
                this.ctx.writeBodyString(funcId, writeSwLwInstruction("sw", tmpReg, "0", this.ctx.getTopReg(), instructionType.INT) + "\n");
                return "incDec " + primaryExprType[primaryExprType.length-1];
            }
        }

    }

    private void writeNeg(cParser.UnaryExpressionContext ctx, String rega, instructionType type)
    {
        if (verbose)
        {
            System.out.printf("#########################################    Neg    #########################################\n");
            System.out.printf("Neg : %s\n", ctx.getText());
            this.ctx.safeRegStackPrint();
            System.out.printf("#########################################    Neg END   !#####################################\n");
        }
        switch(type)
        {
            case DOUBLE: this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeNegInstruction("fneg.d", rega, rega) + "\n"); break;
            case FLOAT: this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeNegInstruction("fneg.s", rega, rega) + "\n"); break;
            default: this.ctx.writeBodyString(this.ctx.getCurrentFunction().getId(), writeNegInstruction("neg", rega, rega) + "\n"); break;
        }
    }


    @Override
    public String visitUnaryExpression(cParser.UnaryExpressionContext ctx)
    {
        if (ctx.getChildCount() == 1)
            return visit(ctx.getChild(0));

        if (ctx.incOp != null)
        {
            return writeIncDecUnaryExpression(ctx, 1);
        } 
        else if (ctx.decOp != null)
        {
            return writeIncDecUnaryExpression(ctx, -1);
        }
        else if (ctx.sizeOfOp != null)
        {
            // TODO: implement sizeof
        }
        String op = ctx.unaryOperator().getText();

        if (op == null)
            return visit(ctx.getChild(0));

        String funcId = this.ctx.getCurrentFunction().getId();
        String [] typeArr = visit(ctx.castExpression()).split("\\s+");
        instructionType type = (typeArr[typeArr.length-1].equals("double")) ? instructionType.DOUBLE : (typeArr[typeArr.length-1].equals("float") ? instructionType.FLOAT : instructionType.INT);

        switch (op) {
            case "&": break; // TODO: Implement when doing pointers
            case "*": break; // TODO: Implement when doing pointers
            case "+": break;
            case "-": 
            {
                unloadCheckpoint(typeArr, funcId, this.ctx.getTopReg(), type);
                writeNeg(ctx, this.ctx.getTopReg(), type);
                return "neg " + String.join(" ", typeArr);
            }
            case "~": 
            {
                if (type == instructionType.DOUBLE || type == instructionType.FLOAT)
                    throw new RuntimeException("Cannot use ~ on float or double");
                unloadCheckpoint(typeArr, funcId, this.ctx.getTopReg(), type);
                this.ctx.writeBodyString(funcId, writeNegInstruction("not", this.ctx.getTopReg(), this.ctx.getTopReg()) + "\n");
            }
            break;
            case "!": break;
            default: break;
        }

        return visit(ctx.getChild(0));
    }

    ///////////////////////////////////////////////////////////////////////
    //////////////////        UnaryExpressions END    /////////////////////
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
            out = String.format("%s%s\n", out, writeImmediateInstruction("addi", "sp", "sp", -offset, instructionType.INT));
            out = String.format("%s%s\n", out, writeSwLwInstruction("sw", "ra", String.valueOf(offset - 4), "sp", instructionType.INT));
            out = String.format("%s%s\n", out, writeSwLwInstruction("sw", "s0", String.valueOf(offset - 8), "sp", instructionType.INT));
            out = String.format("%s%s\n", out, writeImmediateInstruction("addi", "s0", "sp", offset, instructionType.INT));
        }
        return out;
    }

    private String variableParameterWrite(Variable var)
    {
        switch (var.getType()) {
            case "float":
            {
                String reg = this.ctx.getReg("a", var.getType(), false);
                
                if (reg.equals("-1"))
                {
                    reg = this.ctx.getReg("a", "int", false);
                }

                return String.format("%s", writeSwLwInstruction("sw", reg, String.valueOf(var.getOffset()), "s0", instructionType.FLOAT)); // change s0 to sp when implementing stack all regs are full

            }
            case "double":
            {
                String reg = this.ctx.getReg("a", var.getType(), false);
                if (reg.equals("-1"))
                {
                    // TODO: Implement doubles when all regs are full
                }
                return String.format("%s", writeSwLwInstruction("s", reg, String.valueOf(var.getOffset()), "s0", instructionType.DOUBLE)); // change s0 to sp when implementing stack all regs are full
            }
            // case "char": // TODO: CHECK THIS WITH THE CALLIGN CONVENTION AND ABI, I THINK IT SHOULD BE FINE
            // {
            //     // TODO: implement char parameter write
            // }
            // break;
            // case "unsigned":
            // {
            //     // TODO: implement unsigned parameter write
            // }
            // break;
            default: // int, char, unsigned default
            {
                String reg = this.ctx.getReg("a", var.getType(), false); // dont put on stack
                

                if (reg.equals("-1"))
                {
                    // TODO: implement parameter write when all int registers are full
                }
                
                return String.format("%s", writeSwLwInstruction("sw", reg, String.valueOf(var.getOffset()), "s0", instructionType.INT)); // change s0 to sp when implementing stack all regs are full

            }
            // break; 
        }
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
            out = String.format("%s%s\n", out, writeSwLwInstruction("lw", "ra", String.valueOf(offset - 4), "sp", instructionType.INT));
            out = String.format("%s%s\n", out, writeSwLwInstruction("lw", "s0", String.valueOf(offset - 8), "sp", instructionType.INT));
            out = String.format("%s%s\n", out, writeImmediateInstruction("addi", "sp", "sp", offset, instructionType.INT));
            out = String.format("%s%s\n", out, "jr ra");
        }
        return out;
    }        

    private String writeImmediateInstruction(String instruction, String registera, String registerb, int immediate, instructionType instrType)
    {
        return String.format("%s %s, %s, %d", instruction, registera, registerb, immediate);  //default is int
    }

    private String writeSwLwInstruction(String instruction, String registera, String immediate, String registerb, instructionType instrType)
    {
        switch (instrType) {
            case DOUBLE: return String.format("f%sd %s, %s(%s)", instruction, registera, immediate, registerb);
            case FLOAT: return String.format("f%s %s, %s(%s)", instruction, registera, immediate, registerb);
            default: return String.format("%s %s, %s(%s)", instruction, registera, immediate, registerb); //default is int
        }
    }

    private String writeLiInstruction(String reg, String immediate)
    {
        return String.format("li %s, %s", reg, immediate);
    }

    private String writeMvInstruction(String regdst, String regsrc, instructionType instrType)
    {
        switch (instrType) {
            case DOUBLE: return String.format("fmv.d %s, %s", regdst, regsrc);
            case FLOAT: return String.format("fmv.s %s, %s", regdst, regsrc);
            default: return String.format("mv %s, %s", regdst, regsrc); //default is int
        }
    }

    private String writeRegInstruction(String instruction, String dst, String registera, String registerb, instructionType instrType)
    {
        switch (instrType) {
            case DOUBLE: String.format("f%s.d %s, %s, %s", instruction, dst, registera, registerb);
            case FLOAT: String.format("f%s.s %s, %s, %s", instruction, dst, registera, registerb);
            default: return String.format("%s %s, %s, %s", instruction, dst, registera, registerb); //default is int
        }
    }

    private String writeBranchInstruction(String instruction, String regA, String regB, String label)
    {
        return String.format("%s %s, %s, %s", instruction, regA, regB, label);
    }

    private String writeSeqzInstruction(String instruction, String rega, String regb)
    {
        return String.format("%s %s, %s", instruction, rega, rega);
    }

    private String writeCvtsInstruction(String instruction, String dstReg, String rega)
    {
        return String.format("%s %s, %s", instruction, dstReg, rega);
    }

    private String writeNegInstruction(String instruction, String dstReg, String rega)
    {
        return String.format("%s %s, %s", instruction, dstReg, rega);
    }

    private String writeCvtsRTZInstruction(String instruction, String dstReg, String rega)
    {
        return String.format("%s %s, %s, rtz", instruction, dstReg, rega);
    }
    
    // FUNCTIONS SHOULD BE IDEALLY CALLED WHEN THE REGISTERS INPUT ARE AT THE TOP OF THE STACK
    private void castType(String funcId, String reg, String targetType, String inputType) // split into two parts, getting the new register and writing the correct instructions
    {
        String replacedReg = reg;
        if ((inputType.equals("char") || inputType.equals("unsigned"))  && (targetType.equals("float") || targetType.equals("double")))
        {
            replacedReg = getReplacementReg(reg, targetType);
            this.ctx.writeBodyString(funcId, writeCvtsInstruction("fcvt."+ (targetType.equals("double") ? "d" : "s") +".wu", replacedReg, reg) + "\n");
        }
        else if (inputType.equals("int") && (targetType.equals("float") || targetType.equals("double")))
        {
            replacedReg = getReplacementReg(reg, targetType);
            this.ctx.writeBodyString(funcId, writeCvtsInstruction("fcvt."+ (targetType.equals("double") ? "d" : "s") +".w", replacedReg, reg) + "\n");
        }
        else if ((inputType.equals("float") || inputType.equals("double")) && (targetType.equals("char") || targetType.equals("unsigned")))
        {
            replacedReg = getReplacementReg(reg, targetType);
            this.ctx.writeBodyString(funcId, writeCvtsRTZInstruction("fcvt.wu."+ (inputType.equals("double") ? "d" : "s"), replacedReg, reg) + "\n");
        }
        else if ((inputType.equals("float") || inputType.equals("double")) && targetType.equals("int"))
        {
            replacedReg = getReplacementReg(reg, targetType);
            this.ctx.writeBodyString(funcId, writeCvtsRTZInstruction("fcvt.w."+ (inputType.equals("double") ? "d" : "s"), replacedReg, reg) + "\n");
        }
        else if (inputType.equals("float") && targetType.equals("double"))
        {
            this.ctx.writeBodyString(funcId, writeCvtsInstruction("fcvt.d.s", replacedReg, reg) + "\n");
        }
        else if (inputType.equals("double") && targetType.equals("float"))
        {
            replacedReg = getReplacementReg(reg, targetType);
            this.ctx.writeBodyString(funcId, writeCvtsInstruction("fcvt.s.d", replacedReg, reg) + "\n");
        }
        else
        {
            if (verbose) System.out.printf("castType: no casting needed\n");
        }

        if (targetType.equals("char"))
            this.ctx.writeBodyString(funcId, writeImmediateInstruction("andi", replacedReg, replacedReg, 0xff, instructionType.INT) + "\n");

    }

    private String getReplacementReg(String reg, String targetType) 
    {
        // for debugging purposes
        try {
            assert(reg.equals(this.ctx.getTopReg()));
        } catch (Exception e) {
            System.out.printf("dead reg created: %s, topReg: %s\n", reg, this.ctx.getTopReg());
            System.exit(1);
        }

        if (reg.equals(this.ctx.getTopReg()))
            this.ctx.clearTopOfStack();

        String newReg = this.ctx.getReg("a", targetType, true);        
        
        if (newReg == null)
            newReg = this.ctx.getReg("t", targetType, true);
        
        if (newReg == null) // kinda illegal but will be alowed since al sregs will be stored on the stack at the begining of each function call
            newReg = this.ctx.getReg("s", targetType, true);

        if (newReg == null)
            throw new RuntimeException("castType: newReg is null, cant find a new reg");
        

        
        return newReg;
    }
    
    // FUNCTIONS SHOULD BE IDEALLY CALLED WHEN THE REGISTERS INPUT ARE AT THE TOP OF THE STACK
    private String unloadCheckpoint(String [] arr, String funcId, String reg, instructionType instrType)
    {
        if (arr.length != 1) // i dont need to unload
            return String.format("%s", arr[arr.length-1]);
        
        switch (instrType) 
        {
            case INT:
                this.ctx.writeBodyString(funcId, writeSwLwInstruction("lw", reg, "0", reg, instrType) + "\n");
                break;
            default: 
                String tmpReg = getReplacementReg(reg, arr[arr.length-1]);
                this.ctx.writeBodyString(funcId, writeSwLwInstruction(instrType == instructionType.DOUBLE ? "l" : "lw", tmpReg, "0", reg, instrType) + "\n");
                break;

        }
        return arr[arr.length-1];
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


    /**
     * @return boolean return the verbose
     */
    public boolean isVerbose() {
        return verbose;
    }

    /**
     * @param verbose the verbose to set
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

}
