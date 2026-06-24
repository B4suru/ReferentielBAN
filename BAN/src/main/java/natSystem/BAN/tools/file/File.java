package natSystem.BAN.tools.file;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


public class File {
	private String fileName;
	
	public File() {
		super();
	}
	
	public File(String fileName) {
		super();
		this.fileName = fileName;
	}

	public String getFileName() {
		return fileName;
	}
	
	public void setFileName(String fileName) {
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
	
	public boolean isCSVExisting() {
		Path chemin = Path.of(fileName);
		if (!Files.isRegularFile(chemin)
		        || !chemin.getFileName().toString().toLowerCase().endsWith(".csv")) {
		    System.out.println("Ce n'est pas un fichier CSV.");
		}
		return Files.isRegularFile(chemin) && chemin.getFileName().toString().toLowerCase().endsWith(".csv");
	}
	
}
