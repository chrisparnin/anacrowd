package anacrowd.paperanalysis.usage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.JSONException;

import anacrowd.api.AbstractApi;
import anacrowd.documentation.ClassDocumentation;
import anacrowd.documentation.elements.ThreadElement;
import anacrowd.documentation.generator.SerializeDocumentation;
import anacrowd.paperanalysis.BaseAnalysis;

public class SamplingForDifficultyAnalysis extends BaseAnalysis 
{
	@Override
	public void Run()
	{
		List<ClassDocumentation> docs = DeserializeModel(Android);
		SampleHighAndLow(docs, 500);
	}
	
	public void SampleHighAndLow(List<ClassDocumentation> docs, int threads )
	{
		Collections.sort(docs, new Comparator<ClassDocumentation>(){
			@Override
			public int compare(ClassDocumentation a, ClassDocumentation b) 
			{
				return a.getValidatedThreads().size() - b.getValidatedThreads().size();
			}
		});
		
		int firstNonZeroIndex = 0;
		for( int i = 0; i < docs.size(); i++ )
		{
			ClassDocumentation doc = docs.get(i);
			int t = doc.getValidatedThreads().size();
			if( t > 0)
				break;
		}
		int workingRange = docs.size() - firstNonZeroIndex;
		int lowStart = firstNonZeroIndex; // not inclusive
		int lowEnd = (int)(workingRange *.40) + firstNonZeroIndex;
		int highStart = docs.size() - (int)(workingRange *.15);
		int highEnd = docs.size(); // not inclusive
		
		List<ClassDocumentation> lowSet = new ArrayList<ClassDocumentation>(docs.subList(lowStart, lowEnd));
		List<ClassDocumentation> highSet = new ArrayList<ClassDocumentation>(docs.subList(highStart, highEnd));
		
		List<ThreadElement> lowThreads = new ArrayList<ThreadElement>();
		List<ThreadElement> highThreads = new ArrayList<ThreadElement>();
		
		Collections.shuffle(lowSet);
		for( ClassDocumentation doc : lowSet )
		{
			List<ThreadElement> docthreads = doc.getValidatedThreads();
			for( ThreadElement t : docthreads)
			{
//				t.Parent = doc;
			}
			Collections.shuffle(docthreads);
			lowThreads.addAll(docthreads.subList(0,Math.min(10, docthreads.size())));
		}
		Collections.shuffle(highSet);
		for( ClassDocumentation doc : highSet )
		{
			List<ThreadElement> docthreads = doc.getValidatedThreads();
			for( ThreadElement t : docthreads)
			{
//				t.Parent = doc;
			}
			Collections.shuffle(docthreads);
			highThreads.addAll(docthreads.subList(0,Math.min(10, docthreads.size())));
		}
		
		Collections.shuffle(lowThreads);
		Collections.shuffle(highThreads);

		for(int i = 0; i < threads/2; i++)
		{
			OutputThread(lowThreads.get(i));
			OutputThread(highThreads.get(i));
		}
	}
	
	public void OutputThread(ThreadElement thread)
	{
//		System.out.print(thread.Parent.Klass.getFQN() + ",");
		System.out.println("http://stackoverflow.com/questions/" + thread.Question.Id + "/");
	}
	
	public static void main(String[] args)
	{
		BaseAnalysis a = new SamplingForDifficultyAnalysis();
		a.Run();
	}
}
