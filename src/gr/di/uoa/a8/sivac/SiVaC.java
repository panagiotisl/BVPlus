package gr.di.uoa.a8.sivac;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class SiVaC {

	private static final int D = 2;

	
	private static void createNoDFile(String filename) throws IOException
	{
		BufferedReader br = new BufferedReader(new FileReader(filename));
		File file = new File(filename+".noD"+D);
		if (!file.exists()) {
			file.createNewFile();
		}
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		String line;
		while ((line = br.readLine()) != null) {
			String[] temp = line.split("\\s+");
			int a = Integer.parseInt(temp[0]);
			int b = Integer.parseInt(temp[1]);
			if(a>=b-D && a<=b+D)
			{
				
			}
			else
			{
				bw.write(line+'\n');	
			}
			
		}
		br.close();
		bw.close();
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {

		System.out.println("SiVaC");
		createNoDFile("cnr-2000/cnr-2000.txt");

	}

}
