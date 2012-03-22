package anacrowd.paperanalysis.timeseries;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import anacrowd.api.AbstractApi;
import anacrowd.api.elements.ClassElem;
import anacrowd.api.elements.PackageElem;
import anacrowd.documentation.ClassDocumentation;
import anacrowd.documentation.elements.AnswerElement;
import anacrowd.documentation.elements.ThreadElement;
import anacrowd.documentation.generator.BuildDocumentation;
import anacrowd.documentation.generator.LinkDocumentation;
import anacrowd.documentation.generator.SerializeDocumentation;
import anacrowd.paperanalysis.BaseAnalysis;

public class SpeedOfSaturation extends BaseAnalysis
{
	private Date IncrementByDay(Date d)
	{
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.add(Calendar.DATE, 1);
		return c.getTime();
	}
	
	@Override
	public void Run()
	{
		int[] levels = new int[]{1,5,20,50,100};
		List<ClassDocumentation> classDocs = DeserializeModel(Java);

		//HashSet<Date> allDates = new HashSet<Date>();
		
		Hashtable<Integer,List<ClassSaturation>> saturationData = new Hashtable<Integer,List<ClassSaturation>>();
		for( int level : levels )
		{
			List<ClassSaturation> classSats = Saturation(level,classDocs);
			saturationData.put(level,classSats);
			
			// Track all dates
			//for( ClassSaturation classSat : classSats )
			//{
			//	for( Date satEvent : classSat.SaturationEvents )
			//	{
			//		allDates.add(satEvent);
			//	}
			//}
		}

		//ArrayList<Date> sortedAllDates = new ArrayList<Date>(allDates);
		//Collections.sort(sortedAllDates);
		
		// To have a fair linear date time, we need to start at the same time, and inc by one
		SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy");
		ArrayList<Date> sortedAllDates = new ArrayList<Date>();
		try
		{
			Date startDate = format.parse("07-31-2008");
			Date endDate = format.parse("12-01-2011"); // exclusive
			Date curDate = startDate;
			while( curDate.before(endDate) )
			{
				sortedAllDates.add(curDate);
				curDate = IncrementByDay(curDate);
			}
		}
		catch(ParseException ex)
		{
			ex.printStackTrace();
		}
		
		Hashtable<Integer,ArrayList<Integer>> saturationResults = new Hashtable<Integer,ArrayList<Integer>>();
		for( int level : levels)
		{
			ArrayList<Integer> results = new ArrayList<Integer>();
			List<ClassSaturation> saturatedSet = saturationData.get(level);
			
			for( Date d : sortedAllDates )
			{
				List<ClassSaturation> saturatedByDate = new ArrayList<ClassSaturation>();
				for( ClassSaturation sat : saturatedSet )
				{
					if( sat.Last().before(d))
					{
						saturatedByDate.add(sat);
					}
				}
				
				results.add(saturatedByDate.size());
			}
			
			saturationResults.put(level,results);
		}

		int row = 0;
		for(Date d : sortedAllDates) // row
		{
			System.out.print(format.format(d) +";");
			ArrayList<Integer> columns = new ArrayList<Integer>();
			for( int level : levels) // column
			{
				ArrayList<Integer> levelSat = saturationResults.get(level);
				columns.add(levelSat.get(row));
			}
			System.out.println(join(columns,";"));
			row++;
		}
		
		System.out.println("Total classes: " + classDocs.size());
		// size of crowd at day... (contributors)
	}
	
	public List<ClassSaturation> Saturation(int level, List<ClassDocumentation> classDocs)
	{
		List<ClassSaturation> saturatedSet = new ArrayList<ClassSaturation>();
		for( ClassDocumentation classDoc : classDocs )
		{
			// Is there enough threads to achieve saturation?
			if( classDoc.getValidatedThreads().size() >= level)
			{
				// Map of threads and earliest date of class reference in thread, sorted by date.
				Hashtable<ThreadElement,Date> dateThreadMap = new Hashtable<ThreadElement,Date>();
				for( ThreadElement thread: classDoc.getValidatedThreads())
				//for( ThreadElement thread: classDoc.ThreadElements)
				{
					Date d = GetDateComponent(MinDateInThread(thread,classDoc.Klass));
					dateThreadMap.put(thread,d);
				}
				
				ArrayList<Entry<ThreadElement,Date>> sortedThreadElements = new ArrayList<Entry<ThreadElement,Date>>(dateThreadMap.entrySet());
				Collections.sort( sortedThreadElements, new ThreadElementDateComparator() );
			
				// How early did we achieve saturation?
				// What is the time in between gaps? Is this quickening?
				ClassSaturation sat = new ClassSaturation();
				sat.ClassDoc = classDoc;
				List<Entry<ThreadElement,Date>> sub = sortedThreadElements.subList(0, level);
				
				for( Entry<ThreadElement,Date> saturationEvent : sub )
				{
					sat.SaturationEvents.add(saturationEvent.getValue());
				}
				
				saturatedSet.add(sat);
			}
		}
		return saturatedSet;
	}
	

	private Date GetDateComponent(Date in)
	{
		Calendar c = Calendar.getInstance();
		c.setTime(in);
		c.set(Calendar.HOUR, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTime();
	}
	
	private Date MinDateInThread(ThreadElement thread, ClassElem klass)
	{
		Date minDate = new Date(); // now
		for( AnswerElement answer : thread.Answers )
		{
			if( answer.IsValidated(klass))
			{
				if( answer.Date.before(minDate) )
				{
					minDate = answer.Date;
				}
			}
		}
		if( thread.Question.IsLinked && 
			thread.CreationDate.before(minDate) )
		{
			minDate = thread.CreationDate;
		}
		return minDate;
	}

	public static class ClassSaturation
	{
		public List<Date> SaturationEvents = new ArrayList<Date>();
		public ClassDocumentation ClassDoc;
		
		public Date First()
		{
			return SaturationEvents.get(0);
		}
		public Date Last()
		{
			return SaturationEvents.get(SaturationEvents.size()-1);
		}

	}
	
	public static class ThreadElementDateComparator implements Comparator<Entry<ThreadElement,Date>>
	{
		@Override
		public int compare(Entry<ThreadElement, Date> a, Entry<ThreadElement, Date> b) {
			return a.getValue().compareTo(b.getValue());
		}		
	}
	
	public static void main (String [] args) 
	{
		SpeedOfSaturation run = new SpeedOfSaturation();
		run.Run();
		//ArrayList<Integer> list = new ArrayList<Integer>();
		//for(int i = 0; i < 10; i++ )
		//{
		//	list.add(i);
		//}
		
		//System.out.println("size: " + list.size() + " sub" + list.subList(0, 5).size());
	}
}
