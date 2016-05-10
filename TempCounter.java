import SymbolTypes.Methods;
import SymbolTypes.SymbolTable;

import java.util.HashMap;
import java.util.Set;

public class TempCounter {
    HashMap<String, SymbolTable> Symboltable;

    int counter = 0;

    public TempCounter(HashMap<String, SymbolTable> Symboltable) {
        this.Symboltable = Symboltable;
    }

    public void maxArgs() {
        int maxArgs = 0;
        int tempArgs = 0;
        Set<String> classNames = this.Symboltable.keySet();

        for (String classname : classNames) {
            Set<String> methods = this.Symboltable.get(classname).getMethodsMap().keySet();

            for (String methodname : methods) {
                if ((tempArgs = this.Symboltable.get(classname).getMethodsMap().get(methodname).args.size()) > maxArgs) {
                    maxArgs = tempArgs;
                }
            }
        }

        System.out.println("Max method arguments = " + maxArgs);
        counter = maxArgs + 1;
    }

    public int getTemp() {
        counter++;

        return counter;
    }
}
