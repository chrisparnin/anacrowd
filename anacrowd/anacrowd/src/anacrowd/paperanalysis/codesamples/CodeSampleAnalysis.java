package anacrowd.paperanalysis.codesamples;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import anacrowd.api.AbstractApi;
import anacrowd.documentation.ClassDocumentation;
import anacrowd.documentation.elements.AnswerElement;
import anacrowd.documentation.elements.CodeSampleElement;
import anacrowd.documentation.elements.QuestionElement;
import anacrowd.documentation.elements.ThreadElement;
import anacrowd.documentation.elements.CodeSampleElement.CodeBlock;
import anacrowd.paperanalysis.BaseAnalysis;

public class CodeSampleAnalysis extends BaseAnalysis
{
	@Override
	public void Run()
	{
		//OutputApi(GWT);
		OutputApi(Android);
		//OutputApi(Java);
	}
	
	public void OutputApi(AbstractApi api)
	{
		List<ClassDocumentation> docs = DeserializeModel(api);

		Hashtable<Integer,ThreadElement> threadMap = new Hashtable<Integer,ThreadElement>();
		for( ClassDocumentation doc : docs )
		{
			for( ThreadElement t : doc.getValidatedThreads() )
			{
				threadMap.put(t.Question.Id, t);
			}
		}
		
		for( ClassDocumentation doc : docs )
		{
			int codeSamplesInAnswers = 0;
			int codeSamplesInAccepted = 0;
			int codeSamplesInQuestions = 0;
			int total = 0;
			for( CodeSampleElement sample : doc.getCodeSampleElements() )
			{
				int numLines = 0;
				int numBlocks = 0;
				for( CodeBlock block : sample.Blocks )
				{
					String body = block.getBody();
					String[] lines = body.split(System.getProperty("line.separator"));
					if( lines.length > 1 )
					{
						numLines += lines.length;
						numBlocks++;
					}
				}
				
				if( numLines > 1)
				{
					if( IsCodeSampleInQuestion(sample))
						codeSamplesInQuestions++;
					if( IsCodeSampleInAnswer(sample))
						codeSamplesInAnswers++;
					if( IsCodeSampleInAcceptedAnswer(sample) )
						codeSamplesInAccepted++;
					total++;
				}
			}
			
			System.out.println( 
					doc.Klass.getFQN() + "," +
					codeSamplesInQuestions + "," +
					codeSamplesInAnswers + "," +
					codeSamplesInAccepted + "," +
					total );
		}
	}
	
	public boolean IsCodeSampleInQuestion(CodeSampleElement element)
	{
		if( element.Parent instanceof QuestionElement)
		{
			return true;
		}
		return false;
	}

	public boolean IsCodeSampleInAnswer(CodeSampleElement element)
	{
		if(element.Parent instanceof AnswerElement )
			return true;
		return false;
	}
	
	public boolean IsCodeSampleInAcceptedAnswer(CodeSampleElement element)
	{
		if(element.Parent instanceof AnswerElement )
		{
			AnswerElement ans = (AnswerElement)element.Parent;
			return ans.isAcceptedAnswer();
		}
		return false;
	}

	
	public static void main(String[] args)
	{
		new CodeSampleAnalysis().Run();
	}
}
