import visitor.GJDepthFirst;

import SymbolTypes.*;
import java.util.HashMap;

public class SymbolVisitor extends GJDepthFirst<String,String> {
    HashMap<String,String> ClassMap;
    HashMap<String,HashMap<String,Variables>> VariablesMap = new HashMap<String,HashMap<String,Variables>>();
    HashMap<String,HashMap<String,Methods>> MethodsMap = new HashMap<String,HashMap<String,Methods>>();

    SymbolVisitor(HashMap<String,String> ClassMap) throws Exception{
        this.ClassMap = ClassMap;
    }
}
