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
			FirstVisitor first = new FirstVisitor();
			Goal root = parser.Goal();
			root.accept(first, null);
			System.out.println("Checking finished");
		}
		catch(ParseException ex){
			System.out.println(ex.getMessage());
		}
		catch(FileNotFoundException ex){
			System.err.println(ex.getMessage());
		} catch (Exception e) {
			System.out.println("Semantics Error.");
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
