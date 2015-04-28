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
	/**
	 * 文件保存线程
	 * @param hashCode hash值
	 * @param path 路径
	 * @param threadType 线程类型
	 * @param context 上下文
	 */
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
    	   printThreadInfoError("打开文件" + path + "失败",e);
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
			printThreadInfo("开始解析");

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
	                	System.out.println("TAG_链接" + node.getLink());
	                }
	            }

                break;
                
                case ContentType.CONTENT:
                	//TODO Html解析为Word
                	String outputFilePath = writeWordFile(contentStr);
					printThreadInfo("解析结束");
					new CombateThread(hashCode,outputFilePath, ThreadType.RESOLVED,ContentType.CONTENT,context);
                	break;
            
            }

            this.insertToSQL();
        } catch (ParserException ex) {
			this.updateThreadRunFailInfo();
        	printThreadInfoError("解析失败",ex);
            ex.printStackTrace();
        }
        
        
   }

	public static void createPDF(String htmlFileName,String outputFileName){
		// step 1
//		Document document = new Document();
//		// step 2
//		PdfWriter writer = null;
//		try {
//			writer = PdfWriter.getInstance(document, new FileOutputStream(outputFileName));
//		} catch (DocumentException e) {
//			e.printStackTrace();
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}
//		// step 3
//		document.open();
//		// step 4
//
//
////		XMLWorkerHelper.getInstance().parseXHtml(writer, document,
////				new FileInputStream(htmlFileName));
//		//step 5
//		document.close();

		System.out.println( "PDF Created!" );
	}


		public String writeWordFile(String contentStr) {
			printThreadInfo("解析结束");

			int pathSeparatorIndex = path.lastIndexOf("\\");
			String outputFilePath = path.substring(0,pathSeparatorIndex);
			String outputFileName = path.substring(pathSeparatorIndex + 1) +".doc";
			String outputFile = outputFilePath + outputFileName;

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

	public static String convertHtmlToPdf(String inputFile)
			throws Exception {
		int pathSeparatorIndex = inputFile.lastIndexOf("\\");
		String outputFilePath = inputFile.substring(0,pathSeparatorIndex);
		String outputFileName = inputFile.substring(pathSeparatorIndex + 1) +".pdf";
		String outputFile = outputFilePath + outputFileName;
		OutputStream os = new FileOutputStream(outputFile);
		ITextRenderer renderer = new ITextRenderer();
		String url = new File(inputFile).toURI().toURL().toString();

		renderer.setDocument(url);

		// 解决中文支持问题
		//ITextFontResolver fontResolver = renderer.getFontResolver();
		// fontResolver.addFont("C:/Windows/Fonts/SIMSUN.TTC", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
		//解决图片的相对路径问题
		// renderer.getSharedContext().setBaseURL("file:/D:/");
		renderer.layout();
		renderer.createPDF(os);

		os.flush();
		os.close();
		return outputFile;
	}
}
