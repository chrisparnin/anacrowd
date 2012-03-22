package anacrowd.paperanalysis.examples;

import java.util.HashSet;
import java.util.List;

import anacrowd.documentation.ClassDocumentation;
import anacrowd.documentation.elements.AnswerElement;
import anacrowd.documentation.elements.ThreadElement;
import anacrowd.documentation.links.LinkSpan;
import anacrowd.documentation.links.LinkSpan.LinkType;
import anacrowd.paperanalysis.BaseAnalysis;

public class FindLinkExamples extends BaseAnalysis 
{
	@Override
	public void Run()
	{
		List<ClassDocumentation> docs = DeserializeModel(Android);

		for( ClassDocumentation doc : docs )
		{
			for( ThreadElement thread : doc.getValidatedThreads() )
			{
				HashSet<LinkType> types = new HashSet<LinkType>();
				for( LinkSpan link : thread.Question.getLinks(doc.Klass) )
				{
					if( link.Validated )
					{
						types.add(link.LinkType);
					}
				}
				for( AnswerElement ans : thread.Answers )
				{
					for( LinkSpan link : ans.getLinks(doc.Klass) )
					{
						if( link.Validated )
						{
							types.add(link.LinkType);
						}
					}
				}
				
				if( types.size() >= 3)
				{
					System.out.println("http://stackoverflow.com/questions/" + thread.Question.Id);
				}
			}
		}
	}

	public static void main(String[] args)
	{
		new FindLinkExamples().Run();
	}
}
