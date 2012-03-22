package anacrowd.paperanalysis.coveragefilters.filters;

import anacrowd.documentation.elements.ThreadElement;
import anacrowd.stackdb.DBInfo.PostInfo;

public class AnsweredFilter extends AbstractThreadFilter {

	public boolean Include(ThreadElement thread) 
	{
		// Gotta hit the DB! We don't have view count...
		//int answered = getDBInfoInstance().GetPostDetail(thread.Question.Id).AnswerCount;
		int answered = thread.Answers.size();
		return answered > 0;
	}

	public boolean Include(ThreadElement thread, PostInfo info) 
	{
		return info.AnswerCount > 0;
	}

}
