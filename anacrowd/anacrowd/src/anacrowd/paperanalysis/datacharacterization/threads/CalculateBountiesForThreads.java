package anacrowd.paperanalysis.datacharacterization.threads;

import java.util.List;

import anacrowd.paperanalysis.BaseAnalysis;
import anacrowd.stackdb.DBInfo;
import anacrowd.stackdb.DBInfo.PostInfo;

public class CalculateBountiesForThreads extends BaseAnalysis
{
	@Override
	public void Run()
	{
		OutputThreadBounties("<gwt>");
		OutputThreadBounties("<android>");
		OutputThreadBounties("<java>");
	}
	
	public void OutputThreadBounties(String tag)
	{
		DBInfo info = new DBInfo();
		info.Init();
		
		int bounty = 0;
		int numWithBounty  = 0;
		List<PostInfo> taggedPosts = info.Tags(tag);
		for( PostInfo post : taggedPosts )
		{
			int threadBounty = 0;
			// Need to get answers....because they have the bounties...
			for( PostInfo ans : info.Answers(post.Id) )
			{
				Integer amount = info.GetBountyAmount(ans.Id);
				if( amount != null )
				{
					threadBounty+= amount;
					bounty += amount;
				}
			}
			if( threadBounty > 0 ) 					
				numWithBounty++;
		}
		System.out.println(numWithBounty+ "," + bounty);
		info.Close();
	}
	
	public static void main(String[] args)
	{
		new CalculateBountiesForThreads().Run();
	}
}
