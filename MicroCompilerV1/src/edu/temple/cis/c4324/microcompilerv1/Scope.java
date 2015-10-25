package edu.temple.cis.c4324.microcompilerv1;

import java.util.HashMap;
import java.util.Map;

/**
 * Class to represent a Scope.
 * @author Paul
 */
public class Scope {
    public enum Kind {LOCAL, GLOBAL};
    
    private final Map<String, Identifier> symbolTable;
    private final Kind kind;
    private final Scope parent;
//    private final Identifier owner;
    
    public Scope(Kind kind, Scope parent/*, Identifier owner*/) {
        symbolTable = new HashMap<>();
        this.kind = kind;
        this.parent = parent;
//        this.owner = owner;
    }
    
    public Identifier resolve(String s) {
        Identifier id = symbolTable.get(s);
        if (id != null) return id;
        if (parent != null) return parent.resolve(s);
        return null;
    }
    
    public Kind getKind() {return kind;}
    
//    public Identifier getOwner() {return owner;}
    
    public boolean define(String idName, Type type) {
        if (symbolTable.containsKey(idName)) {
            return false;
        }
        Identifier id = new Identifier(idName, type, this);
        symbolTable.put(idName, id);
        return true;
    }
    
    public Scope getParent() {return parent;}
  
}
