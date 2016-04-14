package SymbolTypes;

import java.util.HashMap;

public class SymbolTable {
    HashMap<String,Variables> VariablesMap = new HashMap<String,Variables>();
    HashMap<String,Methods> MethodsMap = new HashMap<String,Methods>();

    public void addVariables(HashMap<String,Variables> VariablesMap) {
        this.VariablesMap = VariablesMap;
    }

    public void addMethods(HashMap<String,Methods> MethodsMap) {
        this.MethodsMap = MethodsMap;
    }
}
