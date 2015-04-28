package spider;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public abstract class ThreadInfo implements Runnable{
	private int contentType;
	public int getContentType(){
		return contentType;
	}
	//线程是否运行
	protected boolean isRunned = true;
	//hashCode
	protected long hashCode = -1L;
	//线程类型
	protected int threadType = 0;
	//path
	protected String path = null;
	//上下文
	protected Context context = null;


	/**
	 * 更新线程正在运行的信息
	 */
	public void updateThreadRunningInfo() {
		++threadType ;
		insertToSQL();
	}

	/**
	 * 更新线程运行失败的信息
	 */
	public void updateThreadRunFailInfo(){
		--threadType;
		insertToSQL();
	}

	public static final class ThreadType{
		public static final int UNSAVE = 0;
		public static final int SAVING = 1;
		public static final int SAVED = 2;
		public static final int RESOLVING = 3;
		public static final int RESOLVED = 4;
		public static final int COMBATING = 5;
		public static final int COMBATED = 6;
	}
	
	
	
	public ThreadInfo(long hashCode,String path,int threadType,int contentType,Context context){
		this.hashCode = hashCode;
		
		this.path = path;
		this.threadType = threadType;
		this.contentType = contentType;
		this.context = context;
		
		printThreadInfo("生成");
		insertToSQL();
	}



	/**
	 * 格式化待插入到数据库中的字符串，使其符合SQL格式
	 * @param string 待转化的sql
	 */
	private String formatStringToSQL(String string){

		Map<String,String> replaceMap = new HashMap<>();
		replaceMap.put("'","’");
		replaceMap.put("\"","“");
		replaceMap.put("\\","\\\\");
		replaceMap.put("%","百分号");
		replaceMap.put("?","？");
		replaceMap.put("_", "下划线");
		Iterator iterator = replaceMap.keySet().iterator();
		String key = null;

		while(iterator.hasNext()){
			key = (String)iterator.next();
			string = string.replace(key, replaceMap.get(key));
		}

		return string;
	}
	protected void printThreadInfo(String extendMsg){
		String tag = null;
		switch(threadType){
			case ThreadType.RESOLVED:
				tag = "合并线程";
				break;
			case ThreadType.SAVED:
				tag = "解析线程";
				break;
			case ThreadType.UNSAVE:
				tag = "保存线程";
				break;

		}
		System.out.println("HashCode值" + hashCode
				+"\t路径" + path
				+ "\t内容类型" + (contentType==0?"索引":"正文")
				+ "\t线程类型" + tag + "\t" + extendMsg);
	}
	
	protected void printThreadInfoError(String extendsMsg,Exception e){
		printThreadInfo(extendsMsg);
		System.out.println("HashCode值" + hashCode
				+"\t路径" + path
				+ "\t线程类型" + threadType 
				+ "\t" + e.getCause() + "\t" + e.getMessage());
	}
	
	public void insertToSQL() {
		try {
			Connection connection = context.getDataBase().getConnection();
			Statement stmt = (Statement) connection.createStatement();

			String sqlStr = "INSERT INTO thread (path,threadtype,contenttype,hashcode) VALUES('"
					+ formatStringToSQL(path) +"','" + threadType +"','" + contentType +"','" + hashCode +"')"
					+ " ON DUPLICATE KEY UPDATE path = '"+ formatStringToSQL(this.path) +"', threadtype = "+ this.threadType + ";";

			System.out.println("SQL插入语句" + sqlStr);
			stmt.execute(sqlStr);
			stmt.close();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public void updateInfoStr1() {

		try {
			Connection connection = context.getDataBase().getConnection();
			Statement stmt = (Statement) connection.createStatement();
			String sqlStr = "UPDATE thread SET path = '" + formatStringToSQL(path) +"',threadtype = '"+ ++threadType + "' WHERE hashcode= '"+ hashCode + "';";
			stmt.execute(sqlStr);
			stmt.close();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 更新正在运行的线程数目
	 */
	public void updateThreadCount(){
		ThreadList.decreseRunningCount();
	}

	public final static class ContentType {

		public static final int INDEX = 0;
		public static final int CONTENT = 1;
	}
}
