package anacrowd.paperanalysis.coveragefilters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import anacrowd.api.AbstractApi;
import anacrowd.documentation.ClassDocumentation;
import anacrowd.documentation.ClassIndex;
import anacrowd.documentation.elements.ThreadElement;
import anacrowd.paperanalysis.BaseAnalysis;
import anacrowd.paperanalysis.coveragefilters.filters.AbstractThreadFilter;
import anacrowd.paperanalysis.coveragefilters.filters.AcceptedFilter;
import anacrowd.paperanalysis.coveragefilters.filters.AnsweredFilter;
import anacrowd.paperanalysis.coveragefilters.filters.AtLeastNFilter;
import anacrowd.paperanalysis.coveragefilters.filters.BountiedFilter;
import anacrowd.paperanalysis.coveragefilters.filters.FavoritedFilter;
import anacrowd.paperanalysis.coveragefilters.filters.ViewedFilter;
import anacrowd.paperanalysis.coveragefilters.filters.VotedFilter;

public class FilterAnalysis extends BaseAnalysis 
{
	@Override
	public void Run()
	{
		//System.out.println("GWT");
		//RunStats(GWT);
		System.out.println("Android");
		RunStats(Android);
		//System.out.println("Java");
		//RunStats(Java);
	}
	
	public void RunStats(AbstractApi api)
	{
		List<ClassDocumentation> docs = DeserializeModel(api);
		int[] levels = new int[]{1,5,20};

		List<AbstractThreadFilter> filters = new ArrayList<AbstractThreadFilter>();
		filters.add(new AcceptedFilter());
		filters.add(new AnsweredFilter());
		filters.add(new FavoritedFilter());
		filters.add(new VotedFilter(3));
		filters.add(new ViewedFilter(500));
		filters.add(new BountiedFilter());

		AtLeastNFilter atLeast2Filter = new AtLeastNFilter(new ArrayList(filters), 2);
		AtLeastNFilter atLeast3Filter = new AtLeastNFilter(new ArrayList(filters), 3);
		AtLeastNFilter atLeast5Filter = new AtLeastNFilter(new ArrayList(filters), 5);

		//filters.clear();
		
		filters.add(atLeast2Filter);
		filters.add(atLeast3Filter);
		filters.add(atLeast5Filter);

		Hashtable<Integer,ThreadElement> threadMap = ClassIndex.getInstance().ThreadMap;
		
		System.out.println("Original:");
		System.out.println(join(GetCoverage(docs,new HashSet(threadMap.keySet()),levels).values(),","));

		for( AbstractThreadFilter filter : filters )
		{
			HashSet<Integer> threads = GetIncludedThreads(threadMap.values(), filter);
			System.out.print(filter.toString() + ":");
			System.out.println(join(GetCoverage(docs,threads,levels).values(),","));
		}
	}
	
	public HashSet<Integer> GetIncludedThreads(Collection<ThreadElement> threads, AbstractThreadFilter filter)
	{
		HashSet<Integer> included = new HashSet<Integer>();
		for( ThreadElement t : threads )
		{
			if (filter.Include(t) )
			{
				included.add(t.Question.Id);
			}
		}
		return included;
	}
	
	public Hashtable<Integer,Integer> GetCoverage(List<ClassDocumentation> docs, HashSet<Integer> includedSet, int[] levels)
	{
		// pool of threads that we check if in filtered...
		Hashtable<Integer,Integer> coverageScore = new Hashtable<Integer,Integer>();
		for( int level: levels )
		{
			int coveredClasses = 0;
			for( ClassDocumentation doc : docs )
			{
				int includedThreads = 0;
				for( ThreadElement t : doc.getValidatedThreads() )
				{
					if( includedSet.contains(t.Question.Id) )
					{
						includedThreads++;
					}
				}
				if( includedThreads >= level )
				{
					coveredClasses++;
				}
			}
			coverageScore.put(level, coveredClasses);
		}
		return coverageScore;
	}
		
	public static void main(String[] args) 
	{
		new FilterAnalysis().Run();
	}
}
