package anacrowd.paperanalysis.usage;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.json.JSONException;

import anacrowd.api.AbstractApi;
import anacrowd.documentation.ClassDocumentation;
import anacrowd.documentation.generator.SerializeDocumentation;
import anacrowd.paperanalysis.BaseAnalysis;

public class OutputActivityAndUsage extends BaseAnalysis 
{
	@Override
	public void Run()
	{
		//OutputApi(Android,"classdoc/android","../data/android.usage.csv");
		OutputApi(Java,"classdoc/java","../data/java.usage.csv");

	}
		
	public void OutputApi(AbstractApi api, String classdocpath, String usageCSV)
	{
		try 
		{
			SerializeDocumentation serializer = new SerializeDocumentation();
			List<ClassDocumentation> docs = serializer.DeserializeClassDocumentation(classdocpath,api);

			Hashtable<String,Integer> usageMap = ReadUsage(usageCSV);
			
			System.out.println("FQN,usage,threads");
			for( ClassDocumentation doc : docs)
			{
				String classFQN = doc.Klass.getFQN();
				int usage = usageMap.get(classFQN);
				int threads = doc.getValidatedThreads().size();
				
				System.out.println(classFQN + "," + usage + "," + threads );
			}
		} 
		catch (JSONException e) 
		{
			e.printStackTrace();
		} catch (Exception e) 
		{
			e.printStackTrace();
		}
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
		BaseAnalysis a = new OutputActivityAndUsage();
		a.Run();
	}
}
