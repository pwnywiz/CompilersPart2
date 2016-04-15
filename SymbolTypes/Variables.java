package SymbolTypes;

public class Variables {
    boolean isVar;   // Variable or Argument
    String varType;

    public String getVarType() {
        return varType;
    }

    public boolean getIsVar() {
        return isVar;
    }

    public void setIsVar(boolean isVar) {
        this.isVar = isVar;
    }

    public void setVarType(String varType) {
        this.varType = varType;
    }
}
