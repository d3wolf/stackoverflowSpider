package spider;


import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.*;

public class ResolveThread extends ThreadInfo{
	   private String contentStr = "";

	public ResolveThread(long hashCode,String path,int threadType,int contentType,Context context){
		super(hashCode, path, threadType, contentType, context);
	}

	@Override
	public void run() {

		resolve();
		updateThreadCount();
		ThreadList.decreseRunningCount();
	}
	private void resolve(){
	       contentStr = openFile(path);
	       resolverStr();
	       
	    }
	 
    private String openFile( String path ){
       try {
           BufferedReader bis = new BufferedReader(new InputStreamReader(new FileInputStream( new File(path)),"utf-8" ) );
           String szContent="";
           String szTemp;
           
           while ( (szTemp = bis.readLine()) != null) {
               szContent+=szTemp+"\n";
           }
           bis.close();
           return szContent;
       }
       catch( Exception e ) {
		   this.updateThreadRunFailInfo();

    	   e.printStackTrace();
           return null;
       }
   }
    
    private void resolverStr(){
		System.out.println();
        try {
            Parser parser = new Parser(contentStr);
            NodeFilter filter = new NodeClassFilter(LinkTag.class);
            NodeList list = parser.extractAllNodesThatMatch(filter);

			switch(this.getContentType()){
            case ContentType.INDEX:
            	for(int i = 0;i < list.size();i ++){
	                LinkTag node =(LinkTag)list.elementAt(i);
	                if(node.getLinkText().trim().equals("next")){
	                	String new_Path = "http://stackoverflow.com" + node.getLink();
						System.out.println("TAG_next" + new_Path);
	                	long hashcode = new_Path.hashCode();
	                	context.increaseNextIndex();
	                	new SaveThread(hashcode,new_Path,ThreadInfo.ThreadType.UNSAVE, ContentType.INDEX,context);
	                }else if(node.getAttribute("class")!= null && node.getAttribute("class").equals("question-hyperlink")){

	                	String new_Path = "http://stackoverflow.com" +  node.getLink();
						System.out.println("TAG_class" + new_Path);
	                	long hashcode = new_Path.hashCode();
	                	new SaveThread(hashcode,new_Path,ThreadInfo.ThreadType.UNSAVE, ContentType.CONTENT,context);
	                }else{

	                }
	            }

                break;
                
                case ContentType.CONTENT:

                	String outputFilePath = writeWordFile(contentStr);

					new CombateThread(hashCode,outputFilePath, ThreadType.RESOLVED,ContentType.CONTENT,context);
                	break;
            
            }

            this.insertToSQL();
        } catch (ParserException ex) {
			this.updateThreadRunFailInfo();

            ex.printStackTrace();
        }
        
        
   }

		public String writeWordFile(String contentStr) {


			int pathSeparatorIndex = path.lastIndexOf("\\");
			String outputFilePath = context.getOutputFilePath();

			File outputPath = new File(outputFilePath);
			if(!(outputPath.exists())){
				outputPath.mkdir();
			}

			String outputFileName = path.substring(pathSeparatorIndex + 1) +".doc";
			String outputFile = outputFilePath + "\\"+ outputFileName;

			try {
				byte b[] = contentStr.getBytes();
				ByteArrayInputStream bais = new ByteArrayInputStream(b);
				POIFSFileSystem poifs = new POIFSFileSystem();
				DirectoryEntry directory = poifs.getRoot();
				DocumentEntry documentEntry = directory.createDocument("WordDocument", bais);
				FileOutputStream ostream = new FileOutputStream(outputFile);
				poifs.writeFilesystem(ostream);
				bais.close();
				ostream.close();
			} catch (Exception e) {
				this.updateThreadRunFailInfo();
				e.printStackTrace();
			}
			return outputFile;
		}

}
