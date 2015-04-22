package spider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;



public class SaveThread extends ThreadInfo {
	private static final int HTTP_OK = 200;
    private static final int HTTP_BLOCK = 202;
    
    private CloseableHttpClient httpClient = null;
    
    /**
     * 文件保存线程
     * @param hashCode hash值
     * @param path 路径
     * @param threadType 线程类型
     * @param context 上下文
     */
	public SaveThread(long hashCode,String path,int threadType,Context context){
		super(hashCode, formatPath(path), threadType, context);
		printThreadInfo(path);
		printThreadInfo(formatPath(path));
	}
	

	public static String formatPath(String path){
		// \/:*?"<>|
		String[] replaceArray = new String[]{"\\","/",":","*","?","\"","<",">","|"};
		System.out.println(path);
		String replace = path.substring(path.lastIndexOf("/") + 1);
		for(int i = 0; i < replaceArray.length; i ++){
			replace = replace.replace(replaceArray[i], "_");
			System.out.println(replace);
		}
		String result = path.subSequence(0,(int)path.lastIndexOf("/") + 1) + replace;
		System.out.println(result);
		return result;
	}
	
	@Override
	public void run() {
		httpClient = (CloseableHttpClient) context.getHttpClient();
		HttpGet httpget = new HttpGet(path);
		CloseableHttpResponse response = null;
		try {
			response = httpClient.execute(httpget);
			int code = response.getStatusLine().getStatusCode();
                        switch(code){
                        //TODO 文件名的问题
                            case HTTP_OK:
                                HttpEntity httpEntity = response.getEntity();
                                InputStream inputStream = httpEntity.getContent();
                                String filePath = Constant.applicationPath
                                        + "html" + File.separator
                                        + path.substring(path.lastIndexOf("/")+1) + ".html";
                                FileOutputStream fileOutputStream = null;
                                try{
                                    fileOutputStream = new FileOutputStream(filePath);
                                }catch(Exception e){
                                	printThreadInfoError("",e);
                                	e.printStackTrace();
                                }
                                int tempByte = -1;
                                while((tempByte = inputStream.read()) > 0){
                                    fileOutputStream.write(tempByte);
                                }
                                
                                path = filePath;
                                //关闭输入流
                                if(inputStream != null){
                                    inputStream.close();
                                }
                                if(fileOutputStream != null){
                                    fileOutputStream.close();
                                }
                                
                                printThreadInfo("");
                                context.getResolveList().addThread(new ResolveThread(hashCode,path,++threadType,context));
                                this.updateInfoStr();
                                printThreadInfo("");
                                break;
                            case HTTP_BLOCK:
                            	printThreadInfo("网络连接失败");
                                break;
                        }
			
		} catch (Exception e) {
			printThreadInfoError("保存失败",e);
			e.printStackTrace();
		} 
		
		ThreadList.decreseRunningCount();
	}
}