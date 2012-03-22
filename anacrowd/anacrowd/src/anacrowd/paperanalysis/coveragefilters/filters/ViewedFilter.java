package anacrowd.paperanalysis.coveragefilters.filters;

import anacrowd.documentation.elements.ThreadElement;
import anacrowd.stackdb.DBInfo.PostInfo;

public class ViewedFilter extends AbstractThreadFilter 
{
	private int MinViews;
	public ViewedFilter(int minViews)
	{
		this.MinViews = minViews;
	}
	@Override
	public boolean Include(ThreadElement thread) 
	{
		// Gotta hit the DB! We don't have view count...
		//int views = getDBInfoInstance().GetPostDetail(thread.Question.Id).ViewCount;
		int views = thread.ViewCount;
		return views >= this.MinViews;
	}
	
	public boolean Include(ThreadElement thread, PostInfo info) 
	{
		return info.ViewCount >= this.MinViews;
	}
}