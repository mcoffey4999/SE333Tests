package AST;

import AST.Visitor.Visitor;
import AST.Visitor.ObjectVisitor;
import java_cup.runtime.ComplexSymbolFactory.Location;

public class Display extends Statement {
  public Exp e;

  public Display(Exp re, Location pos) {
    super(pos);
    e=re; 
  }
  
  public void accept(Visitor v) {
    v.visit(this);
  }
  
  public Object accept(ObjectVisitor v) {
	    return v.visit(this);
	  }
}

