package anacrowd.paperanalysis.datacharacterization;

import java.util.List;

import anacrowd.api.AbstractApi;
import anacrowd.documentation.ClassDocumentation;
import anacrowd.documentation.elements.AnswerElement;
import anacrowd.documentation.elements.ThreadElement;
import anacrowd.documentation.links.LinkSpan;
import anacrowd.documentation.links.LinkSpan.LinkType;
import anacrowd.paperanalysis.BaseAnalysis;

public class LinkAnalysis extends BaseAnalysis
{
	@Override
	public void Run()
	{
		Stats(DeserializeModel(GWT));
		//Stats(DeserializeModel("classdoc/android", Android));
		//Stats(DeserializeModel("classdoc/java", Java));
	}
	
	public void Stats(List<ClassDocumentation> docs)
	{
		// Common Sites
		// Covered why?  Answer, Question, Both
		// Accepted Answer.
		// Link type
		for( ClassDocumentation doc : docs )
		{
			int linksInAnswers=0;
			int linksInQuestions=0;
			int linksInAcceptedAnswers=0;
			
			// why linkage occurred (site). 
			int answerLinkOutcome=0;
			int questionLinkOutcome=0;
			int bothLinkOutcome=0;
			int answerAcceptedLinkOutcome=0; // threads with links to accepted answer
						
			// types of links
			int codeMarks=0;
			int codeSamples=0;
			int wordMatches=0;
			int hrefs=0;
			
			// types of links (normalized by thread)
			int codeMarksThread=0;
			int codeSamplesThread=0;
			int wordMatchesThread=0;
			int hrefsThread=0;

			
			for( ThreadElement thread : doc.getValidatedThreads() )
			{
				int threadAnswerLinks=0;
				int threadQuestionLinks=0;
				int threadAcceptedAnswerLinks=0;
				
				int t_codeMarks=0;
				int t_codeSamples=0;
				int t_wordMatches=0;
				int t_hrefs=0;
				
				for( AnswerElement ans : thread.Answers)
				{
					for( LinkSpan span: ans.getLinks(doc.Klass) )
					{
						if( span.Validated )
						{
							threadAnswerLinks++;
							
							if( span.LinkType == LinkType.CodeMarkupLink )
							{
								codeMarks++;
								t_codeMarks++;
							}
							if( span.LinkType == LinkType.CodeSampleLink )
							{
								codeSamples++;
								t_codeSamples++;
							}
							if( span.LinkType == LinkType.WordLink )
							{
								wordMatches++;
								t_wordMatches++;
							}
							if( span.LinkType == LinkType.HrefLink )
							{
								hrefs++;
								t_hrefs++;
							}
						}
					}
					if( thread.Question.HasAcceptedAnswer && 
						thread.Question.AcceptedAnswerId == ans.Id )
					{
						// Should only be one per thread!
						threadAcceptedAnswerLinks = threadAnswerLinks;
					}
				}
				if( thread.Question.IsLinked && thread.Question.IsValidated(doc.Klass) )
				{
					for( LinkSpan span: thread.Question.getLinks(doc.Klass) )
					{
						if( span.Validated )
						{
							threadQuestionLinks++;
							
							if( span.LinkType == LinkType.CodeMarkupLink )
							{
								codeMarks++;
								t_codeMarks++;
							}
							if( span.LinkType == LinkType.CodeSampleLink )
							{
								codeSamples++;
								t_codeSamples++;
							}
							if( span.LinkType == LinkType.WordLink )
							{
								wordMatches++;
								t_wordMatches++;
							}
							if( span.LinkType == LinkType.HrefLink )
							{
								hrefs++;
								t_hrefs++;
							}
						}
					}
				}
				
				if( t_codeMarks > 0)
					codeMarksThread++;
				if( t_codeSamples > 0)
					codeSamplesThread++;
				if( t_wordMatches > 0)
					wordMatchesThread++;
				if( t_hrefs > 0)
					hrefsThread++;
				
				// Link tallys
				linksInAnswers += threadAnswerLinks;
				linksInAcceptedAnswers += threadAcceptedAnswerLinks;
				linksInQuestions+= threadQuestionLinks;
				
				// Why linked
				if( threadAnswerLinks > 0 && threadQuestionLinks > 0)
				{
					bothLinkOutcome++;
				}
				else if( threadAnswerLinks > 0 )
				{
					answerLinkOutcome++;
				}
				else if( threadQuestionLinks > 0)
				{
					questionLinkOutcome++;
				}
				else
				{
					throw new Error("Invalidate state");
				}
				if( threadAcceptedAnswerLinks > 0)
				{
					answerAcceptedLinkOutcome++;
				}
			}
			
			// Dump it all
			String output = join(new String[]
				{
					doc.getValidatedThreads().size()+"",
					linksInAnswers +"",
					linksInQuestions+"",
					linksInAcceptedAnswers+"",
					// why linkage occurred (site). 
					answerLinkOutcome+"",
					questionLinkOutcome+"",
					bothLinkOutcome+"",
					answerAcceptedLinkOutcome+"", // threads with links to accepted answer
					// types of links
					codeMarks+"",
					codeSamples+"",
					wordMatches+"",
					hrefs+"",
					// types of threads (flat)
					codeMarksThread+"",
					codeSamplesThread+"",
					wordMatchesThread+"",
					hrefsThread+""
				},",");
			System.out.println(output);
		}
	}
	
	public static void main(String[] args)
	{
		BaseAnalysis a = new LinkAnalysis();
		a.Run();
	}
}
