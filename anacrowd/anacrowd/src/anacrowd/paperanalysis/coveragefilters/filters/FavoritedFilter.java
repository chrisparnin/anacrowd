package anacrowd.paperanalysis.coveragefilters.filters;

import anacrowd.documentation.elements.ThreadElement;
import anacrowd.stackdb.DBInfo.PostInfo;

public class FavoritedFilter extends AbstractThreadFilter 
{
	@Override
	public boolean Include(ThreadElement thread) 
	{
		return thread.FavoriteCount > 0;
		// Gotta hit the DB! We don't have favs count...
		//int Favs = getDBInfoInstance().GetPostDetail(thread.Question.Id).FavoriteCount;
 		//return Favs > 0;
	}
	
	public boolean Include(ThreadElement thread, PostInfo info) 
	{
		return info.FavoriteCount > 0;
	}

}