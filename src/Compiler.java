import context.*;
import grammar.*;
import symbols.*;
import grammar.cLexer;
import grammar.cParser;

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
    private boolean debug = false;

    public Compiler(boolean debug, String outputPath) throws IOException
    {
        this.debug = debug;
        try {
            writer = new FileWriter(outputPath);        
        } catch (IOException e) {
            System.err.printf("Error: Cannot open output file\n%s\n", e.toString());
        }
    }

    @Override 
    public String visitFunctionDefinition(cParser.FunctionDefinitionContext ctx) 
    { 
        String functionName = "";
        String functionType = ctx.declarationSpecifiers().getText();
        
        if (!typeSizeMap.keySet().contains(functionType))
            throw new RuntimeException("Error: Invalid function type");

        try {
            functionName = ctx.declarator().directDeclarator().directDeclarator().Identifier().getText();              
        } catch (Exception e) {
            System.err.printf("Error: Cannot find function Id\n%s\n", e.toString());
        }

        String functionHeaderString = String.format(".text\n.globl %s\n\n%s:\n", functionName, functionName);
        String functionBodyString = "";
        
        if (this.debug)
        {
            System.out.printf("###################### Function Declaration ################### \n");
            System.out.printf("Function Name: %s\n", functionName);
            System.out.printf("Function Type: %s\n", functionType);
            System.out.printf("Function Header: \n%s\n", functionHeaderString);
            System.out.printf("Function Body: \n%s\n", functionBodyString);
            System.out.printf("###################### Function Declaration END ################### \n");

        }

        this.ctx.addScope(functionName, functionType, true);
        this.ctx.headerStringsMap.put(functionName, functionHeaderString);
        this.ctx.bodyStringsMap.put(functionName, functionBodyString);
        visit(ctx.compoundStatement());

        return "";
    }

    @Override
    public String visitCompilationUnit(cParser.CompilationUnitContext ctx)
    {
        visitChildren(ctx);
        if (this.debug)
        {
            System.out.printf("###################### Compilation Unit ################### \n");
            
            printFunctionSymbolTable();

            System.out.printf("###################### Compilation Unit END ################### \n");

        }

        try {
            // String functionName = this.ctx.headerStringsMap.keySet().iterator().next();
            for (String i : this.ctx.headerStringsMap.keySet()) 
            {
                String stackDecStr = writeStackDecrement(this.ctx.getFunctionOffset(i));
                writer.write(this.ctx.headerStringsMap.get(i) + stackDecStr);
                writer.write(this.ctx.bodyStringsMap.get(i));
                writer.write("\n");
            }    

            writer.close();

        } catch (Exception e) {
            System.err.printf("Error: Cannot write to output file\n%s\n", e.toString());
            return"";
        }        

        return  "";
    }

    @Override
    public String visitJumpStatement(cParser.JumpStatementContext ctx)
    {
        String jType = ctx.jtype.getText();
        
        if (debug)
        {
            System.out.printf("###################### Jump Statement ################### \n");
            System.out.printf("jump type: %s\n", jType);
            System.out.printf("Current function type: %s\n", this.ctx.getCurrentFunction().getType());
            System.out.printf("###################### Jump Statement END ################### \n");
        }


        switch (jType) {
            case "return":
            {
                this.ctx.setUsed("a0", true); // set a0 as used and push it on the stack 
                this.ctx.getReg("a", this.ctx.getCurrentFunction().getType(), true);
                visit(ctx.expression());
            }
            break;
            case "break":
            {

            }
            break;
            case "continue":
            {

            }
            break;
            default: // TODO: goto jump instruction
            break;
        }
        
        return "";
    }


    public static void main(String[] args) throws IOException, NoSuchFileException 
    {
        boolean debug = false;
        String outputPath = "";
        CharStream input = null;

        // try to open the file, complain if u cant find it
        try {
            if (args[0].equals("-S"))
                input =  CharStreams.fromFileName(args[1]);
            else 
                throw new Exception("Check the syntax of the input, -S not found");

            if (args[2].equals("-o"))
                outputPath = args[3];
            else
                throw new Exception("Check the syntax of the input, -o not found");
            
            if (args.length >= 5)
                if (args[4].equals("-d"))
                    debug = true;
                
            } catch (Exception e) {
            System.err.printf("Yo where the file @ ?\n%s\n", e.toString());
        }

        Compiler compiler = new Compiler(debug, outputPath);

        // System.out.printf("%s\n", input.toString());

        cLexer lexer = new cLexer(input);
        CommonTokenStream tkSteam = new CommonTokenStream(lexer);
        cParser parser = new cParser(tkSteam);
        ParseTree tree = parser.compilationUnit();
        
        compiler.visit(tree); // generate Code

        // For Testing purposes  // all working
        // writer.write(".text\n");
        // writer.write(".globl f\n");
        // writer.write("\n");
        // writer.write("f:\n");
        // writer.write("addi  t0, zero, 0\n");
        // writer.write("addi  t0, t0,   5\n");
        // writer.write("add   a0, zero, t0\n");
        // writer.write("ret\n");
        
        System.out.printf("Compiled !\n");
    }
    

    public void printFunctionSymbolTable()
    {
        this.ctx.printFunctionSymbolTable();
    }


    public String writeStackDecrement(int offset)
    {
        String out = "";
        if (offset > 2032)
        {
            ; // TODO: handle this case
        }
        else
        {   
            out = String.format("%s%s\n", out, writeImmediateInstruction("addi", "sp", "sp", -offset));
            out = String.format("%s%s\n", out, writeSwLwInstruction("sw", "s0", offset - 4, "sp"));
            out = String.format("%s%s\n", out, writeImmediateInstruction("addi", "s0", "sp", offset));
        }
        return out;
    }


    public String writeImmediateInstruction(String instruction, String registera, String registerb, int immediate)
    {

        return String.format("%s %s, %s, %d", instruction, registera, registerb, immediate); 
    }

    public String writeSwLwInstruction(String instruction, String registera, int immediate, String registerb)
    {
        return String.format("%s %s, %d(%s)", instruction, registera, immediate, registerb); 
    }
}
