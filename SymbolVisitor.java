import visitor.GJDepthFirst;

import SymbolTypes.*;
import java.util.HashMap;

public class SymbolVisitor extends GJDepthFirst<String,String> {
    HashMap<String,String> ClassMap;
    HashMap<String,SymbolTable> symboltable = new HashMap<String,SymbolTable>();

    public HashMap<String,SymbolTable> getSymboltable() {
        return symboltable;
    }

    public SymbolVisitor(HashMap<String,String> ClassMap) throws Exception{
        this.ClassMap = ClassMap;
    }
}
