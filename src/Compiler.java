// import grammar.*;
import grammar.*;
import grammar.cLexer;
import grammar.cParser;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.NoSuchFileException;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

public class Compiler extends cBaseVisitor<String>
{
    FileWriter writer;


    public String visitCompilationUnit(cParser.CompilationUnitContext ctx) {
        return visitChildren(ctx);
    }


    public static void main(String[] args) throws IOException, NoSuchFileException 
    {

        Compiler compiler = new Compiler();
        CharStream input = null;
                
        // try to open the file, complain if u cant find it
        try {
            if (args[0].equals("-S"))
                input =  CharStreams.fromFileName(args[1]);
            else 
                throw new Exception("Check the syntax of the input, -S not found");

            if (args[2].equals("-o"))
                compiler.writer = new FileWriter(args[3]);
            else
                throw new Exception("Check the syntax of the input, -o not found");

            } catch (Exception e) {
            System.err.printf("Yo where the file @ ?\n%s\n", e.toString());
        }

        System.out.printf("%s\n", input.toString());

        cLexer lexer = new cLexer(input);
        CommonTokenStream tkSteam = new CommonTokenStream(lexer);
        cParser parser = new cParser(tkSteam);
        ParseTree tree = parser.compilationUnit();
        
        // compiler.visit(tree); // generate Code


        // For Testing purposes  // all working
        // compiler.writer.write(".text\n");
        // compiler.writer.write(".globl f\n");
        // compiler.writer.write("\n");
        // compiler.writer.write("f:\n");
        // compiler.writer.write("addi  t0, zero, 0\n");
        // compiler.writer.write("addi  t0, t0,   5\n");
        // compiler.writer.write("add   a0, zero, t0\n");
        // compiler.writer.write("ret\n");

        compiler.writer.close();
        
        System.out.printf("Testing...\n");
    }
    
}
