package anacrowd.paperanalysis.datacharacterization;

import java.util.Hashtable;
import java.util.List;

import com.sun.xml.internal.bind.v2.schemagen.episode.Klass;

import anacrowd.documentation.ClassDocumentation;
import anacrowd.documentation.ClassIndex;
import anacrowd.documentation.elements.AnswerElement;
import anacrowd.documentation.elements.ThreadElement;
import anacrowd.paperanalysis.BaseAnalysis;

public class OtherRatings extends BaseAnalysis 
{
	@Override
	public void Run()
	{
		List<ClassDocumentation> docs = DeserializeModel(Android);
		Hashtable<Integer,ThreadElement> threadMap = ClassIndex.getInstance().ThreadMap;		
		
		int otherMoreThanAccepted = 0;
		int votedButNotAccepted = 0;
		int threadVoted = 0;
		int numAccepted = 0;
		for( ThreadElement t : threadMap.values() )
		{
			int maxVote = 0;
			AnswerElement accepted = null;
			int votes = 0;
			for( AnswerElement ans : t.Answers )
			{
				if( maxVote < ans.Votes )
				{
					maxVote = ans.Votes;
				}
				if( ans.isAcceptedAnswer() )
				{
					accepted = ans;
				}
				votes += ans.Votes;
			}
			if( accepted != null )
			{
				if( accepted.Votes < maxVote)
				{
					otherMoreThanAccepted++;
				}
			}
			
			if( t.Question.AcceptedAnswerId != 0 )
			{
				numAccepted++;
			}

			if( accepted == null )
			{
				if( votes > 0 )
				{
					votedButNotAccepted++;
				}
			}
			
			if( votes > 0 )
			{
				threadVoted++;
			}
		}
		System.out.println(otherMoreThanAccepted + "," + votedButNotAccepted + "," + threadVoted + ","  + numAccepted + "," + threadMap.size());
	}
	
	public static void main(String[] args)
	{
		new OtherRatings().Run();
	}
}
