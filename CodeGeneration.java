import SymbolTypes.Methods;
import SymbolTypes.SymbolTable;
import SymbolTypes.Variables;
import syntaxtree.ClassDeclaration;
import syntaxtree.MainClass;
import visitor.GJDepthFirst;

import java.util.ArrayList;
import java.util.HashMap;

public class CodeGeneration extends GJDepthFirst<String,String> {
    HashMap<String,String> ClassMap;
    HashMap<String, SymbolTable> Symboltable;
    HashMap<String,ArrayList<String>> vVariables;
    HashMap<String,ArrayList<String>> vMethods;

    TempCounter tempcounter;

    public CodeGeneration(HashMap<String,String> ClassMap, HashMap<String, SymbolTable> Symboltable,
                          HashMap<String,ArrayList<String>> vVariables, HashMap<String,ArrayList<String>> vMethods) {

        this.ClassMap = ClassMap;
        this.Symboltable = Symboltable;
        this.vVariables = vVariables;
        this.vMethods = vMethods;
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
        tempcounter = new TempCounter(this.Symboltable);
        tempcounter.maxArgs();
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
        int a = tempcounter.getTemp();
        return null;
    }
}
