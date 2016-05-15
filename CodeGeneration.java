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
    ArrayList<String> exprArgs = new ArrayList<String>();

    String spCode = "";
    boolean storeVal = false;
    boolean copyVal = false;
    boolean thisObject = false;
    String storedClass = null;
    String actualObject = null;
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
        tempVariables.clear();
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
        copyVal = true;
        String retValue = n.f10.accept(this,null);
        this.spCode += "RETURN\n";
        this.spCode += "\t" + retValue + "\n";
        this.spCode += "END\n";

        copyVal = false;
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
        String rightExpr;
        String leftIdentifier;

        copyVal = true;
        rightExpr = n.f2.accept(this,null);
        storeVal = true;
        leftIdentifier = n.f0.accept(this,null);

        if (leftIdentifier.equals("temp0")) {
            this.spCode += " " + rightExpr + "\n";
        }
        else {
            this.spCode += "MOVE " + leftIdentifier + " " + rightExpr + "\n";
        }

        storeVal = false;
        copyVal = false;
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
        String idtypeExpr;
        String arrayExpr;
        String rightExpr;
        int jumplabel1;
        int jumplabel2;
        int counter = 0;
        HashMap<Integer,String> temps = new HashMap<Integer,String>();

        copyVal = true;
        idtypeExpr = n.f0.accept(this,null);
        copyVal = true;
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

        copyVal = true;
        rightExpr = n.f5.accept(this,null);
        this.spCode += "HSTORE " + temps.get(counter) + " 0 " + rightExpr + "\n";

        copyVal = false;
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

        copyVal = true;
        String ifExpr = n.f2.accept(this,null);
        jumplabel1 = tempcounter.getLabelTemp();

        this.spCode += "CJUMP " + ifExpr + " L" + jumplabel1 + "\n";
        n.f4.accept(this,null);
        jumplabel2 = tempcounter.getLabelTemp();
        this.spCode += "JUMP L" + jumplabel2 + "\n";
        this.spCode += "L" + jumplabel1 + " NOOP\n";
        n.f6.accept(this,null);
        this.spCode += "L" + jumplabel2 + " NOOP\n";

        copyVal = false;
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
        copyVal = true;
        String whileExpr = n.f2.accept(this,null);
        copyVal = false;
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
        copyVal = true;
        String printExpr = n.f2.accept(this,null);

        this.spCode += "PRINT " + printExpr + "\n";

        copyVal = false;
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
//    public String visit(Expression n, String argu) throws Exception {
//        // ToDo Maybe edit it if needed
//
//        return null;
//    }

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
        // ToDo MOVE TEMP inside every PrimaryExpression

        String leftExpr;
        String rightExpr;
        int labeltemp1;

        leftExpr = n.f0.accept(this,null);
        rightExpr = n.f2.accept(this,null);
        labeltemp1 = tempcounter.getTemp();
        this.spCode += "MOVE TEMP " + labeltemp1 + " LT " + leftExpr + " " + rightExpr + "\n";

        return "TEMP " + labeltemp1;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "+"
     * f2 -> PrimaryExpression()
     */
    public String visit(PlusExpression n, String argu) throws Exception {
        String leftExpr;
        String rightExpr;
        int labeltemp1;

        leftExpr = n.f0.accept(this,null);
        rightExpr = n.f0.accept(this,null);
        labeltemp1 = tempcounter.getTemp();
        this.spCode += "MOVE TEMP " + labeltemp1 + " PLUS " + leftExpr + " " + rightExpr + "\n";

        return "TEMP " + labeltemp1;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "-"
     * f2 -> PrimaryExpression()
     */
    public String visit(MinusExpression n, String argu) throws Exception {
        String leftExpr;
        String rightExpr;
        int labeltemp1;

        leftExpr = n.f0.accept(this,null);
        rightExpr = n.f0.accept(this,null);
        labeltemp1 = tempcounter.getTemp();
        this.spCode += "MOVE TEMP " + labeltemp1 + " MINUS " + leftExpr + " " + rightExpr + "\n";

        return "TEMP " + labeltemp1;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "*"
     * f2 -> PrimaryExpression()
     */
    public String visit(TimesExpression n, String argu) throws Exception {
        String leftExpr;
        String rightExpr;
        int labeltemp1;

        leftExpr = n.f0.accept(this,null);
        rightExpr = n.f0.accept(this,null);
        labeltemp1 = tempcounter.getTemp();
        this.spCode += "MOVE TEMP " + labeltemp1 + " TIMES " + leftExpr + " " + rightExpr + "\n";

        return "TEMP " + labeltemp1;
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

        copyVal = true;
        idtypeExpr = n.f0.accept(this,null);
        copyVal = true;
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

        copyVal = false;
        return temps.get(counter - 6);
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> "length"
     */
    public String visit(ArrayLength n, String argu) throws Exception {
        String arrayExpr;
        int labeltemp1;

        copyVal = true;
        arrayExpr = n.f0.accept(this,null);
        labeltemp1 = tempcounter.getTemp();
        this.spCode += "HLOAD TEMP " + labeltemp1 + " " + arrayExpr + " 0\n";

        copyVal = false;
        return "TEMP " + labeltemp1;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( ExpressionList() )?
     * f5 -> ")"
     */
    public String visit(MessageSend n, String argu) throws Exception {
        copyVal = true;
        String callingClass = n.f0.accept(this,null);
        String methodName = n.f2.f0.toString();
        exprArgs.clear();

        ArrayList<String> methodFind;
        String actualClass;
        int labeltemp1;
        int labeltemp2;
        int labeltemp3;
        int methodOffset = 0;

        if (this.thisObject) {
            actualClass = this.storedClass;
        }
        else {
            // ToDo Edit every PrimaryExpression to add the class name at actualObject
            actualClass = this.actualObject;
        }

        labeltemp1 = tempcounter.getTemp();
        this.spCode += "HLOAD TEMP " + labeltemp1 + " " + callingClass + " 0\n";

        methodFind = vMethods.get(actualClass);
        for (String tempmethod : methodFind) {
            if (tempmethod.contains(methodName)) {
                break;
            }
            methodOffset++;
        }

        labeltemp2 = tempcounter.getTemp();
        this.spCode += "HLOAD TEMP " + labeltemp2 + " TEMP " + labeltemp1 + " " + methodOffset*4 + "\n";

        copyVal = true;
        n.f4.accept(this,null);

        labeltemp3 = tempcounter.getTemp();
        this.spCode += "MOVE TEMP " + labeltemp3 + " TEMP " + labeltemp2 + "( " + callingClass + " ";
        for (String tempArg : exprArgs) {
            this.spCode += tempArg + " ";
        }
        this.spCode += ")\n";

        this.thisObject = false;

        copyVal = false;
        return "TEMP " + labeltemp3;
    }

    /**
     * f0 -> Expression()
     * f1 -> ExpressionTail()
     */
    public String visit(ExpressionList n, String argu) throws Exception {
        String expr = n.f0.accept(this,null);
        this.exprArgs.add(expr);
        n.f1.accept(this,null);

        return expr;
    }

    /**
     * f0 -> ","
     * f1 -> Expression()
     */
    public String visit(ExpressionTerm n, String argu) throws Exception {
        String expr = n.f1.accept(this,null);
        this.exprArgs.add(expr);

        return expr;
    }

    /**
     * f0 -> <INTEGER_LITERAL>
     */
    public String visit(IntegerLiteral n, String argu) throws Exception {
        int labeltemp1;

        labeltemp1 = tempcounter.getTemp();
        this.spCode += "MOVE TEMP " + labeltemp1 + " " + n.f0.toString() + "\n";

        return "TEMP " + labeltemp1;
    }

    /**
     * f0 -> "true"
     */
    public String visit(TrueLiteral n, String argu) throws Exception {
        int labeltemp1;

        labeltemp1 = tempcounter.getTemp();
        this.spCode += "MOVE TEMP " + labeltemp1 + " 1\n";

        return "TEMP " + labeltemp1;
    }

    /**
     * f0 -> "false"
     */
    public String visit(FalseLiteral n, String argu) throws Exception {
        int labeltemp1;

        labeltemp1 = tempcounter.getTemp();
        this.spCode += "MOVE TEMP " + labeltemp1 + " 0\n";

        return "TEMP " + labeltemp1;
    }

    /**
     * f0 -> <IDENTIFIER>
     */
    public String visit(Identifier n, String argu) throws Exception {
        ArrayList<String> multiclassVars;
        String tempClass;
        boolean foundVar = false;
        int labeltemp1;
        int varOffset = 1;

        String storedIdentifier = n.f0.toString();

        if (this.storedClass.equals("Main Method")) {
            this.actualObject = this.storedClass;
            return "TEMP " + this.tempVariables.get(storedIdentifier);
        }

        if (this.tempVariables.containsKey(storedIdentifier)) {
            this.actualObject = Symboltable.get(this.storedClass).getMethodsMap().get(this.storedMethod).vars.get(storedIdentifier).getVarType();
            return "TEMP " + this.tempVariables.get(storedIdentifier);
        }

        tempClass = this.storedClass;
        do {
            multiclassVars = vVariables.get(tempClass);
            if (multiclassVars.contains(tempClass + "_" + storedIdentifier)) {
                foundVar = true;
                for (String varName : multiclassVars) {
                    if (varName.equals(tempClass + "_" + storedIdentifier)) {
                        break;
                    }
                    varOffset++;
                }
                break;
            }
        } while ((tempClass = ClassMap.get(tempClass)) != null);

        if (foundVar) {
            if (storeVal) {
                this.spCode += "HSTORE TEMP 0 " + varOffset*4;
                this.storeVal = false;
                return "temp0";
            }
            else if (copyVal) {
                labeltemp1 = tempcounter.getTemp();
                this.spCode += "HLOAD TEMP " + labeltemp1 + " TEMP 0 " + varOffset*4 + "\n";
                this.copyVal = false;
                return "TEMP " + labeltemp1;
            }
        }
        else {
            return "TEMP " + this.tempVariables.get(storedIdentifier);
        }

        this.storeVal = false;
        this.copyVal = false;

        return storedIdentifier;
    }

    /**
     * f0 -> "this"
     */
    public String visit(ThisExpression n, String argu) throws Exception {
        this.thisObject = true;

        return "TEMP 0";
    }

    /**
     * f0 -> "new"
     * f1 -> "int"
     * f2 -> "["
     * f3 -> Expression()
     * f4 -> "]"
     */
    public String visit(ArrayAllocationExpression n, String argu) throws Exception {
        String allocExpr;
        int jumplabel1;
        int jumplabel2;
        int jumplabel3;
        int counter = 0;
        HashMap<Integer,String> temps = new HashMap<Integer,String>();

        copyVal = true;
        allocExpr = n.f3.accept(this,null);
        counter++;
        temps.put(counter,"TEMP " + tempcounter.getTemp());
        this.spCode += "MOVE " + temps.get(counter) + " LT " + allocExpr + " 0\n";
        jumplabel1 = tempcounter.getLabelTemp();
        this.spCode += "CJUMP " + temps.get(counter) + " L" + jumplabel1 + "\n";
        this.spCode += "ERROR\n";
        this.spCode += "L" + jumplabel1 + " NOOP\n";
        counter++;
        temps.put(counter,"TEMP " + tempcounter.getTemp());
        this.spCode += "MOVE " + temps.get(counter) + " PLUS " + allocExpr + " 1\n";
        counter++;
        temps.put(counter,"TEMP " + tempcounter.getTemp());
        this.spCode += "MOVE " + temps.get(counter) + " TIMES " + temps.get(counter - 1) + " 4\n";
        counter++;
        temps.put(counter,"TEMP " + tempcounter.getTemp());
        this.spCode += "MOVE " + temps.get(counter) + " HALLOCATE " + temps.get(counter - 1) + "\n";
        counter++;
        temps.put(counter,"TEMP " + tempcounter.getTemp());
        this.spCode += "MOVE " + temps.get(counter) + " 4\n";
        jumplabel2 = tempcounter.getLabelTemp();
        this.spCode += "L" + jumplabel2 + " NOOP\n";
        counter++;
        temps.put(counter,"TEMP " + tempcounter.getTemp());
        this.spCode += "MOVE " + temps.get(counter) + " LT " + temps.get(counter - 1) + " " + temps.get(counter - 3) + "\n";
        jumplabel3 = tempcounter.getLabelTemp();
        this.spCode += "CJUMP " + temps.get(counter) + " L" + jumplabel3 + "\n";
        counter++;
        temps.put(counter,"TEMP " + tempcounter.getTemp());
        this.spCode += "MOVE " + temps.get(counter) + " PLUS " + temps.get(counter - 3) + " " + temps.get(counter - 2) + "\n";
        counter++;
        temps.put(counter,"TEMP " + tempcounter.getTemp());
        this.spCode += "MOVE " + temps.get(counter) + " 0\n";
        this.spCode += "HSTORE " + temps.get(counter - 1) + " 0 " + temps.get(counter) + "\n";
        this.spCode += "MOVE " + temps.get(counter - 3) + " PLUS " + temps.get(counter - 3) + " 4\n";
        this.spCode += "JUMP L" + jumplabel2 + "\n";
        this.spCode += "L" + jumplabel3 + " NOOP\n";
        this.spCode += "HSTORE " + temps.get(counter - 4) + " 0 " + allocExpr + "\n";

        copyVal = false;
        return temps.get(counter - 4);
    }

    /**
     * f0 -> "new"
     * f1 -> Identifier()
     * f2 -> "("
     * f3 -> ")"
     */
    public String visit(AllocationExpression n, String argu) throws Exception {
        ArrayList<String> classVariables;
        int labeltemp1;
        int labeltemp2;
        int labeltemp3;
        int labeltemp4;

        String objectName = n.f1.f0.toString();
        classVariables = this.vVariables.get(objectName);

        labeltemp1 = tempcounter.getTemp();
        this.spCode += "MOVE TEMP " + labeltemp1 + " HALLOCATE " + (classVariables.size() + 1)*4 + "\n";
        labeltemp2 = tempcounter.getTemp();
        this.spCode += "MOVE TEMP " + labeltemp2 + " " + objectName + "_vTable\n";
        labeltemp3 = tempcounter.getTemp();
        this.spCode += "HLOAD TEMP " + labeltemp3 + " " + labeltemp2 + " 0\n";
        this.spCode += "HSTORE TEMP " + labeltemp1 + " 0 " + labeltemp3 + "\n";
        labeltemp4 = tempcounter.getTemp();
        this.spCode += "MOVE TEMP " + labeltemp4 + " 0\n";

        for (int i = 0; i < classVariables.size(); i++) {
            this.spCode += "HSTORE TEMP " + labeltemp1 + " " + (i+1)*4 + " " + labeltemp4 + "\n";
        }

        return "TEMP " + labeltemp1;
    }

    /**
     * f0 -> "!"
     * f1 -> Clause()
     */
    public String visit(NotExpression n, String argu) throws Exception {
        String exprNot;
        int labeltemp1;
        int labeltemp2;

        copyVal = true;
        exprNot = n.f1.accept(this,null);
        labeltemp1 = tempcounter.getTemp();

        this.spCode += "MOVE TEMP " + labeltemp1 + " 1\n";
        labeltemp2 = tempcounter.getTemp();
        this.spCode += "MOVE TEMP " + labeltemp2 + " MINUS TEMP " + labeltemp1 + " " + exprNot + "\n";

        copyVal = false;
        return "TEMP " + labeltemp2;
    }
}
