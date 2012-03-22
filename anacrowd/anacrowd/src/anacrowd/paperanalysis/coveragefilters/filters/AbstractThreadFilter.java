package anacrowd.paperanalysis.coveragefilters.filters;

import anacrowd.documentation.elements.ThreadElement;
import anacrowd.stackdb.DBInfo;
import anacrowd.stackdb.DBInfo.PostInfo;

public abstract class AbstractThreadFilter 
{
	private static DBInfo sharedInfo;
	
	protected static DBInfo getDBInfoInstance()
	{
		if( sharedInfo == null)
		{
			sharedInfo = new DBInfo();
			sharedInfo.Init();
		}
		return sharedInfo;
	}
	
	public abstract boolean Include(ThreadElement thread);
	public abstract boolean Include(ThreadElement thread, PostInfo info);
}