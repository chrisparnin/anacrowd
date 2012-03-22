package anacrowd.paperanalysis.coveragefilters.filters;

import anacrowd.documentation.elements.AnswerElement;
import anacrowd.documentation.elements.ThreadElement;
import anacrowd.stackdb.DBInfo.PostInfo;

public class VotedFilter extends AbstractThreadFilter 
{
	private int MinVotes;
	public VotedFilter(int minVotes)
	{
		this.MinVotes = minVotes;
	}
	@Override
	public boolean Include(ThreadElement thread) 
	{
		int total = thread.Votes;
		if( thread.Votes >= MinVotes )
			return true;
		// Gotta hit the DB! We only have linked answers..
		//for( PostInfo info : getDBInfoInstance().Answers(thread.Question.Id) )
		for( AnswerElement info : thread.Answers)
		{
			total += info.Votes;
		}
		return total >= MinVotes;
	}
	
	public boolean Include(ThreadElement thread, PostInfo info) 
	{
		return Include(thread);
	}
}
