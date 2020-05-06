package AST;

import AST.Visitor.Visitor;
import AST.Visitor.ObjectVisitor;
import java_cup.runtime.ComplexSymbolFactory.Location;

public abstract class Type extends ASTNode {
    public Type(Location pos) {
        super(pos);
    }
    public abstract void accept(Visitor v);
    
    public abstract Object accept(ObjectVisitor v);
}
