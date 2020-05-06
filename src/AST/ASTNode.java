package AST;

import AST.Visitor.Visitor;
import java_cup.runtime.ComplexSymbolFactory.Location;

abstract public class ASTNode {
  // Line number in source file.
  public final int line_number;
  private ASTNode parent = null;

  // Constructor
  public ASTNode(Location pos) {
    this.line_number = pos.getLine();
  }
  public int getLineNo() {
	  return line_number;
  }
  
  public void setParent(ASTNode p)  {
	  parent = p;
  }
  
  public ASTNode getParent() {
	  return parent;
  }
}
