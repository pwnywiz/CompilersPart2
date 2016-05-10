import syntaxtree.*;
import visitor.GJDepthFirst;
import SymbolTypes.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Supplier;

public class TypeVisitor extends GJDepthFirst<String,String> {
    HashMap<String,String> ClassMap;
    HashMap<String,SymbolTable> symboltable;

    HashMap<String,Variables> VariablesMap;
    HashMap<String,Methods> MethodsMap;
    ArrayList<String> storedArgs = new ArrayList<String>();
    boolean addArgs = false;
    boolean inClass = false;
    boolean enterIdentifier = true;
    String storedClass = null;
    String storedMethod = null;
    String storedIdentifier = null;

    public TypeVisitor(HashMap<String,String> ClassMap, HashMap<String,SymbolTable> symboltable) {
        this.ClassMap = ClassMap;
        this.symboltable = symboltable;
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
        this.storedClass = "Main Method";
        n.f15.accept(this,null);
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
        inClass = true;
        this.storedClass = n.f1.f0.toString();
        this.MethodsMap = symboltable.get(this.storedClass).getMethodsMap();
        this.VariablesMap = symboltable.get(this.storedClass).getVariablesMap();
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
        inClass = true;
        this.storedClass = n.f1.f0.toString();
        this.MethodsMap = symboltable.get(this.storedClass).getMethodsMap();
        this.VariablesMap = symboltable.get(this.storedClass).getVariablesMap();
        n.f6.accept(this,storedClass);

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
        String methodRet = n.f1.accept(this,null);
        String bodyRet;
        this.storedMethod = n.f2.f0.toString();

        n.f8.accept(this,null);
        bodyRet = n.f10.accept(this,null);

        if (!bodyRet.equals(methodRet)) {
            if (bodyRet.equals("boolean") || methodRet.equals("boolean") || bodyRet.equals("int[]") || methodRet.equals("int[]") || bodyRet.equals("int") || methodRet.equals("int")) {
                System.out.println("Difference between the defined and body return types in method with name '" + storedMethod + "'");
                throw new Exception();
            }
            else {  // Chance that the return value is an extended class
                String tempClass = bodyRet;
                boolean found = false;

                while (ClassMap.containsKey(tempClass)) {
                    if (ClassMap.get(tempClass).equals(methodRet)) {
                        found = true;
                        break;
                    }
                    tempClass = ClassMap.get(tempClass);
                }
                if (!found) {
                    System.out.println("Unknown body return type in method with name '" + storedMethod + "'");
                    throw new Exception();
                }
            }
        }
        // this.storedMethod = null;

        return null;
    }

    /**
     * f0 -> ArrayType()
     *       | BooleanType()
     *       | IntegerType()
     *       | Identifier()
     */
    public String visit(Type n, String argu) throws Exception {
        enterIdentifier = false;
        String check =  n.f0.accept(this,null);
        enterIdentifier = true;

        if (check.equals("boolean") || check.equals("int[]") || check.equals("int")) {
            return check;
        }

//        String tempClass = storedClass;
//        while (ClassMap.containsKey(tempClass)) {
//            if (tempClass.equals(check)) {
//                return check;
//            }
//            tempClass = ClassMap.get(tempClass);
//        }
        if (ClassMap.containsKey(check)) {
            return check;
        }

        System.out.println("Unknown Class return type");
        throw new Exception();
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
     * f0 -> <IDENTIFIER>
     */
    public String visit(Identifier n, String argu) throws Exception {
        this.storedIdentifier = n.f0.toString();


        if (enterIdentifier == false) {
            return this.storedIdentifier;
        }

        if (storedClass.equals("Main Method")) {
            return this.storedIdentifier;
        }
        // Begin with the innermost scope and return the variable type
            Methods tempMethod = this.MethodsMap.get(this.storedMethod);

            if (this.storedMethod != null && tempMethod.vars.containsKey(this.storedIdentifier)) {
                return tempMethod.vars.get(this.storedIdentifier).getVarType();
            }
            else if (this.storedMethod != null && tempMethod.findArgs(this.storedIdentifier) != null) {
                return tempMethod.findArgsType(this.storedIdentifier);
            }
            else if (this.VariablesMap.containsKey(this.storedIdentifier)) {
                return this.VariablesMap.get(this.storedIdentifier).getVarType();
            }
            else {
                String tempClass = storedClass;
                HashMap<String,Variables> tempVars;
                while (ClassMap.containsKey(tempClass)) {
                    tempClass = ClassMap.get(tempClass);
                    tempVars = symboltable.get(tempClass).getVariablesMap();
                    if (tempVars.containsKey(this.storedIdentifier)) {
                        return tempVars.get(this.storedIdentifier).getVarType();
                    }
                }
            }

            System.out.println("Variable with name '" + this.storedIdentifier + "' does not exist");
            throw new Exception();

    }

    /**
     * f0 -> Block()
     *       | AssignmentStatement()
     *       | ArrayAssignmentStatement()
     *       | IfStatement()
     *       | WhileStatement()
     *       | PrintStatement()
     */
    public String visit(Statement n, String argu) throws Exception {
        n.f0.accept(this,null);
        return null;
    }

    /**
     * f0 -> Identifier()
     * f1 -> "="
     * f2 -> Expression()
     * f3 -> ";"
     */
    public String visit(AssignmentStatement n, String argu) throws Exception {
        String leftType = n.f0.accept(this,null);
        String rightType = n.f2.accept(this,null);

        if (!leftType.equals(rightType)) {
            if (leftType.equals("boolean") || rightType.equals("boolean") || leftType.equals("int[]") || rightType.equals("int[]") || leftType.equals("int") || rightType.equals("int")) {
                System.out.print("Incorrect assignment of type '" + rightType + "' to type '" + leftType + "'");
                throw new Exception();
            }
            else {
                String tempClass = rightType;
                boolean found = false;

                while (ClassMap.containsKey(tempClass)) {
                    if (ClassMap.get(tempClass).equals(leftType)) {
                        found = true;
                        break;
                    }
                    tempClass = ClassMap.get(tempClass);
                }
                if (!found) {
                    System.out.println("Incorrect assignment of type '" + rightType + "' to type '" + leftType + "'");
                    throw new Exception();
                }
            }
        }

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
        String arrayType = n.f0.accept(this,null);
        String leftType = n.f2.accept(this,null);
        String rightType = n.f5.accept(this,null);

        if (!arrayType.equals("int[]") || leftType.equals("boolean") || rightType.equals("boolean") || (leftType.equals("int[]") && rightType.equals("int[]"))) {
            System.out.println("Incorrect assignment of type '" + rightType + "' to int array");
            throw new Exception();
        }
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
        String ifExpr = n.f2.accept(this,null);

        if (!ifExpr.equals("boolean")) {
            System.out.println("If Statement does not execute a logical action");
            throw new Exception();
        }
        n.f4.accept(this,null);
        n.f6.accept(this,null);

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
        String whileExpr = n.f2.accept(this,null);

        if (!whileExpr.equals("boolean")) {
            System.out.println("While Statement does not execute a logical action");
            throw new Exception();
        }
        n.f4.accept(this,null);

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
        if (!printExpr.equals("boolean") && !printExpr.equals("int")) {
            System.out.println("Incorrect output type from the print method");
            throw new Exception();
        }
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
        String expr = n.f0.accept(this,null);
//        if (this.addArgs) {
            this.storedArgs.add(expr);
//        }

        return expr;
    }

    /**
     * f0 -> Clause()
     * f1 -> "&&"
     * f2 -> Clause()
     */
    public String visit(AndExpression n, String argu) throws Exception {
        String leftType = n.f0.accept(this,null);
        String rightType = n.f2.accept(this,null);

        if (!leftType.equals("boolean") || !rightType.equals("boolean")) {
            System.out.println("Non boolean Logical Expression types");
            throw new Exception();
        }

        return "boolean";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "<"
     * f2 -> PrimaryExpression()
     */
    public String visit(CompareExpression n, String argu) throws Exception {
        String leftType = n.f0.accept(this,null);
        String rightType = n.f2.accept(this,null);

        if ((!leftType.equals("int[]") && !leftType.equals("int")) || (!rightType.equals("int[]") && !rightType.equals("int"))) {
            System.out.println("Non int or int[] Logical Expression types");
            throw new Exception();
        }

        return "boolean";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "+"
     * f2 -> PrimaryExpression()
     */
    public String visit(PlusExpression n, String argu) throws Exception {
        String leftType = n.f0.accept(this,null);
        String rightType = n.f2.accept(this,null);

        if ((!leftType.equals("int[]") && !leftType.equals("int")) || (!rightType.equals("int[]") && !rightType.equals("int"))) {
            System.out.println("Non int or int[] Plus Expression types");
            throw new Exception();
        }

        return "int";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "-"
     * f2 -> PrimaryExpression()
     */
    public String visit(MinusExpression n, String argu) throws Exception {
        String leftType = n.f0.accept(this,null);
        String rightType = n.f2.accept(this,null);

        if ((!leftType.equals("int[]") && !leftType.equals("int")) || (!rightType.equals("int[]") && !rightType.equals("int"))) {
            System.out.println("Non int or int[] Minus Expression types");
            throw new Exception();
        }

        return "int";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "*"
     * f2 -> PrimaryExpression()
     */
    public String visit(TimesExpression n, String argu) throws Exception {
        String leftType = n.f0.accept(this,null);
        String rightType = n.f2.accept(this,null);

        if ((!leftType.equals("int[]") && !leftType.equals("int")) || (!rightType.equals("int[]") && !rightType.equals("int"))) {
            System.out.println("Non int or int[] Times Expression types");
            throw new Exception();
        }

        return "int";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "["
     * f2 -> PrimaryExpression()
     * f3 -> "]"
     */
    public String visit(ArrayLookup n, String argu) throws Exception {
        String leftType = n.f0.accept(this,null);
        String rightType = n.f2.accept(this,null);

        if (!leftType.equals("int[]")) {
            System.out.println("Invalid storage variable on Array Lookup (Must be int[])");
            throw new Exception();
        }
        if (!rightType.equals("int[]") && !rightType.equals("int")) {
            System.out.println("Invalid assignment variable on Array Lookup (Must be int[] or int)");
            throw new Exception();
        }

        return "int";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> "length"
     */
    public String visit(ArrayLength n, String argu) throws Exception {
        String expr = n.f0.accept(this,null);

        if (!expr.equals("int[]")) {
            System.out.println("Invalid length request (Must be int[])");
            throw new Exception();
        }

        return "int";
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
        String callingClass = n.f0.accept(this,null);
        String methodName = n.f2.f0.toString();

        Methods tempMethod;
        HashMap<String,Methods> tempMethodMap;
        ArrayList<String> exprArgs;
        ArrayList<NamedVariables> methodArgs;

        if (!this.ClassMap.containsKey(callingClass)) {
            System.out.println("Calling type with name '" + callingClass + "' does not exist");
            throw new Exception();
        }

        tempMethodMap = symboltable.get(callingClass).getMethodsMap();
        String tempClass = callingClass;
        boolean found = false;

        while (ClassMap.containsKey(tempClass)) {
            if (tempMethodMap.containsKey(methodName)) {
                found = true;
                break;
            }
            tempClass = ClassMap.get(tempClass);
            tempMethodMap = symboltable.get(tempClass).getMethodsMap();
        }
        if (!found) {
            System.out.println("Unknown method call with name '" + methodName + "' from the class with name '" + callingClass + "'");
            throw new Exception();
        }

        tempMethod = tempMethodMap.get(methodName);

        methodArgs = tempMethod.args;
        this.storedArgs = new ArrayList<String>();
        this.addArgs = true;

        n.f4.accept(this,"yes");

        exprArgs = this.storedArgs;
        if (exprArgs.size() != methodArgs.size()) {
            System.out.println("Different amount of arguments between the same method call with name '" + methodName + "'");
            throw new Exception();
        }
        for (int i = 0; i < exprArgs.size(); i++) {
            if (!methodArgs.get(i).vars.getVarType().equals(exprArgs.get(i))) {
                String tempClass2 = exprArgs.get(i);
                boolean found2 = false;

                while (ClassMap.containsKey(tempClass2)) {
                    if (ClassMap.get(tempClass2).equals(methodArgs.get(i).vars.getVarType())) {
                        found2 = true;
                        break;
                    }
                    tempClass2 = ClassMap.get(tempClass2);
                }
                if (!found2) {
                    System.out.println("Different declared and passed argument types in method with name '" + methodName + "'");
                    throw new Exception();
                }
            }
        }
        this.addArgs = false;

        return tempMethod.returnType;
    }

    /**
     * f0 -> Expression()
     * f1 -> ExpressionTail()
     */
    public String visit(ExpressionList n, String argu) throws Exception {
        String expr = n.f0.accept(this,null);
        n.f1.accept(this,null);

        return expr;
    }

    /**
     * f0 -> ","
     * f1 -> Expression()
     */
    public String visit(ExpressionTerm n, String argu) throws Exception {
        return n.f1.accept(this,null);
    }

    /**
     * f0 -> IntegerLiteral()
     *       | TrueLiteral()
     *       | FalseLiteral()
     *       | Identifier()
     *       | ThisExpression()
     *       | ArrayAllocationExpression()
     *       | AllocationExpression()
     *       | BracketExpression()
     */
//    public String visit(PrimaryExpression n, String argu) throws Exception {
//        return n.f0.accept(this,null);
//    }

    /**
     * f0 -> <INTEGER_LITERAL>
     */
    public String visit(IntegerLiteral n, String argu) throws Exception {
        return "int";
    }

    /**
     * f0 -> "true"
     */
    public String visit(TrueLiteral n, String argu) throws Exception {
        return "boolean";
    }

    /**
     * f0 -> "false"
     */
    public String visit(FalseLiteral n, String argu) throws Exception {
        return "boolean";
    }

    /**
     * f0 -> "this"
     */
    public String visit(ThisExpression n, String argu) throws Exception {
        return this.storedClass;
    }

    /**
     * f0 -> "new"
     * f1 -> "int"
     * f2 -> "["
     * f3 -> Expression()
     * f4 -> "]"
     */
    public String visit(ArrayAllocationExpression n, String argu) throws Exception {
        return "int[]";
    }

    /**
     * f0 -> "new"
     * f1 -> Identifier()
     * f2 -> "("
     * f3 -> ")"
     */
    public String visit(AllocationExpression n, String argu) throws Exception {
        enterIdentifier = false;
        n.f1.accept(this,null);
        enterIdentifier = true;

        String idcheck = n.f1.f0.toString();
        if (!this.ClassMap.containsKey(idcheck)) {
            System.out.println("Incorrect Allocation Type");
            throw new Exception();
        }

        return idcheck;
    }

    /**
     * f0 -> "!"
     * f1 -> Clause()
     */
    public String visit(NotExpression n, String argu) throws Exception {
        return n.f1.accept(this,null);
    }

    /**
     * f0 -> "("
     * f1 -> Expression()
     * f2 -> ")"
     */
    public String visit(BracketExpression n, String argu) throws Exception {
        return n.f1.accept(this,null);
    }

}
