package anacrowd.sampling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

import com.mysql.jdbc.UpdatableResultSet;

import anacrowd.api.AbstractApi;
import anacrowd.api.elements.ClassElem;
import anacrowd.api.elements.PackageElem;
import anacrowd.metrics.coverage.WordDistributions;

public class SampleByWordNumber 
{
	public void SampleMatches( AbstractApi api, double percentage, int numQuestionsAnswers  )
	{
		WordDistributions dist = new WordDistributions();
		Hashtable<Integer,ArrayList<ClassElem>> bins = new Hashtable<Integer,ArrayList<ClassElem>>();
		
		for( PackageElem pack : api.Packages )
		{
			for( ClassElem klass : pack.Classes )
			{
				int length = dist.WordLength(klass.Name);
				if( !bins.containsKey(length) )
				{
					bins.put(length, new ArrayList<ClassElem>());
				}
				bins.get(length).add(klass);
			}
		}

		for( int b : bins.keySet() )
		{
			// Output histogram of bins
			int poolSize = NonZeroQuestionsOrAnswersClasses(bins.get(b)).size();
			System.out.println(b+" "+bins.get(b).size() + " " + poolSize);
		}
		
		for( int b : bins.keySet() )
		{
			List<ClassElem> classPool = NonZeroQuestionsOrAnswersClasses(bins.get(b));
			int numClassesToInspect = Math.min((int)( percentage * classPool.size()),numQuestionsAnswers);
			for( ClassElem klass : RandomItems(classPool, numClassesToInspect ))
			{
				List<String> inspectionPool = new ArrayList<String>();
				for( int answerId : RandomItems(klass.AnswerIds, numQuestionsAnswers))
				{
					int questionId = klass.AnswerIdToParentId.get(answerId);
					inspectionPool.add(b + " " + klass.Name + " " + AnswerUrl(questionId, answerId));
				}
				for( int questionId : RandomItems(klass.QuestionIds, numQuestionsAnswers ))
				{
					inspectionPool.add(b + " " + klass.Name + " " + QuestionUrl(questionId));
				}
				
				for( String inspection : RandomItems(inspectionPool, numQuestionsAnswers ))
				{
					System.out.println(inspection);
				}
			}
		}
	}
	
	// ಠ_ಠ LINQ.Where
	private List<ClassElem> NonZeroQuestionsOrAnswersClasses(List<ClassElem> classes)
	{
		List<ClassElem> nonZero = new ArrayList<ClassElem>();
		for( ClassElem klass : classes )
		{
			if( klass.QuestionIds.size() > 0 || klass.AnswerIds.size() > 0)
			{
				nonZero.add(klass);	
			}
		}
		return nonZero;
	}

	
	// http://www.javamex.com/tutorials/random_numbers/random_sample.shtml
	// http://stackoverflow.com/questions/48087/select-a-random-n-elements-from-listt-in-c
	public static <T> List<T> RandomItems(List<T> items, int sampleSize)
	{
		List<T> clone = new ArrayList<T>(items);
		Collections.shuffle(clone);
		return clone.subList(0, Math.min(clone.size(),sampleSize));
	}
	
	public String QuestionUrl( int id )
	{
		return "http://www.stackoverflow.com/questions/"+id;
	}

	public String AnswerUrl( int id, int answerId)
	{
		return "http://www.stackoverflow.com/questions/"+id+"/#"+answerId;
	}
}
