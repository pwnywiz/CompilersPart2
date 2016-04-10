import syntaxtree.*;
import visitor.GJDepthFirst;
import java.util.*;

public class FirstVisitor extends GJDepthFirst<Integer, Integer>{

    /**
     * f0 -> MainClass()
     * f1 -> ( TypeDeclaration() )*
     * f2 -> <EOF>
     */

    public String visit(Goal n, String argu) throws Exception {
        n.f0.accept(this,null);
        if (n.f1.present()) {
            n.f1.accept(this,null);
        }
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

        return null;
    }
}
