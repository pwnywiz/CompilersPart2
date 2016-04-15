package SymbolTypes;

import java.util.ArrayList;
import java.util.HashMap;

public class Methods {
    String returnType;
    HashMap<String,Variables> vars = new HashMap<String,Variables>();
    ArrayList<NamedVariables> args = new ArrayList<NamedVariables>();

    public boolean dupCheck(String varName) {
        for (NamedVariables temp : args) {
            if (temp.name.equals(varName)) {
                return true;
            }
        }
        if (this.vars.containsKey(varName)) {
            return true;
        }
        return false;
    }
}
