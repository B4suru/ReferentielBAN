package natSystem.BAN.file;

import java.io.FileWriter;
import java.io.IOException;

public class File {
	private String fileName;
	
	public File(String fileName) {
		super();
		this.fileName = fileName;
	}

	public void write(String text) {
		try {
	        FileWriter writer = new FileWriter(this.fileName, true);
	        writer.write(text + '\n');
	        writer.close();
	
	    } catch (IOException e) {
	        System.err.println("Le texte n'a pas pu etre écris dans le fichier : " + this.fileName);
	        e.printStackTrace();
	    }

	}
}
