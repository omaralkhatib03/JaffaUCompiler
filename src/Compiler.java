import context.*;
import grammar.*;
import grammar.cLexer;
import grammar.cParser;
import symbols.CommonSymbol;
import symbols.Scope;
import symbols.Variable;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.Map;


import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;


public class Compiler extends cBaseVisitor<String>
{
    public static final Map<String, Integer> typeSizeMap = Map.of(
        "char", 1,
        "short", 2,
        "int", 4,
        "long", 8,
        "float", 4,
        "double", 8,
        "long double", 16
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
        String reg = this.ctx.getReg("t", symbol.getType(), true);
        visit(ctx.assignmentExpression()); // compile into the register
        storeSymbol(symbol, reg);
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

    private CommonSymbol declareVariable(String id, cParser.DeclaratorContext ctx, boolean addToQueue)
    {
        Variable var = new Variable(id, this.ctx.getDeclarationMode(), this.ctx.getCurrentFunction().getCurrentOffset()); // create variable
        this.ctx.getCurrentFunction().decrementOffset(typeSizeMap.get(this.ctx.getDeclarationMode())); // decrement offset by size of variable
        if (addToQueue)
            this.ctx.addInitializer(var); // add variable to initializer queue
        return var;
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

        if (ctx.directDeclarator().funcCall != null && ctx.directDeclarator().param != null) // if there is a function call, we need to add it to the type
            throw new RuntimeException("Error: Cannot declare function call");

        // declare the symbol accordingly
        CommonSymbol symbol = null;
        
        if (ctx.pointer() != null) // if there is a pointer, we need to add it to the type
        {
            // TODO: declare pointer
        }
        else if (ctx.directDeclarator().structDecl != null)
        {
            // TODO: declare struct
        }
        else if (ctx.directDeclarator().arrayDecl != null)
        {
            // TODO: declare array
        } 
        else if (ctx.directDeclarator().varDecl != null)
        {
            symbol = declareVariable(id, ctx, true);
        }
        else
        {
            // throw new RuntimeException("Error: Cannot declare symbol, check syntax");
            return "";
        }

        // System.out.printf("Declared: %s", id);

        // add declared symbol to the correct scope
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
        
        Variable symbol = new Variable(id, this.ctx.getDeclarationMode(), this.ctx.getCurrentFunction().getCurrentOffset()); // create variable
        this.ctx.getCurrentFunction().decrementOffset(typeSizeMap.get(this.ctx.getDeclarationMode())); // decrement offset by size of variable

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
            symbol = declareVariable(id, declarator, false);
        }
        else
        {
            // throw new RuntimeException("Error: Cannot prepare paramter symbol, check syntax");
            return "";
        }
        
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
        String functionName = "";
        String functionType = getType(ctx.declarationSpecifiers());
        
        if (!typeSizeMap.keySet().contains(functionType))
            throw new RuntimeException("Error: Invalid function type");

        try {
            functionName = ctx.declarator().directDeclarator().directDeclarator().Identifier().getText();              
        } catch (Exception e) {
            System.err.printf("Error: Cannot find function Id\n%s\n", e.toString());
        }

        String functionHeaderString = String.format(".text\n.globl %s\n\n%s:\n", functionName, functionName);
        String functionBodyString = "";
        
        if (this.verbose)
        {
            System.out.printf("###################### Function Declaration ################### \n");
            System.out.printf("Function Name: %s\n", functionName);
            System.out.printf("Return Type: %s\n", functionType);
            // System.out.printf("Function Header: \n%s\n", functionHeaderString);
            // System.out.printf("Function Body: \n%s\n", functionBodyString);
        }

        this.ctx.addScope(functionName, functionType, true);
        this.ctx.headerStringsMap.put(functionName, functionHeaderString);
        this.ctx.bodyStringsMap.put(functionName, functionBodyString);
        // get parameters ready
        if (ctx.declarator().directDeclarator().param != null)
            prepareParameter(ctx.declarator().directDeclarator().param);
        if (this.verbose) System.out.printf("###################### Function Declaration END ################### \n");
        visit(ctx.compoundStatement());        
        return "";
    }

    @Override
    public String visitCompilationUnit(cParser.CompilationUnitContext ctx)
    {
        visitChildren(ctx);
        if (this.verbose)
        {
            System.out.printf("###################### Compilation Unit ################### \n");
            printFunctionSymbolTable();
            System.out.printf("###################### Compilation Unit END ################### \n");
            this.ctx.printRegMaps();
            this.ctx.printRegisterStackStatus();
            this.ctx.printAllSymbolTables();
        }

        try {
            for (String i : this.ctx.headerStringsMap.keySet()) 
            {
                String stackDecStr = writeStackDecrement(this.ctx.getFunctionOffset(i));
                String stackIncStr = writeStackIncrement(this.ctx.getFunctionOffset(i));
                writer.write(this.ctx.headerStringsMap.get(i) + stackDecStr);
                writer.write(this.ctx.bodyStringsMap.get(i) + stackIncStr);
                writer.write("\n");
            }    

            writer.close();

        } catch (Exception e) {
            System.err.printf("Error: Cannot write to output file\n%s\n", e.toString());
            return"";
        }        

        return  "";
    }



    ///////////////////////////////////////////////////////////////////////
    //////////////////         Jump Statements       //////////////////////
    ///////////////////////////////////////////////////////////////////////
    
    private void intReturn(String valueReg, cParser.JumpStatementContext ctx)
    {
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
            System.out.printf("###################### Jump Statement END ################### \n");
        }


        switch (jType) {
            case "return":
            {
                this.ctx.setUsed("a0", true); // set a0 as used and push it on the stack 
                String exprReg = this.ctx.getReg("a", this.ctx.getCurrentFunction().getType(), true); // get another reg and push it on the stack
                this.ctx.setFunctionReturn(this.ctx.getCurrentFunction().getId(), true);
                visit(ctx.expression());
                this.ctx.setFunctionReturn(this.ctx.getCurrentFunction().getId(), false);
                this.ctx.clearTopOfStack();
                switch (this.ctx.getCurrentFunction().getType()) {
                    case "double":
                    {
                        // TODO: implement double return
                    }
                    break;
                    case "float":
                    {
                        floatReturn(exprReg, ctx);
                    }
                    break;
                    case "char":
                    {
                        // TODO: implement char return
                    }
                    break;
                    case "unsigned":
                    {
                        // TODO: implement unsigned return
                    }
                    break;
                    default: // int 
                    {
                        intReturn(exprReg, ctx);
                    }
                    break;
                }
                this.ctx.clearTopOfStack(); // clears a0
            }
            break;
            case "break":
            {
                // TODO: implement break
            }
            break;
            case "continue":
            {
                // TODO: implement continue
            }
            break;
            default: // TODO: goto jump instruction
            break;
        }
        
        return "";
    }

    ///////////////////////////////////////////////////////////////////////
    //////////////////       Jump Statements END      /////////////////////
    ///////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////
    //////////////////      Primary EXPRESSIONS      //////////////////////
    ///////////////////////////////////////////////////////////////////////

    public void primaryExpressionConstant(cParser.PrimaryExpressionContext ctx)
    {
        Scope currentFunction = this.ctx.getCurrentFunction();
        String currentFunctionBody = this.ctx.bodyStringsMap.get(currentFunction.getId());
        String out = "";
        switch (currentFunction.getType()) {
            case "float":
            {
                // TODO: implement float constant
            }
            break;
            case "double":
            {
                // TODO: implement double constant
            }
            break;
            case "char":
            {
                // TODO: implement char constant
            }
            break;
            case "unsigned":
            {
                // TODO: implement unsigned constant
            }
            break;
            default: // int default
            {
                out = String.format("%s\n", writeLiInstruction(this.ctx.getTopReg(), ctx.Constant().getText()));
            }
            break;
        }
        this.ctx.bodyStringsMap.put(currentFunction.getId(), currentFunctionBody + out);
    }

    @Override
    public String visitPrimaryExpression(cParser.PrimaryExpressionContext ctx)
    {
        if (ctx.Identifier() != null)
        {
            // TODO: implement primary expression identifier
        }
        else if (ctx.Constant() != null)
        {
            primaryExpressionConstant(ctx);
            return "Constant: Done!\n";
        }
        else if (ctx.StringLiteral() != null)
        {
            // TODO: implement primary expression string literal
        }
        else if (ctx.expression() != null)
        {
            // TODO: implement primary expression expression
        }
        return "";
    }

    ///////////////////////////////////////////////////////////////////////
    //////////////////      Primary EXPRESSIONS END     ///////////////////
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
    

    public void printFunctionSymbolTable()
    {
        this.ctx.printFunctionSymbolTable();
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

    private String writeStackIncrement(int offset)
    {
        String out = "";
        if (offset > 2032)
        {
            ; // TODO: handle stack increment when offset is greater than 2032
        }
        else
        {   
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
