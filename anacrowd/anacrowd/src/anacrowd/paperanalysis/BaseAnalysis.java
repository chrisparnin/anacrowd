package anacrowd.paperanalysis;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import anacrowd.api.AbstractApi;
import anacrowd.api.AndroidApi;
import anacrowd.api.GWTApi;
import anacrowd.api.JavaApi;
import anacrowd.api.elements.ClassElem;
import anacrowd.api.elements.PackageElem;
import anacrowd.cache.ResultCache;
import anacrowd.documentation.ClassDocumentation;
import anacrowd.documentation.generator.SerializeDocumentation;
import anacrowd.metrics.coverage.WordDistributions;
import anacrowd.paperanalysis.ClassTagReliability.QATuple;
import anacrowd.stackdb.Search;

public class BaseAnalysis 
{
	public AndroidApi Android;
	public JavaApi Java;
	public GWTApi GWT;
	
	public String QuestionsTempTableName;
	public String AnswersTempTableName;
	
	public BaseAnalysis()
	{
		Android = new AndroidApi();
		Android.Parse("/Users/gameweld/Documents/blogs/tools/stackoverflow/QuestionSnab/api_current.xml");	

		Java = new JavaApi();
		Java.Parse("/Users/gameweld/Documents/blogs/tools/stackoverflow/data/java.xml");
		
		GWT = new GWTApi();
		GWT.Parse("/Users/gameweld/Documents/blogs/tools/stackoverflow/data/gwt.txt");
	}
	
	public void PrepareApiForSearch(Search info, AbstractApi api)
	{
		String qTable = api.MainTag + "questions_table";
		String aTable = api.MainTag + "answers_table";
		info.CreateTemporaryTable(api.MainTag, qTable);
		info.CreateTemporaryRelatedTable(api.MainTag, aTable, qTable);
		
		this.AnswersTempTableName = aTable;
		this.QuestionsTempTableName = qTable;
	}
	
	public void EnsureCacheIsBuilt()
	{
		ResultCache cache = new ResultCache();
		if( !cache.Exists() )
		{
			Date now = new Date();
			System.out.println("Make yourself some tea or coffee...gonna build a cache.");
			
			BuildCacheForApi(Android);
			System.out.println("Android done");

			BuildCacheForApi(GWT);
			System.out.println("GWT done");
			
			BuildCacheForApi(Java);
			System.out.println("Java done");
			
			Date finish = new Date();
			System.out.println("Cache complete, start time: " + now + " finish time: " + finish );
		}
	}
	
	public List<ClassDocumentation> DeserializeModel(AbstractApi api)
	{
		SerializeDocumentation serializeDoc = new SerializeDocumentation();
		try 
		{
			return serializeDoc.DeserializeClassDocumentation(api.ClassDocPath, api);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public void BuildCacheForApi(AbstractApi api)
	{
		Search search = new Search();
		search.Init();

		PrepareApiForSearch(search, api);
				
		for( PackageElem pack : api.Packages )
		{
			System.out.println("Building cache for classes in " + pack.Name);
			for( ClassElem klass : pack.Classes )
			{
				// Questions
				search.GetIdsViaTags(this.QuestionsTempTableName, klass.Name);
				search.GetIdsViaWordMatches(this.QuestionsTempTableName, klass.Name);
				search.GetIdsViaCodeMatches(this.QuestionsTempTableName, klass.Name);
				search.GetIdsViaLinkMatches(this.QuestionsTempTableName, klass.Name);

				// Answers
				search.GetIdsViaTags(this.AnswersTempTableName, klass.Name);
				search.GetIdsViaWordMatches(this.AnswersTempTableName, klass.Name);				
				search.GetIdsViaCodeMatches(this.AnswersTempTableName, klass.Name);
				search.GetIdsViaLinkMatches(this.AnswersTempTableName, klass.Name);
			}
		}
		search.Close();
	}
	
	public void Run()
	{
		EnsureCacheIsBuilt();
	}
	
	protected static String join(Collection<?> s, String delimiter) {
	     StringBuilder builder = new StringBuilder();
	     Iterator iter = s.iterator();
	     while (iter.hasNext()) {
	         builder.append(iter.next());
	         if (!iter.hasNext()) {
	           break;                  
	         }
	         builder.append(delimiter);
	     }
	     return builder.toString();
	 }

	
	 public static String join(String[] stringsA, String delimiter) {
	     StringBuilder builder = new StringBuilder();
	     Collection<String> strings = Arrays.asList(stringsA);
	     Iterator iter = strings.iterator();
	     while (iter.hasNext()) {
	         builder.append(iter.next());
	         if (!iter.hasNext()) {
	           break;                  
	         }
	         builder.append(delimiter);
	     }
	     return builder.toString();
	 }
	 
	 public static String readStream(InputStream is) {
		    StringBuilder sb = new StringBuilder(512);
		    try {
		        Reader r = new InputStreamReader(is, "UTF-8");
		        int c = 0;
		        while (c != -1) {
		            c = r.read();
		            sb.append((char) c);
		        }
		    } catch (IOException e) {
		        throw new RuntimeException(e);
		    }
		    return sb.toString();
		}

}
