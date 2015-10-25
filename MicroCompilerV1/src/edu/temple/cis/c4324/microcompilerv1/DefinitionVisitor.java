package edu.temple.cis.c4324.microcompilerv1;

import edu.temple.cis.c4324.micro.MicroBaseVisitor;
import edu.temple.cis.c4324.micro.MicroParser;
import edu.temple.cis.c4324.micro.MicroParser.PrimitiveTypeContext;
import edu.temple.cis.c4324.micro.MicroParser.ProgramContext;
import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.tree.ParseTreeProperty;


/**
 * The definition phase visits the parse tree and defines all of the
 * identifiers.
 *
 * @author Paul
 */
public class DefinitionVisitor extends MicroBaseVisitor<Type> {

    private final ParseTreeProperty<Scope> scopeMap;
    private Scope globalScope;
    private Scope currentScope;
    private String programName;

    public DefinitionVisitor() {
        scopeMap = new ParseTreeProperty<>();
    }

    public ParseTreeProperty<Scope> getScopeMap() {
        return scopeMap;
    }

    @Override
    public Type visitProgram(ProgramContext ctx) {
        programName = ctx.ID().getText();
        globalScope = new Scope(Scope.Kind.GLOBAL, null);
        globalScope.define(programName, null);
        currentScope = globalScope;
        scopeMap.put(ctx, currentScope);
        visitChildren(ctx);
        return null;
    }

    @Override
    public Type visitProcedureDeclaration(MicroParser.ProcedureDeclarationContext ctx) {
        String procedureName = ctx.ID().getText();
        currentScope = new Scope(Scope.Kind.LOCAL, currentScope);
        scopeMap.put(ctx, currentScope);
        List<String> parameterNameList = new ArrayList<>();
        List<String> parameterTypeList = new ArrayList<>();
        ctx.parameterList().parameterDeclaration().forEach(pd -> {
            visit(pd);
            String parameterName;
            if (pd instanceof MicroParser.SimpleParamDeclContext) {
                parameterName = ((MicroParser.SimpleParamDeclContext) pd).ID().getText();
            } else {
                parameterName = ((MicroParser.ArrayParamDeclContext) pd).ID().getText();
            }
            parameterNameList.add(parameterName);
            Identifier parameterId = currentScope.resolve(parameterName);
            String parameterType = parameterId.getType().getJavaTypeName();
            parameterTypeList.add(parameterType);
        });
        ProcedureOrFunction procedureType = new ProcedureOrFunction(
                programName + "." + procedureName,
                PrimitiveType.VOID,
                parameterNameList,
                parameterTypeList);
        ctx.variableDeclaration().forEach(vd -> {
            visit(vd);
        });
        currentScope = currentScope.getParent();
        currentScope.define(procedureName, procedureType);
        return null;
    }

    @Override
    public Type visitFunctionDeclaration(MicroParser.FunctionDeclarationContext ctx) {
        String functionName = ctx.ID().getText();
        Type returnType = visit(ctx.type());
        currentScope = new Scope(Scope.Kind.LOCAL, currentScope);
        scopeMap.put(ctx, currentScope);
        List<String> parameterNameList = new ArrayList<>();
        List<String> parameterTypeList = new ArrayList<>();
        ctx.parameterList().parameterDeclaration().forEach(pd -> {
            visit(pd);
            String parameterName;
            if (pd instanceof MicroParser.SimpleParamDeclContext) {
                parameterName = ((MicroParser.SimpleParamDeclContext) pd).ID().getText();
            } else {
                parameterName = ((MicroParser.ArrayParamDeclContext) pd).ID().getText();
            }
            parameterNameList.add(parameterName);
            Identifier parameterId = currentScope.resolve(parameterName);
            String parameterType = parameterId.getType().getJavaTypeName();
            parameterTypeList.add(parameterType);
        });
        ProcedureOrFunction procedureType = new ProcedureOrFunction(
                programName + "." + functionName,
                returnType,
                parameterNameList,
                parameterTypeList);
        ctx.variableDeclaration().forEach(vd -> {
            visit(vd);
        });
        currentScope = currentScope.getParent();
        currentScope.define(functionName, procedureType);
        return null;
    }

    @Override
    public Type visitPrimitiveType(PrimitiveTypeContext ctx) {
        String text = ctx.getText();
        switch (text) {
            case "int":
                return PrimitiveType.INT;
            case "real":
                return PrimitiveType.REAL;
            case "char":
                return PrimitiveType.CHAR;
            case "bool":
                return PrimitiveType.BOOL;
        }
        return null;
    }

    @Override
    public Type visitSimpleVariableDecl(MicroParser.SimpleVariableDeclContext ctx) {
        String idName = ctx.ID().getText();
        Type type = visit(ctx.type());
        if (!currentScope.define(idName, type)) {
            MicroCompilerV1.error(ctx, idName + " is already defined");
        }
        return type;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public Type visitArrayVariableDecl(MicroParser.ArrayVariableDeclContext ctx) {
        String arrayVariableName = ctx.ID().getText();
        Type componentType = visit(ctx.primitiveType());
        int length = Integer.parseInt(ctx.INT().getText());
        ArrayType arrayType = new ArrayType(componentType, length);
        Identifier id = new Identifier(arrayVariableName, arrayType, currentScope);
        currentScope.define(arrayVariableName, arrayType);
        return arrayType;
    }

    @Override
    public Type visitSimpleParamDecl(MicroParser.SimpleParamDeclContext ctx) {
        String idName = ctx.ID().getText();
        Type type = visit(ctx.type());
        if (!currentScope.define(idName, type)) {
            MicroCompilerV1.error(ctx, idName + " is already defined");
        }
        return type;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     * @param ctx The current parse tree node
     * @return The type of this node.
     */
    @Override
    public Type visitArrayParamDecl(MicroParser.ArrayParamDeclContext ctx) {
        String idName = ctx.ID().getText();
        PrimitiveType componentType = (PrimitiveType)visit(ctx.primitiveType());
        ArrayType arrayType = new ArrayType(componentType, 0);
        if (!currentScope.define(idName, arrayType)) {
            MicroCompilerV1.error(ctx, idName + " is already defined");
        }
        return arrayType;
    }

}
