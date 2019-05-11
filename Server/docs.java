import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ListIterator;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

public class docs {
	public void mkdoc(ArrayList<String> content,String path, String fname) throws FileNotFoundException, IOException, InvalidFormatException {

		XWPFDocument document = new XWPFDocument();
		XWPFParagraph tmpParagraph = document.createParagraph();
		XWPFRun tmpRun = tmpParagraph.createRun();
		
		String tmp = "";
		ListIterator<String> iterator = content.listIterator();
		tmpRun.setFontSize(12);
		
		while(iterator.hasNext()) {
			tmpRun.setText(iterator.next());
			tmpRun.addBreak();
		}

		File dir = new File(path);

		if (!dir.exists()) {//make directory when it doesn't exist.
			dir.mkdirs();
		}

		File saveFile = new File(path +"/"+ fname +"/seokee.doc");

		FileOutputStream fos = new FileOutputStream(saveFile);
		document.write(fos);
		fos.close();
	}
	
	public void mktxt(ArrayList<String> content,String path, String fname) {
        try{
		File dir = new File(path);
		File saveFile = new File(path +"/"+ fname +".txt");
		
		if (!dir.exists()) {//make directory when it doesn't exist.
			dir.mkdirs();
		}
		BufferedWriter fw = new BufferedWriter(new FileWriter(saveFile, true));

		ListIterator<String> iterator = content.listIterator();
		
		while(iterator.hasNext()) {
			fw.write(iterator.next());
			fw.newLine();
		}
		

		fw.flush();
		fw.close();
        }catch(Exception e){
            e.printStackTrace();
        }
	}
	
	

}
