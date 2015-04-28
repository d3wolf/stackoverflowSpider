package spider;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ThreadList{
	
	//线程数
	private static int runningCount = 0;
	//线程数锁
	private static Object runningCountLock = new Object();
	
	/**
	 * 获得线程数
	 * @return
	 */
	public static int getRunningCount() {
		synchronized(runningCountLock){
			return runningCount;
		}
	}
	
	/**
	 * 线程数自增
	 */
	public static void increseRunningCount() {
		synchronized(runningCountLock){
			runningCount ++;
		}
	}


	
	/**
	 * 线程数减少
	 */
	public static void decreseRunningCount() {
		synchronized(runningCountLock){
			runningCount --;
		}
	}
	
	//List对象
	List<ThreadInfo> list = null;
	//线程锁
	private Object listLock = new Object();

	private int listType;
	static final class ListType{
		public static final int SAVE_LIST = 0;
		public static final int RESOLVE_LIST = 1;
		public static final int COMBATE_LIST = 2;
	}

	Context context = null;
	public ThreadList(Context context,int ListType){
		synchronized (listLock) {
			list = new ArrayList<ThreadInfo>();
		}
		this.context = context;
		this.listType = ListType;
	}

	/**
	 * 添加线程到队列中
	 * @param threadInfo
	 */
	public synchronized void addThread(ThreadInfo threadInfo){
		synchronized(listLock){
			list.add(threadInfo);
			String log = null;
			switch(listType){
				case ListType.SAVE_LIST:
					log = "文件保存队列";
					break;
				case ListType.RESOLVE_LIST:
					log = "文件解析队列";
					break;
				case ListType.COMBATE_LIST:
					log = "文件合并队列";
					break;
			}
			threadInfo.updateThreadRunningInfo();
			threadInfo.printThreadInfo("\t 已经添加到" + log + "中\t队列大小为" + list.size());
		}
	}

	/**
	 * 清除Thread队列
	 */
	public void clear(){
		synchronized (listLock) {
			System.out.println(list.size());
			list.clear();
		}
	}

	/**
	 * 获取线程
	 */
	public synchronized ThreadInfo getThread() {
	
		synchronized (listLock) {
			ThreadInfo threadInfo = null;

			if (list.size() <= 0) {
				int threadTypes = -1;
				switch (listType){
					case ListType.SAVE_LIST:
						threadTypes = ThreadInfo.ThreadType.UNSAVE;
						break;
					case ListType.RESOLVE_LIST:
						threadTypes = ThreadInfo.ThreadType.SAVED;
						break;
					case ListType.COMBATE_LIST:
						threadTypes = ThreadInfo.ThreadType.RESOLVED;
						break;
				}
				initContent(threadTypes, 20);
			}

			if (list.size() > 0) {
				threadInfo =  list.get(0);
				list.remove(0);
				increseRunningCount();
				printThreadListInfo();
			}
			return threadInfo;
		}
	}

	private String printThreadListInfo(String msg){
		return msg + "\t" + printThreadListInfo();
	}
	private String printThreadListInfo() {
		String listTypeStr = "";
		switch(listType){
			case ListType.SAVE_LIST:
				listTypeStr = "保存队列";
				break;
			case ListType.RESOLVE_LIST:
				listTypeStr = "解析队列";
				break;
			case ListType.COMBATE_LIST:
				listTypeStr = "合并队列";
				break;
		}
		String result = "ListType\t" + listTypeStr + "\t大小\t" + list.size();

		return result;
	}

	public synchronized void initContent(int threadType,int size) {
		//从数据库中初始化线程信息
		try {
			Connection connection = context.getDataBase().getConnection();
			Statement stmt = (Statement) connection.createStatement();
			String sqlStr = 
					"SELECT path,threadtype,hashcode,contenttype FROM thread WHERE threadtype = '"
							+ threadType +"' LIMIT " + size;

			ResultSet rs = stmt.executeQuery(sqlStr);

			while (rs.next()) {
					System.out.println("进入队列");
					ThreadInfo threadInfo = null;
					switch (threadType) {
						case ThreadInfo.ThreadType.UNSAVE:
							threadInfo = new SaveThread(rs.getLong("hashcode"),
									rs.getString("path"),
									rs.getInt("threadtype"),
									rs.getInt("contenttype"),
									context);
							context.getSaveList().addThread(threadInfo);
							break;
						case ThreadInfo.ThreadType.SAVED:
							threadInfo = new ResolveThread(rs.getLong("hashcode"),
									rs.getString("path"),
									rs.getInt("threadtype"),
									rs.getInt("contenttype"),
									context);
							context.getResolveList().addThread(threadInfo);
							break;
						case ThreadInfo.ThreadType.COMBATED:
							threadInfo = new CombateThread(rs.getLong("hashcode"),
									rs.getString("path"),
									rs.getInt("threadtype"),
									rs.getInt("contenttype"),
									context);
							context.getCombateList().addThread(threadInfo);
							break;
					}
			}

			stmt.close();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
