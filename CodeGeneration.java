import SymbolTypes.Methods;
import SymbolTypes.NamedVariables;
import SymbolTypes.SymbolTable;
import SymbolTypes.Variables;
import syntaxtree.*;
import visitor.GJDepthFirst;

import java.util.ArrayList;
import java.util.HashMap;

public class CodeGeneration extends GJDepthFirst<String,String> {
    HashMap<String,String> ClassMap;
    HashMap<String, SymbolTable> Symboltable;
    HashMap<String,ArrayList<String>> vVariables;
    HashMap<String,ArrayList<String>> vMethods;

    HashMap<String,Integer> tempVariables = new HashMap<String,Integer>();

    String spCode = "";
    boolean thisObject = false;
    String storedClass = null;
    String storedMethod = null;

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

        n.f14.accept(this,null);
        n.f15.accept(this,null);

        this.spCode += "END\n";
        this.spCode += "\n";

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
        this.storedClass = n.f1.f0.toString();
        n.f4.accept(this,storedClass);

        return null;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "extends"
     * f3 -> Identifier()
     * f4 -> "{"
     * f5 -> ( VarDeclaration() )*
     * f6 -> ( MethodDeclaration() )*
     * f7 -> "}"
     */
    public String visit(ClassExtendsDeclaration n, String argu) throws Exception {
        this.storedClass = n.f1.f0.toString();
        n.f6.accept(this,storedClass);

        return null;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     * f2 -> ";"
     */
    public String visit(VarDeclaration n, String argu) throws Exception {
        // ToDo Maybe Edit later depending on the needs
        tempVariables.put(n.f1.f0.toString(),tempcounter.getTemp());

        return null;
    }

    /**
     * f0 -> "public"
     * f1 -> Type()
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( FormalParameterList() )?
     * f5 -> ")"
     * f6 -> "{"
     * f7 -> ( VarDeclaration() )*
     * f8 -> ( Statement() )*
     * f9 -> "return"
     * f10 -> Expression()
     * f11 -> ";"
     * f12 -> "}"
     */
    public String visit(MethodDeclaration n, String storedName) throws Exception {
        this.storedMethod = n.f2.f0.toString();
        tempcounter.resetArgCounter();
        tempVariables.clear();

        tempVariables.put("this", 0);
        ArrayList<NamedVariables> methodArgs = Symboltable.get(this.storedClass).getMethodsMap().get(this.storedMethod).args;
        for (NamedVariables namedVar : methodArgs) {
                tempVariables.put(namedVar.name,tempcounter.getArgTemp());
        }
        this.spCode += this.storedClass + "_" + this.storedMethod + " [" + methodArgs.size() + "]\n";
        n.f7.accept(this,null);

        this.spCode += "BEGIN\n";
        n.f8.accept(this,null);
        String retValue = n.f10.accept(this,null);
        this.spCode += "RETURN\n";
        this.spCode += "\t" + retValue + "\n";
        this.spCode += "END\n";

        return null;
    }

    /**
     * f0 -> "int"
     * f1 -> "["
     * f2 -> "]"
     */
    public String visit(ArrayType n, String argu) throws Exception {
        return "int[]";
    }

    /**
     * f0 -> "boolean"
     */
    public String visit(BooleanType n, String argu) throws Exception {
        return "boolean";
    }

    /**
     * f0 -> "int"
     */
    public String visit(IntegerType n, String argu) throws Exception {
        return "int";
    }

    /**
     * f0 -> Identifier()
     * f1 -> "="
     * f2 -> Expression()
     * f3 -> ";"
     */
    public String visit(AssignmentStatement n, String argu) throws Exception {
        // ToDo Edit Later

        return null;
    }

    /**
     * f0 -> Identifier()
     * f1 -> "["
     * f2 -> Expression()
     * f3 -> "]"
     * f4 -> "="
     * f5 -> Expression()
     * f6 -> ";"
     */
    public String visit(ArrayAssignmentStatement n, String argu) throws Exception {
        // ToDo Edit Later

        return null;
    }

    /**
     * f0 -> "if"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     * f5 -> "else"
     * f6 -> Statement()
     */
    public String visit(IfStatement n, String argu) throws Exception {
        int jumplabel1;
        int jumplabel2;

        String ifExpr = n.f2.accept(this,null);
        jumplabel1 = tempcounter.getLabelTemp();

        this.spCode += "CJUMP " + ifExpr + " L" + jumplabel1 + "\n";
        n.f4.accept(this,null);
        jumplabel2 = tempcounter.getLabelTemp();
        this.spCode += "JUMP L" + jumplabel2 + "\n";
        this.spCode += "L" + jumplabel1 + " NOOP\n";
        n.f6.accept(this,null);
        this.spCode += "L" + jumplabel2 + " NOOP\n";

        return null;
    }

    /**
     * f0 -> "while"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     */
    public String visit(WhileStatement n, String argu) throws Exception {
        int jumplabel1;
        int jumplabel2;

        jumplabel1 = tempcounter.getLabelTemp();
        this.spCode += "L" + jumplabel1 + " NOOP\n";
        String whileExpr = n.f2.accept(this,null);
        jumplabel2 = tempcounter.getLabelTemp();
        this.spCode += "CJUMP " + whileExpr + " L" + jumplabel2 + "\n";
        n.f4.accept(this,null);
        this.spCode += "JUMP L" + jumplabel1 + "\n";
        this.spCode += "L" + jumplabel2 + " NOOP\n";

        return null;
    }

    /**
     * f0 -> "System.out.println"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> ";"
     */
    public String visit(PrintStatement n, String argu) throws Exception {
        String printExpr = n.f2.accept(this,null);

        this.spCode += "PRINT " + printExpr + "\n";

        return null;
    }

    /**
     * f0 -> AndExpression()
     *       | CompareExpression()
     *       | PlusExpression()
     *       | MinusExpression()
     *       | TimesExpression()
     *       | ArrayLookup()
     *       | ArrayLength()
     *       | MessageSend()
     *       | Clause()
     */
    public String visit(Expression n, String argu) throws Exception {
        // ToDo Maybe edit it if needed

        return null;
    }

    /**
     * f0 -> Clause()
     * f1 -> "&&"
     * f2 -> Clause()
     */
    public String visit(AndExpression n, String argu) throws Exception {
        String leftExpr;
        String rightExpr;
        int jumplabel1;

        leftExpr = n.f0.accept(this,null);
        jumplabel1 = tempcounter.getLabelTemp();
        this.spCode += "CJUMP " + leftExpr + " L" + jumplabel1 + "\n";
        rightExpr = n.f2.accept(this,null);
        this.spCode += "MOVE " + leftExpr + " " + rightExpr + "\n";
        this.spCode += "L" + jumplabel1 + " NOOP\n";

        return leftExpr;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "<"
     * f2 -> PrimaryExpression()
     */
    public String visit(CompareExpression n, String argu) throws Exception {
        // ToDo Maybe add MOVE TEMP if not stored already

        String leftExpr;
        String rightExpr;
        int jumptemp1;

        leftExpr = n.f0.accept(this,null);
        rightExpr = n.f2.accept(this,null);
        jumptemp1 = tempcounter.getTemp();
        this.spCode += "MOVE TEMP " + jumptemp1 + " LT " + leftExpr + " " + rightExpr + "\n";

        return "TEMP " + jumptemp1;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "+"
     * f2 -> PrimaryExpression()
     */
    public String visit(PlusExpression n, String argu) throws Exception {
        String leftExpr;
        String rightExpr;
        int jumptemp1;

        leftExpr = n.f0.accept(this,null);
        rightExpr = n.f0.accept(this,null);
        jumptemp1 = tempcounter.getTemp();
        this.spCode += "MOVE TEMP " + jumptemp1 + " PLUS " + leftExpr + " " + rightExpr + "\n";

        return "TEMP " + jumptemp1;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "-"
     * f2 -> PrimaryExpression()
     */
    public String visit(MinusExpression n, String argu) throws Exception {
        String leftExpr;
        String rightExpr;
        int jumptemp1;

        leftExpr = n.f0.accept(this,null);
        rightExpr = n.f0.accept(this,null);
        jumptemp1 = tempcounter.getTemp();
        this.spCode += "MOVE TEMP " + jumptemp1 + " MINUS " + leftExpr + " " + rightExpr + "\n";

        return "TEMP " + jumptemp1;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "*"
     * f2 -> PrimaryExpression()
     */
    public String visit(TimesExpression n, String argu) throws Exception {
        String leftExpr;
        String rightExpr;
        int jumptemp1;

        leftExpr = n.f0.accept(this,null);
        rightExpr = n.f0.accept(this,null);
        jumptemp1 = tempcounter.getTemp();
        this.spCode += "MOVE TEMP " + jumptemp1 + " TIMES " + leftExpr + " " + rightExpr + "\n";

        return "TEMP " + jumptemp1;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "["
     * f2 -> PrimaryExpression()
     * f3 -> "]"
     */
    public String visit(ArrayLookup n, String argu) throws Exception {
        String idtypeExpr;
        String arrayExpr;
        int jumplabel1;
        int jumplabel2;
        int counter = 0;
        HashMap<Integer,String> temps = new HashMap<Integer,String>();

        idtypeExpr = n.f0.accept(this,null);
        arrayExpr = n.f2.accept(this,null);
        counter++;
        temps.put(counter,"TEMP " + tempcounter.getTemp());
        this.spCode += "MOVE " + temps.get(counter) + " LT " + arrayExpr + " 0\n";
        jumplabel1 = tempcounter.getLabelTemp();
        this.spCode += "CJUMP " + temps.get(counter) + " L" + jumplabel1 + "\n";
        this.spCode += "ERROR\n";
        this.spCode += "L" + jumplabel1 + " NOOP\n";
        counter++;
        temps.put(counter,"TEMP " + tempcounter.getTemp());
        this.spCode += "HLOAD " + temps.get(counter) + " " + idtypeExpr + " 0\n";
        counter++;
        temps.put(counter,"TEMP " + tempcounter.getTemp());
        this.spCode += "MOVE " + temps.get(counter) + " 1\n";
        counter++;
        temps.put(counter,"TEMP " + tempcounter.getTemp());
        this.spCode += "MOVE " + temps.get(counter) + " LT " + arrayExpr + " " + temps.get(counter - 2) + "\n";
        counter++;
        temps.put(counter,"TEMP " + tempcounter.getTemp());
        this.spCode += "MOVE " + temps.get(counter) + " MINUS " + temps.get(counter - 2) + " " + temps.get(counter -1 ) + "\n";
        jumplabel2 = tempcounter.getLabelTemp();
        this.spCode += "CJUMP " + temps.get(counter) + " L" + jumplabel2 + "\n";
        this.spCode += "ERROR\n";
        this.spCode += "L" + jumplabel2 + " NOOP\n";
        counter++;
        temps.put(counter,"TEMP " + tempcounter.getTemp());
        this.spCode += "MOVE " + temps.get(counter) + " TIMES " + arrayExpr + " 4\n";
        counter++;
        temps.put(counter,"TEMP " + tempcounter.getTemp());
        this.spCode += "MOVE " + temps.get(counter) + " PLUS " + temps.get(counter - 1) + " 4\n";
        counter++;
        temps.put(counter,"TEMP " + tempcounter.getTemp());
        this.spCode += "MOVE " + temps.get(counter) + " PLUS " + idtypeExpr + " " + temps.get(counter - 1) + "\n";
        this.spCode += "HLOAD " + temps.get(counter - 6) + " " + temps.get(counter) + " 0\n";

        return temps.get(counter - 6);
    }

}
