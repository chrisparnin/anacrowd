package anacrowd.paperanalysis.coveragefilters.filters;

import anacrowd.documentation.elements.AnswerElement;
import anacrowd.documentation.elements.ThreadElement;
import anacrowd.stackdb.DBInfo.PostInfo;

public class BountiedFilter extends AbstractThreadFilter 
{
	@Override
	public boolean Include(ThreadElement thread) 
	{
		// Gotta hit the DB! No bounty info in model.
		int threadBounty = 0;
		// Need to get answers....because they have the bounties...
		//for( PostInfo ans : getDBInfoInstance().Answers(thread.Question.Id) )
		for( AnswerElement ans : thread.Answers)
		{
			//Integer amount = getDBInfoInstance().GetBountyAmount(ans.Id);
			Integer amount = ans.BountyAmount;
			if( amount != null )
			{
				threadBounty += amount;
			}
		}
		return threadBounty > 0;
	}

	public boolean Include(ThreadElement thread, PostInfo info) 
	{
		return Include(thread);
	}
}
