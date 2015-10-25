package edu.temple.cis.c4324.microcompilerv1;

/**
 * Class to represent Array Types.
 * @author Paul
 */
public class ArrayType implements Type {
    
    private final Type componentType;
    private final int length;
    
    public ArrayType(Type componentType, int length) {
        this.componentType = componentType;
        this.length = length;
    }
    
    /**
     * Return the equivalent Java type name
     * @return The component Java Type followed by "[]"
     */
    @Override
    public String getJavaTypeName() {
        return componentType.getJavaTypeName() + "[]";
    }
    
    public Type getComponentType() {
        return componentType;
    }
    
}
