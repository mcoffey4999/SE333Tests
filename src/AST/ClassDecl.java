package AST;

import AST.Visitor.Visitor;
import AST.Visitor.ObjectVisitor;
import java_cup.runtime.ComplexSymbolFactory.Location;

public abstract class ClassDecl extends ASTNode{
  public ClassDecl(Location pos) {
    super(pos);
  }
  public abstract void accept(Visitor v);
  
  public abstract Object accept(ObjectVisitor v);
}
