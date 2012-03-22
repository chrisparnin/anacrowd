package anacrowd.paperanalysis.crowd;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import anacrowd.api.AbstractApi;
import anacrowd.documentation.ClassDocumentation;
import anacrowd.documentation.ClassIndex;
import anacrowd.documentation.elements.AnswerElement;
import anacrowd.documentation.elements.ThreadElement;
import anacrowd.paperanalysis.BaseAnalysis;
import anacrowd.stackdb.DBInfo;

public class WhoContributes extends BaseAnalysis 
{
	DBInfo info = new DBInfo();
	@Override
	public void Run()
	{
		info.Init();
		//SizeOfCrowd(GWT);
		//SizeOfCrowd(Android);
		//SizeOfCrowd(Java);
		
		//SizeOfContribution(GWT, "gwt_userContributions.txt");
		//SizeOfContribution(Android, "android_userContributions.txt");
		//SizeOfContribution(Java, "java_userContributions.txt");

		PercentileBuckets(GWT);
		//PercentileBuckets(Android);
		//PercentileBuckets(Java);
		
		info.Close();
	}

	public static class PercentileBucket
	{
		public PercentileBucket(int start, int end, Collection<Integer> source)
		{
			sourceSorted = new ArrayList<Integer>(source);
			Collections.sort(sourceSorted);
			this.StartPercentile = start;
			this.EndPercentile = end;
			
			this.Population = sourceSorted.subList(CalcIndex(StartPercentile,source.size()), CalcIndex(EndPercentile,source.size()));
		}
		public int StartPercentile;
		public int EndPercentile;
		List<Integer> Population;
		ArrayList<Integer> sourceSorted;
		public int CalcIndex(int percentile, int N)
		{
			return (int)Math.min(Math.floor((percentile/100.0) * N + 0.5), N);
		}
		
		public int Sum(Collection<Integer> col)
		{
			int sum = 0; 
			for( int item : col)
			{
				sum+=item;
			}
			return sum;
		}
		
		public int Min()
		{
			return Population.get(0);
		}
		
		public int Max()
		{
			return Population.get(Population.size()-1);
		}
		
		public int Med()
		{
			return Population.get(CalcIndex(50, Population.size()));
		}
		
		@Override
		public String toString()
		{
			String self = StartPercentile + ":" + EndPercentile + "";
			self += "[" + Min() + "-" + Max() + "("+ Med()+")];";
			self += Sum(Population) + "("+ (double)Sum(Population) / Sum(sourceSorted) +")";
			return self;
		}
	}
	
	public void PercentileBuckets(AbstractApi api)
	{
		List<ClassDocumentation> docs = DeserializeModel(api);

		Hashtable<Integer,Integer> Askers = new Hashtable<Integer,Integer>();
		Hashtable<Integer,Integer> Advisors = new Hashtable<Integer,Integer>();
		Hashtable<Integer,Integer> SelfAdvisors = new Hashtable<Integer,Integer>();
		
		for(ThreadElement thread : ClassIndex.getInstance().ThreadMap.values() )
		{
			IncByOne(Askers,thread.Question.UserId);
			
			for( AnswerElement ans : thread.Answers )
			{
				IncByOne(Advisors,ans.UserId);
			}
			
			if( thread.Answers.size() == 1 && thread.Question.UserId == thread.Answers.get(0).UserId )
			{
				// One is the loneliest number
				IncByOne(SelfAdvisors, thread.Question.UserId);
			}
		}
				
		//System.out.println(Askers.size() + " " + Advisors.size() + " " + All.size());
		HashSet<Integer> All = new HashSet<Integer>();
		All.addAll(Askers.keySet());
		All.addAll(Advisors.keySet());

		System.out.println("Askers:");
		PrintPercentileStats(Askers.values());
		System.out.println("Advisors:");
		PrintPercentileStats(Advisors.values());
	}
	
	public void PrintPercentileStats(Collection<Integer> pool)
	{
		PercentileBucket bucket0_25  = new PercentileBucket(0,25,pool); 
		PercentileBucket bucket25_50 = new PercentileBucket(25,50,pool); 
		PercentileBucket bucket50_75 = new PercentileBucket(50,75,pool); 
		PercentileBucket bucket75_95 = new PercentileBucket(75,95,pool); 
		PercentileBucket bucket95_00 = new PercentileBucket(95,100,pool); 

		System.out.println(bucket0_25);
		System.out.println(bucket25_50);
		System.out.println(bucket50_75);
		System.out.println(bucket75_95);
		System.out.println(bucket95_00);
		
		PercentileBucket bucket90_91 = new PercentileBucket(90,91,pool); 
		PercentileBucket bucket91_92 = new PercentileBucket(91,92,pool); 
		PercentileBucket bucket92_93 = new PercentileBucket(92,93,pool); 
		PercentileBucket bucket93_94 = new PercentileBucket(93,94,pool); 
		PercentileBucket bucket94_95 = new PercentileBucket(94,95,pool); 
		PercentileBucket bucket95_96 = new PercentileBucket(95,96,pool); 
		PercentileBucket bucket96_97 = new PercentileBucket(96,97,pool); 
		PercentileBucket bucket97_98 = new PercentileBucket(97,98,pool); 
		PercentileBucket bucket98_99 = new PercentileBucket(98,99,pool); 
		PercentileBucket bucket99_00 = new PercentileBucket(99,100,pool); 		
		

		System.out.println("%%%%%%%%%%%%");
		System.out.println(bucket90_91);
		System.out.println(bucket91_92);
		System.out.println(bucket92_93);
		System.out.println(bucket93_94);
		System.out.println(bucket94_95);
		System.out.println(bucket95_96);
		System.out.println(bucket96_97);
		System.out.println(bucket97_98);
		System.out.println(bucket98_99);
		System.out.println(bucket99_00);
	}
	
	public HashSet<Integer> GetCuratorIds(Integer PostId)
	{
		HashSet<Integer> results = new HashSet<Integer>(info.GetCurators(PostId));
		results.remove(0);
		return results;
	}
	
	public void SizeOfContribution(AbstractApi api, String outputPath)
	{
		List<ClassDocumentation> docs = DeserializeModel(api);

		Hashtable<Integer,Integer> Askers = new Hashtable<Integer,Integer>();
		Hashtable<Integer,Integer> Advisors = new Hashtable<Integer,Integer>();
		Hashtable<Integer,Integer> SelfAdvisors = new Hashtable<Integer,Integer>();
		
		for(ThreadElement thread : ClassIndex.getInstance().ThreadMap.values() )
		{
			IncByOne(Askers,thread.Question.UserId);
			
			for( AnswerElement ans : thread.Answers )
			{
				IncByOne(Advisors,ans.UserId);
			}
			
			if( thread.Answers.size() == 1 && thread.Question.UserId == thread.Answers.get(0).UserId )
			{
				// One is the loneliest number
				IncByOne(SelfAdvisors, thread.Question.UserId);
			}
		}
				
		//System.out.println(Askers.size() + " " + Advisors.size() + " " + All.size());
		HashSet<Integer> All = new HashSet<Integer>();
		All.addAll(Askers.keySet());
		All.addAll(Advisors.keySet());

		try
		{
			FileWriter fw = new FileWriter(outputPath);
			for( Integer userId : All)
			{
				int qContributions = Askers.containsKey(userId) ? Askers.get(userId) : 0;
				int aContributions = Advisors.containsKey(userId) ? Advisors.get(userId) : 0;
				int selfContributions = SelfAdvisors.containsKey(userId) ? SelfAdvisors.get(userId) : 0;

				fw.write(userId + "," + qContributions + "," + aContributions + "," + selfContributions + "\n");
			}
			fw.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
	}

	private void IncByOne(Hashtable<Integer,Integer> map, Integer key)
	{
		if( !map.containsKey(key))
			map.put(key, 0);
		map.put(key, map.get(key)+1);
	}
	
	public void SizeOfCrowd(AbstractApi api)
	{
		List<ClassDocumentation> docs = DeserializeModel(api);

		HashSet<Integer> Askers = new HashSet<Integer>();
		HashSet<Integer> Advisors = new HashSet<Integer>();
		HashSet<Integer> Curators = new HashSet<Integer>();

		for(ThreadElement thread : ClassIndex.getInstance().ThreadMap.values() )
		{
			Askers.add(thread.Question.UserId);
			
			for( AnswerElement ans : thread.Answers )
			{
				Advisors.add(ans.UserId);
			}
		}
		
		HashSet<Integer> All = new HashSet<Integer>();
		All.addAll(Askers);
		All.addAll(Advisors);
		
		System.out.println(Askers.size() + " " + Advisors.size() + " " + All.size());
	}
		
	public static void main(String[] args) 
	{
		new WhoContributes().Run();
	}
}
