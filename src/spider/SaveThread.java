package spider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
	 * @param contentType 内容类型
     * @param context 上下文
     */
	public SaveThread(long hashCode,String path,int threadType,int contentType,Context context){
		super(hashCode, path, threadType,contentType, context);
	}


	/**
	 * 格式化文件名以保存到磁盘
	 * @param fileName 文件名
	 * @return 格式化后的文件名
	 */
	public static String formatFileName(String fileName){
//		星号 (*)
//		竖线 (|)
//		反斜杠 (\)
//		冒号 (:)
//		双引号 (“)
//		小于号 (<)
//		大于号 (>)
//		问号 (?)
//		正斜杠 (/)
		String[] replaceArray = new String[]{"\\","/",":","*","?","\"","<",">","|"};


		String temp = fileName.substring(fileName.lastIndexOf(File.pathSeparatorChar) + 1);

		for(int i = 0; i < replaceArray.length; i ++){
			fileName = fileName.replace(replaceArray[i], "_");
		}


		return fileName;
	}

	/**
	 * 格式化路径
	 */
	public String formatPath(String path){


			Map<String,String> replaceMap = new HashMap<>();
			replaceMap.put("’","'");
			replaceMap.put("“","\"");
			replaceMap.put("\\\\","\\");
			replaceMap.put("百分号","%");
			replaceMap.put("？","?");
			replaceMap.put("下划线","_");
			Iterator iterator = replaceMap.keySet().iterator();
			String key = null;

			while(iterator.hasNext()){
				key = (String)iterator.next();
				path = path.replace(key, replaceMap.get(key));
			}

			return path;
		}

	@Override
	public void run() {

		httpClient = (CloseableHttpClient) context.getHttpClient();

		HttpGet httpget = new HttpGet(formatPath(path));

		CloseableHttpResponse response = null;
		try {
			response = httpClient.execute(httpget);
			int code = response.getStatusLine().getStatusCode();
                        switch(code){
                        //TODO 文件路径要修改的问题
                            case HTTP_OK:
                                HttpEntity httpEntity = response.getEntity();
                                InputStream inputStream = httpEntity.getContent();
                                String filePath = Constant.applicationPath
                                        + "html" + File.separator
                                        + formatFileName(path.substring(path.lastIndexOf("/") + 1)) + ".html";

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

								updateThreadCount();
								printThreadInfo("运行结束");
                                new ResolveThread(hashCode, filePath, ++threadType,this.getContentType(),context);

								break;
                            case HTTP_BLOCK:
                            	printThreadInfo("网络连接失败");

								this.updateThreadRunFailInfo();
                                break;
                        }
			
		} catch (Exception e) {

			this.updateThreadRunFailInfo();

			printThreadInfoError("保存失败",e);
			e.printStackTrace();
		} 
		

	}
}