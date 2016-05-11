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

    String spCode = "";
    String storedClass = null;
    TempCounter tempcounter;

    public CodeGeneration(HashMap<String,String> ClassMap, HashMap<String, SymbolTable> Symboltable,
                          HashMap<String,ArrayList<String>> vVariables, HashMap<String,ArrayList<String>> vMethods) {

        this.ClassMap = ClassMap;
        this.Symboltable = Symboltable;
        this.vVariables = vVariables;
        this.vMethods = vMethods;
    }

    public String getSpCode() {
        return this.spCode;
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
        this.storedClass = "Main Method";

        this.spCode += "MAIN\n";

        for (String key : vMethods.keySet()) {
            System.out.println(key);
            int labeltemp = tempcounter.getTemp();
            int labelallocate = tempcounter.getTemp();
            this.spCode += "MOVE TEMP " + labeltemp + " " + key + "_vTable\n";
            this.spCode += "MOVE TEMP " + labelallocate + " HALLOCATE " + 4*vMethods.get(key).size() + "\n";
            this.spCode += "HSTORE TEMP " + labeltemp + " 0 TEMP " + labelallocate + "\n";

            int offset = 0;
            for (String vmethod : vMethods.get(key)) {
                int methodtemp = tempcounter.getTemp();
                this.spCode += "MOVE TEMP " + methodtemp + " " + vmethod + "\n";
                this.spCode += "HSTORE TEMP " + labelallocate + " " + offset + " TEMP " + methodtemp + "\n";
                offset += 4;
            }
        }

        this.spCode += "END\n";

        System.out.println(this.spCode);
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
