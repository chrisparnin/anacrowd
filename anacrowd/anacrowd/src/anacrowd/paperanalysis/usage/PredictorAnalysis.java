package anacrowd.paperanalysis.usage;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Hashtable;
import java.util.List;

import org.json.JSONException;

import anacrowd.api.AbstractApi;
import anacrowd.documentation.ClassDocumentation;
import anacrowd.documentation.elements.AnswerElement;
import anacrowd.documentation.elements.ThreadElement;
import anacrowd.documentation.generator.SerializeDocumentation;
import anacrowd.documentation.links.LinkSpan;
import anacrowd.paperanalysis.BaseAnalysis;

public class PredictorAnalysis extends BaseAnalysis 
{
	@Override
	public void Run()
	{
		//OutputApi(Android,"classdoc/android","../data/android.usage.csv");
		OutputApi(Java,"classdoc/java","../data/java.usage.csv");
	}

	public void OutputApi(AbstractApi api, String classdocpath, String usageCSV)
	{
		List<ClassDocumentation> docs = DeserializeModel(api);
		Hashtable<String,Integer> usageMap = ReadUsage(usageCSV);
		
		System.out.println("FQN,usage,threads,answerFrequency,questionFrequency");
		for( ClassDocumentation doc : docs)
		{
			String classFQN = doc.Klass.getFQN();
			int usage = usageMap.get(classFQN);
			int threads = doc.getValidatedThreads().size();
			int[] aQThreads = AnswerQuestionRatio(doc);

			System.out.println(classFQN + "," + usage + "," + threads + "," + aQThreads[0] + "," + aQThreads[1] );
		}
	}
	
	public int[] AnswerQuestionRatio(ClassDocumentation doc)
	{
		int numberThreadQs = 0; // How many threads that are linked by Q
		int numberThreadAs = 0; // How many threads that are linked by at least one A.
		int numberQs = 0; // Raw number of Q
		int numberAs = 0; // Raw number of A
		
		int[] answerQuestions = new int[2];
		
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
		answerQuestions[0] = numberThreadAs;
		answerQuestions[1] = numberThreadQs;
		return answerQuestions;
	}

	
	private Hashtable<String,Integer> ReadUsage(String path)
	{
		try 
		{
			String contents = readStream(new FileInputStream(path));
			String[] lines = contents.split(System.getProperty("line.separator"));
			Hashtable<String,Integer> usageMap = new Hashtable<String,Integer>();
			for( String line : lines)
			{	
				usageMap.put( line.split(",")[0], Integer.parseInt(line.split(",")[1]) );
			}
			return usageMap;
		}
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		}
		return null;
	}

	
	public static void main(String[] args) 
	{
		new PredictorAnalysis().Run();
	}
}
