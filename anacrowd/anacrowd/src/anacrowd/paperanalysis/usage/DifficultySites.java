package anacrowd.paperanalysis.usage;

import java.util.List;

import anacrowd.documentation.ClassDocumentation;
import anacrowd.documentation.elements.AnswerElement;
import anacrowd.documentation.elements.ThreadElement;
import anacrowd.documentation.links.LinkSpan;
import anacrowd.paperanalysis.BaseAnalysis;

public class DifficultySites extends BaseAnalysis
{
	@Override
	public void Run()
	{
		List<ClassDocumentation> docs = DeserializeModel(Android);
		for( ClassDocumentation doc : docs)
		{
			int numberThreadQs = 0; // How many threads that are linked by Q
			int numberThreadAs = 0; // How many threads that are linked by at least one A.
			int numberQs = 0; // Raw number of Q
			int numberAs = 0; // Raw number of A
			
			for( ThreadElement t : doc.getValidatedThreads() )
			{
				int threadQs = 0;
				int threadAs = 0;
				if( t.Question.IsLinked && t.Question.IsValidated(doc.Klass) )
				{
					for( LinkSpan link : t.Question.getLinks(doc.Klass) )
					{
						if( link.Validated )
						{
							threadQs++;
						}
					}
				}
				
				for(AnswerElement ans : t.Answers )
				{
					for( LinkSpan link : ans.getLinks(doc.Klass) )
					{
						if( link.Validated )
						{
							threadAs++;
						}
					}
				}
				
				if( threadQs > 0 )
				{
					numberThreadQs++;
				}
				if( threadAs > 0 )
				{
					numberThreadAs++;
				}
				numberQs += threadQs;
				numberAs += threadAs;
			}
			
			System.out.println(doc.Klass.getFQN() + "," +
					numberThreadQs + "," +
					numberThreadAs + "," +
					numberQs + "," +
					numberAs);
		}
	}
	// Do some classes appear more in questions or answers.
	public static void main(String[] args)
	{
		new DifficultySites().Run();
	}
}