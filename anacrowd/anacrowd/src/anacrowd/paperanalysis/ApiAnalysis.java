package anacrowd.paperanalysis;

import java.util.HashSet;

import anacrowd.api.AbstractApi;
import anacrowd.api.elements.ClassElem;
import anacrowd.api.elements.PackageElem;

public class ApiAnalysis extends BaseAnalysis
{
	@Override
	public void Run()
	{
		System.out.println("Android");
		ClassNameCollisions(Android);

		System.out.println("Java");
		ClassNameCollisions(Java);
		
		System.out.println("GWT");
		ClassNameCollisions(GWT);
	}
	
	public void ClassNameCollisions(AbstractApi api)
	{
		int classCount = 0;
		HashSet<String> classSet = new HashSet<String>();
		for( PackageElem pack: api.Packages)
		{
			for( ClassElem klass : pack.Classes )
			{
				classCount++;
				classSet.add(klass.Name);
			}
		}
		System.out.println("ClassNameCollisions: " + classSet.size() + "/" + classCount);
	}

	public static void main(String args[])
	{
		ApiAnalysis api = new ApiAnalysis();
		api.Run();
	}
}
