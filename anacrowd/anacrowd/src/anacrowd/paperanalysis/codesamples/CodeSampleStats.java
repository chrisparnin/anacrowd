package anacrowd.paperanalysis.codesamples;

import java.io.FileWriter;
import java.util.List;

import anacrowd.api.AbstractApi;
import anacrowd.documentation.ClassDocumentation;
import anacrowd.documentation.elements.AnswerElement;
import anacrowd.documentation.elements.CodeSampleElement;
import anacrowd.documentation.elements.QuestionElement;
import anacrowd.documentation.elements.CodeSampleElement.CodeBlock;
import anacrowd.paperanalysis.BaseAnalysis;

public class CodeSampleStats extends BaseAnalysis
{
	@Override
	public void Run()
	{
		CodeSampleAnalysis(Android, "android.codesamples.txt");
	}

	private void CodeSampleAnalysis(AbstractApi api, String outputPath) 
	{
		try
		{
			FileWriter fw = new FileWriter(outputPath);
			List<ClassDocumentation> docs = DeserializeModel(api);
			for( ClassDocumentation doc: docs )
			{
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
					
					if( numBlocks > 0 )
					{
						fw.write(
							doc.Klass.getFQN() + "," + 
							numBlocks + "," +
							numLines + "," +
							IsCodeSampleInQuestion(sample) + "," +
							IsCodeSampleInAnswer(sample) + "," +
							IsCodeSampleInAcceptedAnswer(sample) + "," +
							GetCodeSampleVotes(sample)
						);
						fw.write("\n");
					}
				}
			}
			fw.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public int GetCodeSampleVotes(CodeSampleElement element)
	{
		if( element.Parent instanceof QuestionElement )
			return element.Parent.Parent.Votes;
		else
		{
			AnswerElement ans = (AnswerElement)element.Parent;
			return ans.Votes;
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
		new CodeSampleStats().Run();
	}
}
