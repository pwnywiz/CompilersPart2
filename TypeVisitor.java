import syntaxtree.*;
import visitor.GJDepthFirst;
import SymbolTypes.*;
import java.util.ArrayList;
import java.util.HashMap;

public class TypeVisitor extends GJDepthFirst<String,String> {
    HashMap<String,String> ClassMap;
    HashMap<String,SymbolTable> symboltable;

    HashMap<String,Variables> VariablesMap;
    HashMap<String,Methods> MethodsMap;
    ArrayList<NamedVariables> args;
    Variables var;
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
        // Edit it later
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
        this.storedClass = n.f1.accept(this,null);
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
                System.out.print("Difference between the defined and body return types in method with name '" + storedMethod + "'");
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
        String id = n.f0.f0.toString();
        String expr = n.f2.accept(this,null);
        //Fill

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
        String id = n.f0.f0.toString();
        String leftExpr = n.f2.accept(this,null);
        String rightExpr = n.f4.accept(this,null);

        return null;
    }

    /**
     * f0 -> <IDENTIFIER>
     */
    public String visit(Identifier n, String argu) throws Exception {
        this.storedIdentifier = n.f0.toString();
        Methods tempMethod = this.MethodsMap.get(this.storedMethod);
        Variables tempVar;

        // Begin with the innermost scope
        if (this.storedClass != null) {

            if (this.storedMethod != null && tempMethod.vars.containsKey(storedIdentifier)) {
                return tempMethod.vars.get(storedIdentifier).getVarType();
            }
            else if (this.storedMethod != null && tempMethod.findArgs(storedIdentifier) != null) {
                return tempMethod.findArgs(storedIdentifier);
            }
            else if (this.ClassMap.containsKey(this.storedIdentifier)) {
                return this.storedIdentifier;
            }
            // else if (it is an inherited variable)
        }

        return null;
    }
}
