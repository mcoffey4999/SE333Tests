package AST.Visitor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.sun.xml.internal.ws.wsdl.writer.document.Binding;

import AST.*;
import Symtab.*;

public class CodeTranslateVisitor implements Visitor {

	SymbolTable st = null;
	private HashMap<VarSymbol, Integer> stable = new HashMap<VarSymbol, Integer>();
	public int errors = 0;
	int tabs = 0;
	public int stack_pos = 0;
	public int arg_pos = 0;
	int l_num = 0;
	public String c_regs[] = {"%rdi", "%rsi", "%rdx", "%rcx", "%r8", "%r9"};
	public SymbolTable stack_table;
	private HashMap<String, Integer> lab = new HashMap<String, Integer>();

	public void setSymtab(SymbolTable s) { st = s; }
	 
	public SymbolTable getSymtab() { return st; }
	
	public Integer getSymLoc(VarSymbol vs) {
		return stable.get(vs);
	}
	
	public void setSymLoc(VarSymbol vs, int loc) {
		stable.put(vs, new Integer(loc));
	}
	
	public void incTab() {
		tabs += 1;
	}
	
	public void decTab() {
		tabs -= 1;
		if(tabs < 0) tabs = 0;
	}
	
	public void printTabs() {
		String spaces = "";
		for(int i = 0; i < tabs * 8; i++) {
			spaces += " ";
		}
		System.out.println(spaces);
	}
	public String getType(Type t) {
		if(t == null) return "";
		else if(t instanceof IntArrayType) {
			return "Int[]";
		}
		else if(t instanceof BooleanType) {
			return "Boolean";
		}
		else if(t instanceof IntegerType) {
			return "Int";
		}
		else if(t instanceof IdentifierType) {
			return ((IdentifierType)t).s;
		}
		else return "";
	}
	
	public int objectSize(String t, boolean ic) {
		if(t == null || t == "") {
			return 0;
		}
		else if(t.equals("Int") || t.equals("Int[]") || t.equals("Boolean")
				|| t.equals("boolean") || ic) {
			return 8;
		}
		int size = 8;
		ClassSymbol cs = (ClassSymbol)st.lookupSymbol(t, "ClassSymbol");
		if(cs != null && cs.getVariables() != null) {
			List<VarSymbol> vl = cs.getVariables();
			for(int i = 0; i < vl.size(); i++) {
				size += objectSize(vl.get(i).getType(), true);
			}
		}
		return size;
	}
	
	public void printIn(String ins, String[] args) {
		printTabs();
		System.out.print(ins);
		int len = 8 - ins.length();
		if(len > 0) {
			for(int j = 0; j < len; j++) {
				System.out.print(" ");
			}
		}
		for(int l = 0; l < args.length; l++) {
			if(l > 0) System.out.println(", ");
			System.out.print(args[l]);
		}
		System.out.print("\n");
	}
	
	public void printIn(String ins) {
		printTabs();
		System.out.println(ins);
	}
	
	public void report_error(int line, String msg) {
		System.out.println(line + ": " + msg);
		++errors;
	}
	
	public String getLabel(Identifier i) {
		return getLabel("", i.s);
	}
	
	public String getLabel(String class_name, String call_name) {
		String label = !class_name.isEmpty() ? class_name+"$"+call_name : call_name;
		String os = System.getProperty("os.name");
		if ( os.contains("Windows") || os.contains("OS X") ) {
			return "_"+label;
		}
		return label;
	}
	
	public String getExpr(ASTNode n, boolean dst) {
		if ( n == null ) {
			return "";
		}
		else if ( n instanceof IntegerLiteral ) {
			IntegerLiteral i = (IntegerLiteral)n;
			i.accept(this);
			return "%rax";
		}
		else if ( n instanceof True ) {
			True i = (True)n;
			i.accept(this);
			return "%rax";
		}
		else if ( n instanceof False ) {
			False i = (False)n;
			i.accept(this);
			return "%rax";
		}
		else if ( n instanceof IdentifierExp ) {
			IdentifierExp i = (IdentifierExp)n;
			Symbol s = st.getSymbol(i.s);
			if(s != null && s instanceof VarSymbol) {
				VarSymbol vs = (VarSymbol)s;
				String operand = getSymLoc(vs) + "(%rbp)";
				if(dst) return operand;
				printIn("movq", new String[] {operand, "%rax"});
				return "%rax";
			}
			s = st.lookupSymbol(i.s, "VarSymbol");
			if(s != null && s instanceof VarSymbol) {
				VarSymbol vs = (VarSymbol)s;
				printIn("movq", new String[] {"-8(%rbp)", "%rax"});
				String operand = getSymLoc(vs) + "(%rax)";
				if(dst) return operand;
				printIn("movq", new String[] {operand, "%rax"});
				return "%rax";
			}
			ASTNode p = n;
			while(p != null && !(p instanceof ClassDecl)) {
				p = p.getParent();
			}
			if(p != null && p instanceof ClassDeclExtends) {
				ClassDeclExtends c = (ClassDeclExtends)p;
				ClassSymbol cs = (ClassSymbol)st.lookupSymbol(c.j.s);
				if(cs != null) {
					VarSymbol vs = cs.getVariable(i.s);
					if(vs != null) {
						printIn("movq", new String[] {"-8(%rbp)", "%rax"});
						String operand = getSymLoc(vs) + "(%rax)";
						if(dst) return operand;
						printIn("movq", new String[] {operand, "%rax"});
						return "%rax";
					}
				}
			}
		}
		else if ( n instanceof Identifier ) {
			Identifier i = (Identifier)n;
			Symbol s = st.getSymbol(i.s);
			if(s != null && s instanceof VarSymbol) {
				VarSymbol vs = (VarSymbol)s;
				String operand = getSymLoc(vs) + "(%rbp)";
				if(dst) return operand;
				printIn("movq", new String[] {operand, "%rax"});
				return "%rax";
			}
			s = st.lookupSymbol(i.s, "VarSymbol");
			if(s != null && s instanceof VarSymbol) {
				VarSymbol vs = (VarSymbol)s;
				printIn("movq", new String[] {"-8(%rbp)", "%rax"});
				String operand = getSymLoc(vs) + "(%rax)";
				if(dst) return operand;
				printIn("movq", new String[] {operand, "%rax"});
				return "%rax";
			}
			ASTNode p = n;
			while(p != null && !(p instanceof ClassDecl)) {
				p = p.getParent();
			}
			if(p != null && p instanceof ClassDeclExtends) {
				ClassDeclExtends c = (ClassDeclExtends)p;
				ClassSymbol cs = (ClassSymbol)st.lookupSymbol(c.j.s);
				if(cs != null) {
					VarSymbol vs = cs.getVariable(i.s);
					if(vs != null) {
						printIn("movq", new String[] {"-8(%rbp)", "%rax"});
						String operand = getSymLoc(vs) + "(%rax)";
						if(dst) return operand;
						printIn("movq", new String[] {operand, "%rax"});
						return "%rax";
					}
				}
			}
		}
		else if ( n instanceof ArrayLookup ) {
			ArrayLookup e = (ArrayLookup)n;
			e.accept(this);
			return "%rax";
		}
		else if ( n instanceof Exp ) {
			Exp e = (Exp)n;
			e.accept(this);
			return "%rax";
		}
		report_error( n.line_number, "Undefined expression.");
		return "";
	}

	// Display added for toy example language. Not used in regular MiniJava
	public void visit(Display n) {
		n.e.accept(this);
	}

	// MainClass m;
	// ClassDeclList cl;
	public void visit(Program n) {
		if(n.cl != null) {
			System.out.println(".data");
			for (int i = 0; i < n.cl.size(); i++) {
				ClassDecl c = n.cl.get(i);
				if(c instanceof ClassDeclSimple) {
					ClassDeclSimple cs = (ClassDeclSimple)c;
					System.out.println(getLabel("",cs.i.s) + "$:");
					incTab();
					printTabs();
					System.out.println(".quad 0");
					ClassSymbol cy = (ClassSymbol)getSymtab().lookupSymbol(cs.i.s, "ClassSymbol");
					if(cy != null) {
						for(int m = 0; m < cy.getMethods().size(); m++) {
							printTabs();
							System.out.println(".quad" + " "+ getLabel(cs.i.s, cy.getMethods().get(m).getName()));
						}
					}
					decTab();
				}
				else if(c instanceof ClassDeclExtends) {
					ClassDeclExtends ce = (ClassDeclExtends)c;
					System.out.println(getLabel("", ce.i.s) + "$:");
					incTab();
					printTabs();
					System.out.println(".quad " + getLabel(ce.j.s, ""));
					ClassSymbol cs = (ClassSymbol)getSymtab().lookupSymbol(ce.i.s, "ClassSymbol");
					if(cs != null) {
						for(int o = 0; o < cs.getMethods().size(); o++) {
							MethodSymbol ms = cs.getMethods().get(o);
							ClassSymbol pcs = (ClassSymbol)ms.getParameters().get(o);
							String cn = pcs != null ? pcs.getName() : "";
							printTabs();
							System.out.println(".quad" + " " + getLabel(cn, cs.getMethods().get(o).getName()));
							
						}
					}
					decTab();
					
				}
			}
			System.out.println("");
			System.out.println(".text");
			System.out.println(".globl " + getLabel("", "asm_main"));
			n.m.accept(this);
			for(int q = 0; q < n.cl.size(); q++) {
				n.cl.get(q).accept(this);
			}
		}
	}

	// Identifier i1,i2;
	// Statement s;
	public void visit(MainClass n) {
		n.i1.accept(this);
		st = st.findScope(n.i1.toString());
		n.i2.accept(this);
		st = st.findScope("main");
		stack_pos = 0;
		arg_pos = 0;
		System.out.println("");
		System.out.println( getLabel("", "asm_main") + ":" );
		incTab();
		printIn("pushq", new String[] {"%rbp"});
		printIn("movq", new String[] {"%rsp", "rbp"});
		n.s.accept(this);
		printIn("leave");
		printIn("ret");
		decTab();
		st = st.exitScope();
		st = st.exitScope();
	}

	// Identifier i;
	// VarDeclList vl;
	// MethodDeclList ml;
	public void visit(ClassDeclSimple n) {
		n.i.accept(this);
		st = st.findScope(n.i.s);
		stack_pos = 0;
		arg_pos = 0;
		for (int i = 0; i < n.vl.size(); i++) {
			n.vl.get(i).accept(this);
		}
		for (int i = 0; i < n.ml.size(); i++) {
			n.ml.get(i).accept(this);
		}
		st = st.exitScope();
	}

	// Identifier i;
	// Identifier j;
	// VarDeclList vl;
	// MethodDeclList ml;
	public void visit(ClassDeclExtends n) {
		n.i.accept(this);
		n.j.accept(this);
		st = st.findScope(n.i.toString());
		stack_pos = 0;
		arg_pos = 0;
		for (int i = 0; i < n.vl.size(); i++) {
			n.vl.get(i).accept(this);
		}
		for (int i = 0; i < n.ml.size(); i++) {
			n.ml.get(i).accept(this);
		}
		st = st.exitScope();
	}

	// Type t;
	// Identifier i;
	public void visit(VarDecl n) {
		if(n.i == null) return;
		Symbol s = st.getVarTable().get(n.i.s);
		if(s != null && s instanceof VarSymbol) {
			VarSymbol vs = (VarSymbol)s;
			stack_pos -= 8;
			setSymLoc(vs, stack_pos);
		}
	}

	// Type t;
	// Identifier i;
	// FormalList fl;
	// VarDeclList vl;
	// StatementList sl;
	// Exp e;
	public void visit(MethodDecl n) {
		ASTNode p = st.getScope();
		st = st.findScope(n.i.toString());
		stack_pos = 0;
		arg_pos = 0;
		String fname = n.i.s;
		String cname = "";
		if(p != null && p instanceof ClassDeclSimple) {
			ClassDeclSimple c = (ClassDeclSimple)p;
			cname = c.i.s;
		}
		else if(p != null && p instanceof ClassDeclExtends) {
			ClassDeclExtends ce = (ClassDeclExtends)p;
			cname = ce.i.s;
		}
		System.out.println(getLabel(cname, fname) + ":");
		incTab();
		printIn("pushq", new String[] {"%rbp"});
		printIn("movq", new String[] {"%rsp", "%rbp"});
		int ssize = 8 * (n.fl.size() + n.vl.size() + 1);
		if(ssize > 0) {
			ssize += (ssize % 16);
			printIn("subq", new String[] {"$"+Integer.toString(ssize), "%rsp"});
		}
		stack_pos -= 8;
		String sloc = Integer.toString(stack_pos) + "(%rbp)";
		printIn("movq", new String[] {c_regs[arg_pos++], sloc});
		for(int i = 0; i < n.fl.size() && i < 5; i++) {
			n.fl.get(i).accept(this);
		}
		for(int j = 0; j < n.vl.size(); j++) {
			n.vl.get(j).accept(this);
		}
		for(int l = 0; l < n.sl.size(); l++) {
			n.sl.get(l).accept(this);
		}
		if(n.e != null) {
			String ret = getExpr(n.e, false);
			if(ret != "%rax") {
				printIn("movq", new String[] {ret, "%rax"});
			}
		}
		st = st.exitScope();
		printIn("leave");
		printIn("ret");
		decTab();
	}

	// Type t;
	// Identifier i;
	public void visit(Formal n) {
		if(n.i == null) return;
		Symbol sm = st.lookupSymbol(n.i.s);
		if(sm != null && sm instanceof VarSymbol) {
			VarSymbol vs = (VarSymbol)sm;
			String stack_loc = Integer.toString(stack_pos)+ "(%rbp)";
			stack_pos -= 8;
			stack_table.addSymbol(vs);
			printIn("movq", new String[] {c_regs[arg_pos++], stack_loc});
		}
	}

	public void visit(IntArrayType n) {
	}

	public void visit(BooleanType n) {
	}

	public void visit(IntegerType n) {
	}

	// String s;
	public void visit(IdentifierType n) {
	}

	// StatementList sl;
	public void visit(Block n) {
		for (int i = 0; i < n.sl.size(); i++) {
			n.sl.get(i).accept(this);
		}
	}

	// Exp e;
	// Statement s1,s2;
	public void visit(If n) {
		String lab = getLabel("", "L"+l_num++);
		String c = getExpr(n.e, false);
		printIn("cmp", new String[] {"$0", c});
		printIn("je", new String[] {lab});
		n.s1.accept(this);
		if(n.s2 != null) {
			String lab2 = getLabel("", "L"+l_num++);
			printIn("jmp", new String[] {lab2});
			System.out.println(lab+":");
			n.s2.accept(this);
			lab = lab2;
			
		}
		System.out.println(lab +":");
	}

	// Exp e;
	// Statement s;
	public void visit(While n) {
		String lab = getLabel("", "L"+l_num++);
		String lab2 = getLabel("", "L"+l_num++);
		System.out.println(lab +":");
		String c = getExpr(n.e, false);
		printIn("cmp", new String[] {"$0", c});
		printIn("je", new String[] {lab2});
		n.s.accept(this);
		printIn("jmp", new String[] {lab});
		System.out.println(lab2+":");
	}

	// Exp e;
	public void visit(Print n) {
		String operand = getExpr(n.e, false);
		printIn("movq", new String[] {operand, "%rdi"});
		printIn("call", new String[] {getLabel("", "put")});
	}

	// Identifier i;
	// Exp e;
	public void visit(Assign n) {
		String e = getExpr(n.e, false);
		printIn("pushq", new String[] {e});
		String i = getExpr(n.i, true);
		printIn("popq", new String[] {"%r10"});
		printIn("movq", new String[] {"%r10", i});
	}

	// Identifier i;
	// Exp e1,e2;
	public void visit(ArrayAssign n) {
		String v = getExpr(n.e2, false);
		printIn("pushq", new String[] {v});
		String a = getExpr(n.i, true);
		printIn("pushq", new String[] {a});
		String i = getExpr(n.e1, false);
		printIn("popq", new String[] {"%r10"});
		printIn("popq", new String[] {"%rdx"});
		printIn("movq", new String[] {"%rdx", "16(%r10,"+i+",8)"});
	}

	// Exp e1,e2;
	public void visit(And n) {
		String o1 = getExpr(n.e1, false);
		printIn("pushq", new String[] {"%rax"});
		String o2 = getExpr(n.e2, false);
		printIn("popq", new String[] {"%r10"});
		o1 = "%r10";
		printIn("andq", new String[] {o1, o2});
	}

	// Exp e1,e2;
	public void visit(LessThan n) {
		String o1 = getExpr(n.e1, false);
		printIn("pushq", new String[] {"%rax"});
		String o2 = getExpr(n.e2, false);
		printIn("popq", new String[] {"%r10"});
		o1 = "%r10";
		printIn("cmpq", new String[] {o1, o2});
		printIn("setg", new String[] {"%al"});
		printIn("movzbq", new String[] {"%al", "%rax"});
	}

	// Exp e1,e2;
	public void visit(Plus n) {
		String o1 = getExpr(n.e1, false);
		printIn("pushq", new String[] {"%rax"});
		String o2 = getExpr(n.e2, false);
		printIn("popq", new String[] {"%r10"});
		o1 = "%r10";
		printIn("addq", new String[] {o1, o2});
	}

	// Exp e1,e2;
	public void visit(Minus n) {
		String o1 = getExpr(n.e1, false);
		printIn("pushq", new String[] {"%rax"});
		String o2 = getExpr(n.e2, false);
		printIn("popq", new String[] {"%r10"});
		o1 = "%r10";
		printIn("subq", new String[] {o1, o2});
	}

	// Exp e1,e2;
	public void visit(Times n) {
		String o1 = getExpr(n.e1, false);
		printIn("pushq", new String[] {"%rax"});
		String o2 = getExpr(n.e2, false);
		printIn("popq", new String[] {"%r10"});
		o1 = "%r10";
		printIn("imulq", new String[] {o1, o2});
	}
	
	// Exp e1,e2;
	public void visit(Divide n) {
		String o1 = getExpr(n.e1, false);
		printIn("pushq", new String[] {"%rax"});
		String o2 = getExpr(n.e2, false);
		if(o2 == "0") {
			report_error(n.line_number, "Cannot divide by 0");
			return;
		}
		printIn("popq", new String[] {"%r10"});
		o1 = "%r10";
		printIn("movq", new String[] {"$"+ o2,"%rbx"});
		printIn("cqto");
		printIn("idivq", new String[] {"%rbx"});
	}

	// Exp e1,e2;
	public void visit(ArrayLookup n) {
		String a = getExpr(n.e1, false);
		printIn("pushq", new String[] {a});
		String i = getExpr(n.e2, false);
		printIn("popq", new String[] {"%r10"});
		printIn("movq", new String[] {"16(%r10,"+i+",8)", "%rax"});
	}

	// Exp e;
	public void visit(ArrayLength n) {
		String a = getExpr(n.e, false);
		printIn("movq", new String[] {"0("+a+")", "%rax"});
	}

	// Exp e;
	// Identifier i;
	// ExpList el;
	public void visit(Call n) {
		String fname = n.i.s;
		String cname = "";
		int moffset = -1;
		String obj = getExpr(n.e, false);
		printIn("pushq", new String[] {obj});
		for(int i = 0; i < n.el.size(); i++) {
			String e = getExpr(n.el.get(i), false);
			printIn("pushq", new String[] {e});
		}
		if(n.e != null) {
			if(n.e instanceof IdentifierExp) {
				IdentifierExp i = (IdentifierExp)n.e;
				Symbol s = st.lookupSymbol(i.s);
				if(s != null && s instanceof ClassSymbol) {
					ClassSymbol cs = (ClassSymbol)s;
					cname = cs.getName();
				}
				else if(s != null && s instanceof VarSymbol) {
					VarSymbol vs = (VarSymbol)s;
					cname = vs.getType();
				}
				else if(s == null) {
					ASTNode p = i;
					while(p != null && !(p instanceof ClassDecl)) {
						p = p.getParent();
					}
					if(p != null && p instanceof ClassDeclExtends) {
						ClassDeclExtends c = (ClassDeclExtends)p;
						ClassSymbol cy = (ClassSymbol)st.lookupSymbol(c.j.s);
						if(cy != null) {
							VarSymbol vy = cy.getVariable(i.s);
							cname = vy.getType();
						}
						
					}
				}
			}
			else if(n.e instanceof Call) {
				Call ca = (Call)n.e;
				MethodSymbol msy = (MethodSymbol)st.lookupSymbol(ca.i.s, "MethodSymbol");
				if(msy != null) cname = msy.getType();
			}
			else if(n.e instanceof This) {
				This t = (This)n.e;
				ASTNode pe = t.getParent();
				while(pe != null && !(pe instanceof ClassDecl))  pe = pe.getParent();
				if(pe != null && pe instanceof ClassDeclSimple) {
					ClassDeclSimple cds = (ClassDeclSimple)pe;
					ClassSymbol csym = (ClassSymbol)st.lookupSymbol(cds.i.s, "ClassSymbol");
					if(csym != null && csym.getMethod(fname) != null) {
						cname = csym.getName();
					}
				}
				else if(pe != null && pe instanceof ClassDeclExtends) {
					ClassDeclExtends cde = (ClassDeclExtends)pe;
					MethodSymbol me = null;
					ClassSymbol cym = (ClassSymbol)st.lookupSymbol(cde.i.s, "ClassSymbol");
					if(cym != null) {
						me = cym.getMethod(fname);
						cname = cym.getName();
					}
					ClassSymbol csym_ext = (ClassSymbol)st.lookupSymbol(cde.j.s, "ClassSymbol");
					if(csym_ext != null) {
						MethodSymbol mse = csym_ext.getMethod(fname);
						if(cname == null || me == mse) {
							cname = csym_ext.getName();
						}
					}
				}
			}
			else if(n.e instanceof NewObject) {
				NewObject o = (NewObject)n.e;
				cname = o.i.s;
			}
		}
		for(int i = n.el.size(); i >= 0; i--) {
			printIn("popq", new String[] {c_regs[i]});
		}
		if(cname != null) {
			ClassSymbol cls = (ClassSymbol)st.lookupSymbol(cname, "ClassSymbol");
			MethodSymbol mes = (cls != null) ? cls.getMethod(fname) : null;
			moffset = (mes != null) ? 8 * cls.getMethods().indexOf(mes) + 8 : -1;
		}
		if(moffset != -1) {
			printIn("movq", new String[] {"0(%rdi)", "%rax"});
			printIn("call", new String[] {"*"+moffset+"(%rax)"});
		}
		else {
			printIn("call", new String[] {getLabel(cname, fname)});
			visit(n);
		}
	}

	// int i;
	public void visit(IntegerLiteral n) {
		String i = "$" + Integer.toString(n.i);
		printIn("movq", new String[] {i, "%rax"});
	}

	public void visit(True n) {
		printIn("movq", new String[] { "$1", "%rax"});
	}

	public void visit(False n) {
		printIn("movq", new String[] {"$0", "%rax"});
	}

	// String s;
	public void visit(IdentifierExp n) {
	}

	public void visit(This n) {
		printIn("movq", new String[] {"-8(%rbp)", "%rax"});
	}

	// Exp e;
	public void visit(NewArray n) {
		String e = getExpr(n.e, false);
		printIn("pushq", new String[] {e});
		printIn("imulq", new String[] {"$8", e});
		printIn("addq", new String[] {"$16", e});
		printIn("movq", new String[] {e, "%rdi"});
		printIn("call", new String[] {getLabel("", "mjcalloc")});
		printIn("popq", new String[] {"0(%rax)"});
		printIn("movq", new String[] {"$16", "8(%rax)"});
	}

	// Identifier i;
	public void visit(NewObject n) {
		String obj = n.i.s;
		int obsize = objectSize(obj, false);
		obsize += (obsize % 16);
		printIn("movq", new String[] {"$"+obsize, "%rdi"});
		printIn("call", new String[] {getLabel("", "mjcalloc")});
		printIn("movabs", new String[] {"$"+getLabel(n.i.s,""), "%rdx"});
		printIn("movq", new String[] {"%rdx", "0(%rax)"});
	}

	// Exp e;
	public void visit(Not n) {
		String o = getExpr(n.e, false);
		printIn("cmpq", new String[] {"$0", o});
		printIn("sete", new String[] {"%al"});
		printIn("movzbq", new String[] {"%al", "%rax"});
	}

	// String s;
	public void visit(Identifier n) {
	}
}
