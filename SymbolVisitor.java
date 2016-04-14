import syntaxtree.ClassDeclaration;
import syntaxtree.MainClass;
import visitor.GJDepthFirst;

import SymbolTypes.*;
import java.util.HashMap;

public class SymbolVisitor extends GJDepthFirst<String,String> {
    HashMap<String,String> ClassMap;
    HashMap<String,SymbolTable> symboltable = new HashMap<String,SymbolTable>();


    SymbolTable symbol;
    String className;

    public HashMap<String,SymbolTable> getSymboltable() {
        return symboltable;
    }

    public SymbolVisitor(HashMap<String,String> ClassMap) throws Exception{
        this.ClassMap = ClassMap;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> "public"
     * f4 -> "static"
     * f5 -> "void"
     * f6 -> "main"
     * f7 -> "("
     * f8 -> "String"
     * f9 -> "["
     * f10 -> "]"
     * f11 -> Identifier()
     * f12 -> ")"
     * f13 -> "{"
     * f14 -> ( VarDeclaration() )*
     * f15 -> ( Statement() )*
     * f16 -> "}"
     * f17 -> "}"
     */

    public String visit(MainClass n, String argu) throws Exception {
        // Don't forget to save variables and arguments of main method
        return null;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> ( VarDeclaration() )*
     * f4 -> ( MethodDeclaration() )*
     * f5 -> "}"
     */
    public String visit(ClassDeclaration n, String argu) throws Exception {

        return null;
    }
}
