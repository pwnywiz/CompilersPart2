import syntaxtree.*;
import visitor.GJDepthFirst;
import java.util.*;

public class ClassVisitor extends GJDepthFirst<String,String>{
    HashMap<String,String> ClassMap = new HashMap<>(); // Holds the class name as key and the possible extended class as value

    public HashMap<String,String> getClassMap() {
        return this.ClassMap;
    }

    private boolean MainVisited = false;
    /**
     * f0 -> MainClass()
     * f1 -> ( TypeDeclaration() )*
     * f2 -> <EOF>
     */

    public String visit(Goal n, String argu) throws Exception {
        n.f0.accept(this,null);
        n.f1.accept(this,null);
        return null;
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
        if (MainVisited) {
            System.out.println("More than one main methods");
            throw new Exception();
        }
        MainVisited = true;
        this.ClassMap.put(n.f1.f0.toString(),null);
        // Might need to accept f14

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
        if (this.ClassMap.containsKey(n.f1.f0.toString())) {
            System.out.println("Class name '" + n.f1.f0.toString() + "' already exists");
            throw new Exception();
        }
        this.ClassMap.put(n.f1.f0.toString(),null);

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
        if (this.ClassMap.containsKey(n.f1.f0.toString())) {
            System.out.println("Class name '" + n.f1.f0.toString() + "' already exists");
            throw new Exception();
        }
        if (!this.ClassMap.containsKey(n.f3.f0.toString())) {
            System.out.println("Extended Class name '" + n.f3.f0.toString() + "' does not exist");
            throw new Exception();
        }
        this.ClassMap.put(n.f1.f0.toString(),n.f3.f0.toString());

        return null;
    }
}
