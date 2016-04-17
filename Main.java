import syntaxtree.*;
import visitor.*;
import java.io.*;

class Main {
	public static void main (String [] args){
		if(args.length != 1){
			System.err.println("Usage: java Driver <inputFile>");
			System.exit(1);
		}
		FileInputStream fis = null;
		try{
			fis = new FileInputStream(args[0]);
			MiniJavaParser parser = new MiniJavaParser(fis);
			System.err.println("Program parsed successfully.");
			Goal root = parser.Goal();

			ClassVisitor first = new ClassVisitor();
			root.accept(first, null);

			SymbolVisitor second = new SymbolVisitor(first.getClassMap());
			root.accept(second, null);

			TypeVisitor third = new TypeVisitor(first.getClassMap(), second.getSymboltable());
			root.accept(third,null);

			System.out.println("Checking ended successfully");
		}
		catch(ParseException ex){
			System.out.println(ex.getMessage());
		}
		catch(FileNotFoundException ex){
			System.err.println(ex.getMessage());
		} catch (Exception e) {
			System.out.println("Exception Error");
			System.out.println();
		} finally {
			try {
				if(fis != null) fis.close();
			}
			catch(IOException ex){
				System.err.println(ex.getMessage());
			}
		}
	}
}
