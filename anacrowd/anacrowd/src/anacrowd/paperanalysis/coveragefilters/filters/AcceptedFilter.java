package anacrowd.paperanalysis.coveragefilters.filters;

import anacrowd.documentation.elements.ThreadElement;
import anacrowd.stackdb.DBInfo.PostInfo;

public class AcceptedFilter extends AbstractThreadFilter 
{
	public boolean Include(ThreadElement thread) 
	{
		return thread.Question.AcceptedAnswerId != 0;
	}
	
	public boolean Include(ThreadElement thread, PostInfo info) 
	{
		return thread.Question.AcceptedAnswerId != 0;
	}
}
