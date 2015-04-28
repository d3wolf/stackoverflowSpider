package spider;

public class CombateThread extends ThreadInfo{
	/**
     * 文件合并线程
     * @param hashCode hash值
     * @param path 路径
     * @param threadType 线程类型
     * @param context 上下文
     */
	public CombateThread(long hashCode,String path,int threadType,int contentType,Context context){
		super(hashCode, path, threadType,contentType, context);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
}
