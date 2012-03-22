package anacrowd.paperanalysis.coveragefilters.filters;

import java.util.List;

import anacrowd.documentation.elements.ThreadElement;
import anacrowd.stackdb.DBInfo.PostInfo;

public class AtLeastNFilter extends AbstractThreadFilter
{
	List<AbstractThreadFilter> Filters;
	int atLeast;
	public AtLeastNFilter(List<AbstractThreadFilter> filters, int n)
	{
		this.Filters = filters;
		this.atLeast = n;
	}
	
	@Override
	public boolean Include(ThreadElement thread) 
	{
//		int passed = 0;
//		for( AbstractThreadFilter f : Filters )
//		{
//			if( f.Include(thread) )
//			{
//				passed++;
//			}
//			if( passed >= atLeast) // Skip out early if possible.
//				return true;
//		}
//		return passed >= atLeast;
		return IncludeOptimized(thread);
	}
	
	public boolean IncludeOptimized(ThreadElement thread)
	{
		int passed = 0;
		PostInfo info = getDBInfoInstance().GetPostDetail(thread.Question.Id);
		for( AbstractThreadFilter f : Filters )
		{
			if( f.Include(thread, info) )
			{
				passed++;
			}
			if( passed >= atLeast) // Skip out early if possible.
				return true;
		}
		return passed >= atLeast;
	}
	
	public boolean Include(ThreadElement thread, PostInfo info)
	{
		throw new RuntimeException("Not implemented");
	}
}
