import syntaxtree.*;
import visitor.GJDepthFirst;
import SymbolTypes.*;
import java.util.ArrayList;
import java.util.HashMap;

public class SymbolVisitor extends GJDepthFirst<String,String> {
    HashMap<String,String> ClassMap;
    HashMap<String,SymbolTable> symboltable = new HashMap<String,SymbolTable>();

    HashMap<String,Variables> VariablesMap;
    HashMap<String,Methods> MethodsMap;
    String inClass = null;
    String inMethod = null;
    ArrayList<NamedVariables> args;
    Variables var;
    String storedClass;

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
        this.storedClass = n.f1.accept(this,null);
        VariablesMap = new HashMap<String,Variables>();
        MethodsMap = new HashMap<String,Methods>();
        SymbolTable symbtable = new SymbolTable();

        inClass = "yes";
        n.f3.accept(this,this.storedClass);
        symbtable.addVariables(VariablesMap);
        n.f4.accept(this,this.storedClass);
        symbtable.addMethods(MethodsMap);
        symboltable.put(this.storedClass,symbtable);
        inClass = null;

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
        this.storedClass = n.f1.accept(this,null);
        VariablesMap = new HashMap<String,Variables>();
        MethodsMap = new HashMap<String,Methods>();
        SymbolTable symbtable = new SymbolTable();

        inClass = "yes";
        n.f5.accept(this,this.storedClass);
        symbtable.addVariables(VariablesMap);
        n.f6.accept(this,this.storedClass);
        symbtable.addMethods(MethodsMap);
        symboltable.put(this.storedClass,symbtable);
        inClass = null;

        return null;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     * f2 -> ";"
     */
    public String visit(VarDeclaration n, String storedName) throws Exception {
        String varType = n.f0.accept(this,null);
        String varName = n.f1.f0.toString();
        var = new Variables();
        var.setIsVar(true);
        var.setVarType(varType);

        if (!varType.equals("boolean") && !varType.equals("int") && !varType.equals("int[]") && !ClassMap.containsKey(varType)) {
            System.out.println("Unknown type declaration with name '" + varType + "'");
            throw new Exception();
        }

        if (inMethod != null) {
            Methods temp = this.MethodsMap.get(storedName);

            if (temp.dupCheck(varName)) {
                System.out.println("Duplicate variable declaration with name '" + varName + "' in method '" + storedName + "'");
                throw new Exception();
            }

            temp.vars.put(varName,var);
            MethodsMap.put(storedName,temp);
        }
        else if (inClass != null) {
            if (this.VariablesMap.containsKey(varName)) {
                System.out.println("Duplicate variable declaration with name '" + varName + "' in class '" + storedName + "'");
                throw new Exception();
            }

            VariablesMap.put(varName,var);
        }

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
        String methodType = n.f1.accept(this,null);
        String methodName = n.f2.f0.toString();
        Methods temp = new Methods();
        args = new ArrayList<NamedVariables>();
        inMethod = "yes";

        if (!methodType.equals("boolean") && !methodType.equals("int") && !methodType.equals("int[]") && !ClassMap.containsKey(methodType)) {
            System.out.println("Unknown return type with name '" + methodType + "'");
            throw new Exception();
        }
        if (this.MethodsMap.containsKey(methodName)) {
            System.out.println("Duplicate method declaration with name '" + methodName + "' in class '" + storedName + "'");
            throw new Exception();
        }

        n.f4.accept(this,methodName);

        // Shadowing Check
        if (this.ClassMap.get(storedName) != null) {
            String superClass = this.ClassMap.get(storedName);
            HashMap<String,Methods> temp2;
            ArrayList<NamedVariables> tempArray;

            while (superClass != null) {
                if (this.symboltable.containsKey(superClass)) {
                    temp2 = this.symboltable.get(superClass).getMethodsMap();
                }
                else {
                    superClass = this.ClassMap.get(superClass);
                    continue;
                }

                if (temp2.containsKey(methodName)) {
                    if (!temp2.get(methodName).returnType.equals(methodType)) {
                        System.out.println("Different return types of method with name '" + methodName + "'");
                        throw new Exception();
                    }

                    tempArray = temp2.get(methodName).args;
                    if (tempArray.size() != args.size()) {
                        System.out.println("Different amount of arguments in method with name '" + methodName + "' between class '" + storedName + "' and '" + superClass + "'");
                        throw new Exception();
                    }

                    for (int i = 0; i < tempArray.size(); i++) {
                        if (!tempArray.get(i).vars.getVarType().equals(args.get(i).vars.getVarType())) {
                            System.out.println("Different argument types in method with name '" + methodName + "' between class '" + storedName + "' and '" + superClass + "'");
                            throw new Exception();
                        }
                    }

                    break;
                }
                superClass = this.ClassMap.get(superClass);
            }
        }

        temp.returnType = methodType;
        temp.args = args;
        MethodsMap.put(methodName,temp);
        n.f7.accept(this,methodName);
        inMethod = null;

        return null;
    }

    /**
     * f0 -> FormalParameter()
     * f1 -> FormalParameterTail()
     */
    public String visit(FormalParameterList n, String argu) throws Exception {
        n.f0.accept(this,argu);
        n.f1.accept(this,argu);

        return null;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     */
    public String visit(FormalParameter n, String storedName) throws Exception {
        Variables tempVars = new Variables();
        tempVars.setIsVar(false);
        tempVars.setVarType(n.f0.accept(this,null));
        NamedVariables tempNamedVariables = new NamedVariables(tempVars, n.f1.f0.toString());

        if (!tempVars.getVarType().equals("boolean") && !tempVars.getVarType().equals("int") && !tempVars.getVarType().equals("int[]") && !ClassMap.containsKey(tempVars.getVarType())) {
            System.out.println("Unknown return type with name '" + tempVars.getVarType() + "' in method declaration with name '" + storedName + "'");
            throw new Exception();
        }
        for (NamedVariables temp : args) {
            if (temp.name.equals(tempNamedVariables.name)) {
                System.out.println("Duplicate declaration of argument with name '" + temp.name + "' in method with name '" + storedName + "'");
                throw new Exception();
            }
        }

        args.add(tempNamedVariables);

        return null;
    }

    /**
     * f0 -> ArrayType()
     *       | BooleanType()
     *       | IntegerType()
     *       | Identifier()
     */
    public String visit(Type n, String argu) throws Exception {
        return n.f0.accept(this,null);
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
        return n.f0.toString();
    }
}
