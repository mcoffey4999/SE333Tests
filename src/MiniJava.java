import Scanner.*;
import Parser.parser;
import Parser.sym;
import java_cup.runtime.Symbol;
import java_cup.runtime.ComplexSymbolFactory;
import java.io.*;
import java.util.List;

import AST.Program;
import AST.Statement;
import AST.Visitor.PrettyPrintVisitor;
import AST.Visitor.*;
import Symtab.*;


public class MiniJava {
    public static void main(String [] args) {
    	    int status = 0;
    	    String help = "Use:\n" +
    	                  " java MiniJava -S <sorce_file>\n" +
    	    		      " java MiniJava -P <source_file>\n" +
    	                  " java MiniJava -T <source_file>\n" +
    	    		      " java MiniJava -A <source_file>\n" +
    	                  " java MiniJava -C <source_file>\n";
    	    if(args.length == 1 && (args[0].equals("-h") || args[0].equals("-H")) ) {
    	    	System.out.println(help);
    	    }
        	if(args.length == 2 && args[0] == "-S") {
        			status = RunScanner(args[1]);
        	}
        	else if(args.length == 2 && args[0] == "-P") {
        			status = RunParser(args[1]);
        		}
        	else if(args.length == 2 && args[0] == "-T") {
        		status = SymbolTableGen(args[1]); 
        	}
        	else if(args.length == 2 && args[0] == "-A") {
        		status = SemanticAnalysis(args[1]);
        	}
        	else if(args.length == 2 && args[0] == "-C") {
        		status = CodeGenerator(args[1]);
        	}
        	else {
        		System.err.println("Invalid program arguments. \nUse: java Main -P <source_file>");
        		status = 1;
        	}
        	System.exit(status);
    }
    public static int RunScanner(String file){
    	int errors = 0;
    	try {
    	ComplexSymbolFactory sf = new ComplexSymbolFactory();
	       Reader in = new BufferedReader(new FileReader(file));
	       scanner s = new scanner(in, sf);
	       Symbol t = s.next_token();
	       while (t.sym != sym.EOF){ 
	    	   if(t.sym == sym.error) ++errors;
	           System.out.print(s.symbolToString(t) + " ");
	           t = s.next_token();
	       }
	       System.out.print("\nLexical analysis completed");
	       System.out.print(errors + " errors were found.");
    	}catch (Exception e) {
    		System.err.println("Unexpected internal compiler error: " + 
                    e.toString());
    		e.printStackTrace();
    	}
    	return errors == 0 ? 0 : 1;
    }
    public static int RunParser(String file) {
    	int errors = 0;
    	try {
            ComplexSymbolFactory sf = new ComplexSymbolFactory();
            Reader in = new BufferedReader(new FileReader(file));
            scanner s = new scanner(in, sf);
            parser p = new parser(s, sf);
            Symbol root;
            root = p.parse();
            if(root.sym == sym.error) ++errors;
            Program program = (Program)root.value;
            if(program != null) {
            	program.accept(new PrettyPrintVisitor());
            	System.out.print("\nParsing completed");
                System.out.print(errors + " errors were found.");
            }
            else {
            	System.out.print("\nParsing failed");
                System.out.print(errors + " errors were found.");
            }
        } catch (Exception e) {
            System.err.println("Unexpected internal compiler error: " + 
                               e.toString());
            e.printStackTrace();
        }
    	return errors == 0 ? 0 : 1;
    }
    public static int SymbolTableGen(String file) {
    	int errors = 0;
    	try {
            ComplexSymbolFactory sf = new ComplexSymbolFactory();
            Reader in = new BufferedReader(new FileReader(file));
            scanner s = new scanner(in, sf);
            parser p = new parser(s, sf);
            Symbol root;
            root = p.parse();
            if(root.sym == sym.error) ++errors;
            Program program = (Program)root.value;
            SymTableVisitor st = new SymTableVisitor();
            program.accept(st);
            st.print();
            System.out.print("\nParsing completed");
            System.out.print(errors + " errors were found.");
        } catch (Exception e) {
            System.err.println("Unexpected internal compiler error: " + 
                               e.toString());
            e.printStackTrace();
        }
    	return errors == 0 ? 0 : 1;
    }
    public static int SemanticAnalysis(String file) {
    	int errors = 0;
    	try {
            ComplexSymbolFactory sf = new ComplexSymbolFactory();
            Reader in = new BufferedReader(new FileReader(file));
            scanner s = new scanner(in, sf);
            parser p = new parser(s, sf);
            Symbol root;
            root = p.parse();
            if(root.sym == sym.error) ++errors;
            Program program = (Program)root.value;
            SymTableVisitor st = new SymTableVisitor();
            program.accept(st);
            errors = st.errors; 
            {
	            SemanticAnalysisVisitor sa = new SemanticAnalysisVisitor();
	            sa.setSymtab(st.getSymtab());
	            program.accept( sa );
	            errors += sa.errors;
            }
            
	        System.out.println("\nCompiler completed"); 
	        System.out.println(errors + " errors were found.");	    
	    } catch (Exception e) {
	        System.err.println("Unexpected internal compiler error: " + 
	                    e.toString());
	        e.printStackTrace();
	    }
	    
	    return errors == 0 ? 0 : 1;    
}
    
    public static int CodeGenerator(String file) {
    	int errors = 0;
    	try {
            ComplexSymbolFactory sf = new ComplexSymbolFactory();
            Reader in = new BufferedReader(new FileReader(file));
            scanner s = new scanner(in, sf);
            parser p = new parser(s, sf);
            Symbol root;
            root = p.parse();
            if(root.sym == sym.error) ++errors;
            Program program = (Program)root.value;
            SymTableVisitor st = new SymTableVisitor();
            program.accept(st);
            errors = st.errors;
            CodeTranslateVisitor va = new CodeTranslateVisitor();
            program.accept(va);
            errors += va.errors;
	        System.out.println("\nCompiler completed"); 
	        System.out.println(errors + " errors were found.");	    
	    } catch (Exception e) {
	        System.err.println("Unexpected internal compiler error: " + 
	                    e.toString());
	        e.printStackTrace();
	    }
    	return errors == 0 ? 0 : 1;
    }
}
