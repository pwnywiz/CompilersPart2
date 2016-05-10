import SymbolTypes.Methods;

import java.util.HashMap;
import java.util.Set;

public class TempCounter {
    HashMap<String,Methods> methods;

    int counter = 0;

    public TempCounter(HashMap<String,Methods> methods) {
        this.methods = methods;
    }

    public void maxArgs() {
        int maxArgs = 0;
        int tempArgs = 0;
        Set<String> methodNames = methods.keySet();

        for (String methodname : methodNames) {
            if ((tempArgs = methods.get(methodname).args.size()) > maxArgs) {
                maxArgs = tempArgs;
            }
        }

        System.out.println("Max method arguments = " + maxArgs);
        counter = maxArgs;
    }
}
