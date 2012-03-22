package anacrowd.paperanalysis;

import java.util.Hashtable;
import java.util.List;

import anacrowd.api.AbstractApi;
import anacrowd.api.elements.ClassElem;
import anacrowd.api.elements.PackageElem;
import anacrowd.metrics.coverage.WordDistributions;
import anacrowd.stackdb.Search;

public class ClassTagReliability extends BaseAnalysis
{
	@Override
	public void Run()
	{
		super.Run();
		
		Hashtable<Integer,QATuple> androidValues = TagReliability( Android );
		Hashtable<Integer,QATuple> javaValues = TagReliability( Java );

		PrintTable(androidValues, Android);
		PrintTable(javaValues, Java);
	}

	private void PrintTable(Hashtable<Integer, QATuple> values, AbstractApi api) 
	{
		System.out.println(api.MainTag);
		System.out.println("n-word\t\t\tTags Matches (Q/A) \t\t\tWord Matches (Q/A)");

		for( int length : values.keySet() )
		{
			QATuple tuple = values.get(length);
			System.out.println(
					length+"/"+tuple.NumClasses + "\t\t\t" +
					tuple.QuestionsTags + "/" + tuple.AnswersTags + "\t\t\t" +
					tuple.QuestionsWords +"/" + tuple.AnswersWords);
		}
	}
	
	public class QATuple
	{
		int AnswersTags;
		int QuestionsTags;
		int AnswersWords;
		int QuestionsWords;
		int NumClasses;
	}
	
	public Hashtable<Integer,QATuple> TagReliability( AbstractApi api )
	{
		WordDistributions d = new WordDistributions();
		Search search = new Search();
		search.Init();

		PrepareApiForSearch(search, api);
		
		Hashtable<Integer,QATuple> hash = new Hashtable<Integer,QATuple>();
		
		for( PackageElem pack : api.Packages )
		{
			for( ClassElem klass : pack.Classes )
			{
				int length = d.WordLength(klass.Name);
				List<Integer> klassQuestionsTags = search.GetIdsViaTags(this.QuestionsTempTableName, klass.Name);
				List<Integer> klassAnswersTags = search.GetIdsViaTags(this.AnswersTempTableName, klass.Name);

				List<Integer> klassQuestionsWords = search.GetIdsViaWordMatches(this.QuestionsTempTableName, klass.Name);
				List<Integer> klassAnswersWords = search.GetIdsViaWordMatches(this.AnswersTempTableName, klass.Name);
				
				if( !hash.containsKey(length) )
				{
					hash.put(length, new QATuple());
				}
				
				hash.get(length).QuestionsTags += klassQuestionsTags.size();
				hash.get(length).AnswersTags += klassAnswersTags.size();
				hash.get(length).QuestionsWords += klassQuestionsWords.size();
				hash.get(length).AnswersWords += klassAnswersWords.size();
				hash.get(length).NumClasses++;
			}
		}
		search.Close();
		return hash;
	}
	
	public static void main (String [] args) 
	{
		BaseAnalysis run = new ClassTagReliability();
		run.Run();
	}	
}
