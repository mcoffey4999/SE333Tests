package AST;

import AST.Visitor.Visitor;
import AST.Visitor.ObjectVisitor;
import java_cup.runtime.ComplexSymbolFactory.Location;

public class Program extends ASTNode {
  public MainClass m;
  public ClassDeclList cl;

  public Program(MainClass am, ClassDeclList acl, Location pos) {
    super(pos);
    m=am; cl=acl; 
  }

  public void accept(Visitor v) {
    v.visit(this);
  }
  
  public Object accept(ObjectVisitor v) {
	    return v.visit(this);
	  }
}
