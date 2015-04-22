package spider;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class Context extends Thread{
	
	private String tag = null;
	public void setTag(String string) {
		tag = string;
	}
	public String getTag(){
		return tag;
	}
	
	//关键字
	private String keyWord = null;

	public String getKeyWord() {
		return keyWord;
	}

	public void setKeyWord(String keyWord) {
		this.keyWord = keyWord;
	}
	
	//输出文件路径
	private String outputFilePath = null;

	public String getOutputFilePath() {
		return outputFilePath;
	}

	public void setOutputFilePath(String outputFilePath) {
		this.outputFilePath = outputFilePath;
	}

	//最大线程数
	private int maxThreadNumber = 0;
	public void setMaxThreadNumber(int value) {
		this.maxThreadNumber = value;
	}

	public int getMaxThreadNumber() {
		return maxThreadNumber;
	}

	//数据库连接池初始化连接数
	private int dataBaseInitialSize = 10;
	public int getDataBaseInitialSize() {
		return dataBaseInitialSize;
	}

	public void setDataBaseInitialSize(int dataBaseInitialSize) {
		this.dataBaseInitialSize = dataBaseInitialSize;
	}

	//数据库连接池最大连接数
	private int dataBaseMaxConnectionSize = 50;
	public int getDataBaseMaxConnectionSize() {
		return dataBaseMaxConnectionSize;
	}

	public void setDataBaseMaxConnectionSize(
			int dataBaseMaxConnectionSize) {
		this.dataBaseMaxConnectionSize = dataBaseMaxConnectionSize;
	}

	private BasicDataSource ds = null;
	/**
     * 初始化数据库连接池
     */
    private void initDataBaseConnectionPool() {
        ds = new BasicDataSource();
        ds.setDriverClassName(Constant.dataBaseDriverClassName);
        ds.setUrl(Constant.dataBaseUrl);
        ds.setUsername(Constant.dataBaseUserName);
        ds.setPassword(Constant.dataBasePassWord);
        ds.setInitialSize(10);
        ds.setMaxIdle(dataBaseMaxConnectionSize);
        ds.setMinIdle(20);
    }   
    
    public synchronized BasicDataSource getDataBase(){
    	return ds;
    }
    
	//HttpClient对象
	private CloseableHttpClient httpClient = null;
	public HttpClient getHttpClient() {
		return httpClient;
	}
	
	//线程池最大线程数
		private int threadPoolMaxSize = 20;
	public int getThreadPoolMaxSize() {
			return threadPoolMaxSize;
		}

		public void setThreadPoolMaxSize(int threadPoolMaxSize) {
			this.threadPoolMaxSize = threadPoolMaxSize;
		}

		//线程池对象
		private ExecutorService threadPool = null;
		public ExecutorService getThreadPool() {
			return threadPool;
		}

		//解析队列
		private ThreadList resolveList = null;
		//保存队列
		private ThreadList saveList = null;
		//合并队列
		private ThreadList combateList = null;
		
		public synchronized ThreadList getResolveList() {
			return resolveList;
		}

		public synchronized ThreadList getSaveList() {
			return saveList;
		}

		public synchronized ThreadList getCombateList() {
			return combateList;
		}

	//索引
	private int nextIndex = 0;
	
	public synchronized void increaseNextIndex(){
		nextIndex ++;
	}
	
	private boolean runFlag = true;
	//开始运行
	private boolean isRunned = false;
	public void onStartRunning() {
		threadPool = Executors.newCachedThreadPool();
		saveList = new ThreadList(this);
		resolveList = new ThreadList(this);
		combateList = new ThreadList(this);
		//根据是否运行过初始化队列
		if(isRunned){
			initListFromSQL();
		}else{
			String url = "D:\\StackOverFlowSpider\\html\\Frequent java Questions - Stack Overflow.html";
			resolveList.addThread(new ResolveThread(url.hashCode(),
					url,
					ThreadInfo.ThreadType.SAVED_NEXT,
					this));
//			= "http://stackoverflow.com/questions/tagged/" + keyWord 
//					+ "?sort=" + tag + "&pagesize=50";
//			saveList.addThread(new SaveThread(url.hashCode(),
//					url,
//					ThreadInfo.ThreadType.UNSAVE_NEXT,
//					this));
			}
		
		runFlag = true;
		start();
	}

	private void initListFromSQL() {
		// TODO 
		saveList.initContent(ThreadInfo.ThreadType.UNSAVE,20);
		resolveList.initContent(ThreadInfo.ThreadType.SAVED,20);
		saveList.initContent(ThreadInfo.ThreadType.RESOLVED,20);
	}

	//停止运行
	public void onStopRunning() {
		runFlag = false;
		
		
		saveList.clear();
		resolveList.clear();
		combateList.clear();
		
		//TODO 等待线程运行完毕在关闭
		threadPool.shutdown();
		stop();
		//TODO 保存运行过的信息
	}

	public Context(){
		httpClient = HttpClients.createDefault();
		initDataBaseConnectionPool();
	}

	@Override
	public void run() {
while(runFlag){
			
			if(saveList.getRunningCount() <= threadPoolMaxSize){
				
				int leftThreadNum = threadPoolMaxSize - saveList.getRunningCount();
				for(int i = 0;i < leftThreadNum/3;i ++){
					ThreadInfo threadInfo = saveList.getThread(ThreadInfo.ThreadType.UNSAVE);
					if(threadInfo == null){
						System.out.println("class_Context_Method_run_saveList为空");
					}else{
						System.out.println(threadInfo.toString());
						threadPool.execute(threadInfo);
					}
					threadInfo = resolveList.getThread(ThreadInfo.ThreadType.SAVED);
					if(threadInfo == null){
						System.out.println("class_Context_Method_run_resolveList为空");
					}else{
						System.out.println(threadInfo.toString());
						threadPool.execute(threadInfo);
					}
					
					threadInfo = combateList.getThread(ThreadInfo.ThreadType.SAVED);
					if(threadInfo == null){
						System.out.println("class_Context_Method_run_combateList为空");
					}else{
						System.out.println(threadInfo.toString());
						threadPool.execute(threadInfo);
					}
				}
			}
		}
	}

	
}
