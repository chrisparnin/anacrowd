package anacrowd.metrics.coverage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import anacrowd.api.AbstractApi;
import anacrowd.stackdb.Search;
import anacrowd.api.elements.ClassElem;
import anacrowd.api.elements.MethodElem;
import anacrowd.api.elements.PackageElem;

public class Visitor 
{
	
	public void ClassQuestionsAnswers( AbstractApi api )
	{
		Search search = new Search();
		search.Init();

		search.CreateTemporaryTable(api.MainTag, api.MainTag + "questions_table");
		System.out.println("Created Questions Table");

		search.CreateTemporaryRelatedTable(api.MainTag, api.MainTag + "answers_table", api.MainTag + "questions_table");
		System.out.println("Created Answers Table");
		
		for( PackageElem pack : api.Packages )
		{
			System.out.println("Package: " + pack.Name);
			for( ClassElem klass : pack.Classes )
			{
				List<Integer> klassQuestions = search.GetIds(api.MainTag + "questions_table", klass.Name);
				klass.QuestionIds = klassQuestions;
				
				List<Integer> klassAnswers = search.GetIds(api.MainTag + "answers_table", klass.Name);
				klass.AnswerIds = klassAnswers;
				
				for( int answerId: klass.AnswerIds )
				{
					klass.AnswerIdToParentId.put(answerId, search.GetParentId(api.MainTag + "answers_table", answerId));
				}
			}
		}
		
		search.Close();
	}

	public void ClassTagCoverage( AbstractApi api ) throws SQLException
	{
		Search search = new Search();
		search.Init();
		Statement s = search.conn.createStatement();
		search.CreateTemporaryTable(api.MainTag, api.MainTag + "questions_table");
		System.out.println("Created Questions Table");

		search.CreateTemporaryRelatedTable(api.MainTag, api.MainTag + "answers_table", api.MainTag + "questions_table");
		System.out.println("Created Answers Table");

		WordDistributions wd = new WordDistributions();
		for( PackageElem pack : api.Packages )
		{
			System.out.println("Package: " + pack.Name);
			for( ClassElem klass : pack.Classes )
			{
				List<Integer> klassQuestions = search.GetIds(api.MainTag + "questions_table", klass.Name);
				klass.QuestionIds = klassQuestions;
				
				List<Integer> klassAnswers = search.GetIds(api.MainTag + "answers_table", klass.Name);
				klass.AnswerIds = klassAnswers;
				
				for( int answerId: klass.AnswerIds )
				{
					klass.AnswerIdToParentId.put(answerId, search.GetParentId(api.MainTag + "answers_table", answerId));
				}
			}
		}
		
		search.Close();
	}
	
	
	
	public void MethodQuestionsAnswers( AbstractApi api, boolean useClassName )
	{
		Search search = new Search();
		search.Init();
		search.CreateTemporaryTable(api.MainTag, api.MainTag + "questions_table");
		search.CreateTemporaryRelatedTable(api.MainTag, api.MainTag + "answers_table", api.MainTag + "questions_table");
		System.out.println("Created temp tables for " + api.MainTag);
		for( PackageElem pack : api.Packages )
		{
			System.out.println(pack.Name);
			for( ClassElem klass : pack.Classes )
			{
				for( MethodElem meth : klass.Methods )
				{
					List<Integer> methQuestions = search.GetIds(api.MainTag + "questions_table",  meth.Name);
					meth.QuestionIds = methQuestions;
					
					List<Integer> methAnswers = search.GetIds(api.MainTag + "answers_table",  meth.Name);
					meth.AnswerIds = methAnswers;
				}
				
				if( useClassName )
				{
					List<Integer> klassQuestions = search.GetIds(api.MainTag + "questions_table",  klass.Name);
					List<Integer> klassAnswers = search.GetIds(api.MainTag + "answers_table",  klass.Name);

					HashSet<Integer> klassQs = new HashSet<Integer>(klassQuestions);
					HashSet<Integer> klassAs = new HashSet<Integer>(klassAnswers);
					
					for( MethodElem meth : klass.Methods )
					{
						List<Integer> toRemoveQ = new ArrayList<Integer>();
						for( int q : meth.QuestionIds )
						{
							if( !klassQs.contains(q) )
							{
								toRemoveQ.add(q);
							}
						}
						for( Integer q : toRemoveQ)
						{
							meth.QuestionIds.remove(q);
						}
						
						List<Integer> toRemoveA = new ArrayList<Integer>();
						for( Integer a : meth.AnswerIds )
						{
							if( !klassAs.contains(a) )
							{
								toRemoveA.add(a);
							}
						}
						for( Integer a : toRemoveA)
						{
							meth.AnswerIds.remove(a);
						}
					}
				}
			}
		}
		
		search.Close();
	}

	
	public void ClassCoveragePercentage( AbstractApi api )
	{
		Search search = new Search();
		search.Init();

		search.CreateTemporaryTable(api.MainTag, api.MainTag + "_table");
		
		for( PackageElem pack : api.Packages )
		{
			System.out.println("Package: " + pack.Name);
			for( ClassElem klass : pack.Classes )
			{
				int klassQuestions = search.QuestionsInBodyTitle(api.MainTag + "_table", klass.Name);
				System.out.println(klass.Name + ":" + klassQuestions);
				klass.NumQuestions = klassQuestions;
			}
		}
		
		search.Close();
	}
	
	public void MethodCoveragePercentage( AbstractApi api )
	{
		Search search = new Search();
		search.Init();
		search.CreateTemporaryTable(api.MainTag, api.MainTag + "_table");

		for( PackageElem pack : api.Packages )
		{
			for( ClassElem klass : pack.Classes )
			{
				System.out.println(klass.Name);
				for( MethodElem meth : klass.Methods )
				{
					//int methodQuestions = search.QuestionsInBodyTitle(api.MainTag + "_table", klass.Name, meth.Name);
					int methodQuestions = search.QuestionsInBodyTitle(api.MainTag + "_table", meth.Name);
					System.out.println(meth.Name + ":" + methodQuestions);
					meth.NumQuestions = methodQuestions;
				}
			}
		}
		
		search.Close();
	}
}
