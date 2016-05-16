import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class vCreate {
    HashMap<String,ArrayList<String>> vVariables = new HashMap<String,ArrayList<String>>();
    HashMap<String,ArrayList<String>> vMethods = new HashMap<String,ArrayList<String>>();

    HashMap<String,ArrayList<String>> VariablesSorted;
    HashMap<String,ArrayList<String>> MethodsSorted;
    HashMap<String,String> ClassMap;

    String currentClass;
    HashMap<String,String> insertedMethods;


    public vCreate(HashMap<String,ArrayList<String>> VariablesSorted, HashMap<String,ArrayList<String>> MethodsSorted, HashMap<String,String> ClassMap) {
        this.VariablesSorted = VariablesSorted;
        this.MethodsSorted = MethodsSorted;
        this.ClassMap = ClassMap;
    }

    public HashMap<String,ArrayList<String>> getvVariables() {
        return vVariables;
    }

    public HashMap<String,ArrayList<String>> getvMethods() {
        return vMethods;
    }

    public void CreateVariablesTable() {
        Set<String> classes = ClassMap.keySet();

        for (String temp : classes) {
            if (ClassMap.get(temp) != null && ClassMap.get(temp).equals("-main-")) {
                classes.remove(temp);
                break;
            }
        }

        for (String clss : classes) {
            currentClass = clss;
            insertedMethods = new HashMap<String,String>();
            vVariables.put(clss, new ArrayList<String>());
            vMethods.put(clss, new ArrayList<String>());
            fillVariables(clss);
            fillMethods(clss);
        }

//        for (String temp2 : vVariables.get("B")) {
//            System.out.println(temp2);
//        }
//
//        for (String temp3 : vMethods.get("B")) {
//            System.out.println(temp3);
//        }
    }

    public void fillVariables(String className) {
        if (this.ClassMap.get(className) != null) {
            fillVariables(this.ClassMap.get(className));
        }

        ArrayList<String> tempVars = new ArrayList<String>();
        for (String temp : VariablesSorted.get(className)) {
            tempVars.add(className + "_" + temp);
        }

        vVariables.get(currentClass).addAll(tempVars);
    }

    public void fillMethods(String className) {
        if (this.ClassMap.get(className) != null) {
            fillMethods(this.ClassMap.get(className));
        }

        int listIndex;
        String inheritClass;
        ArrayList<String> tempMethods = new ArrayList<String>();
        for (String temp : MethodsSorted.get(className)) {
            if ((inheritClass = insertedMethods.get(temp)) != null) {
                listIndex = vMethods.get(currentClass).indexOf(inheritClass + "_" + temp);
                vMethods.get(currentClass).set(listIndex,className + "_" + temp);
                insertedMethods.put(temp,className);
            }
            else {
                tempMethods.add(className + "_" + temp);
                insertedMethods.put(temp,className);
            }
        }

        vMethods.get(currentClass).addAll(tempMethods);
    }
}
