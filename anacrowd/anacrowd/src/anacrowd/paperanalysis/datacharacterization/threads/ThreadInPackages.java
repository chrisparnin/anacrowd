package anacrowd.paperanalysis.datacharacterization.threads;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import anacrowd.documentation.ClassDocumentation;
import anacrowd.documentation.elements.ClusteredLinkElement;
import anacrowd.documentation.elements.ThreadElement;
import anacrowd.paperanalysis.BaseAnalysis;

import com.sun.xml.internal.ws.encoding.soap.DeserializationException;

public class ThreadInPackages extends BaseAnalysis
{
	@Override
	public void Run()
	{
		List<ClassDocumentation> docs = DeserializeModel(Android);
		
		Hashtable<String,HashSet<Integer>> PackageMap = new Hashtable<String,HashSet<Integer>>();
		for( ClassDocumentation doc: docs )
		{
			String p = doc.Klass.ParentPackage.Name;
			if( !PackageMap.containsKey(p))
			{
				PackageMap.put(p, new HashSet<Integer>());
			}
			for( ThreadElement t: doc.getValidatedThreads() )
			{
				PackageMap.get(p).add(t.Question.Id);
			}
		}

		List<MyEntry<String,Integer>> sortedPackages = new ArrayList<MyEntry<String,Integer>>();
		for( String key: PackageMap.keySet() )
		{
			Integer count = PackageMap.get(key).size();
			sortedPackages.add(new MyEntry<String,Integer>(key,count));
		}
		
		Collections.sort(sortedPackages, new Comparator<MyEntry<String, Integer>>()
		{
			@Override
			public int compare(MyEntry<String, Integer> a,
					MyEntry<String, Integer> b) {
				return b.getValue() - a.getValue();
			}
		});
		
		for( MyEntry<String,Integer> p : sortedPackages )
		{
			System.out.println(p.getKey() + "," + p.getValue() );
		}
	}
	public static void main(String args[])
	{
		new ThreadInPackages().Run();
	}
}
