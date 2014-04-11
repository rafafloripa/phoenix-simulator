package simulator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;


public class StorageFileReader implements ModuleInterface  {
	private File file;
	private BufferedReader br;
	private LinkedList<String> fileLines;
	
	public StorageFileReader(File file){
		this.file = file;
	}
	
	public void readFile() throws IOException {
		br = new BufferedReader(new FileReader(file));
		String newLine = "";
		while((newLine = br.readLine()) != null){
			fileLines.add(newLine);
		}
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}
	
}
