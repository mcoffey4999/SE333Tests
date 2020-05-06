package AST;

import AST.Visitor.Visitor;
import AST.Visitor.ObjectVisitor;
import java_cup.runtime.ComplexSymbolFactory.Location;

public class ArrayLookup extends Exp {
  public Exp e1,e2;
  
  public ArrayLookup(Exp ae1, Exp ae2, Location pos) {
    super(pos);
    e1=ae1; e2=ae2;
  }

  public void accept(Visitor v) {
    v.visit(this);
  }
  
  public Object accept(ObjectVisitor v) {
	    return v.visit(this);
	  }
}
