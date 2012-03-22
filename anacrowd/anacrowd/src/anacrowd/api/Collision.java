package anacrowd.api;

import java.util.HashSet;

import anacrowd.api.elements.ClassElem;
import anacrowd.api.elements.MethodElem;
import anacrowd.api.elements.PackageElem;

public class Collision 
{
	public void ReportCollision(AbstractApi api)
	{
		HashSet<String> uniqueClasses = new HashSet<String>();
		HashSet<String> uniqueMethods = new HashSet<String>();
		HashSet<String> overloadedMethods = new HashSet<String>();

		int classCount = 0;
		int methodCount = 0;

		for( PackageElem pack: api.Packages )
		{
			for( ClassElem klass : pack.Classes )
			{
				uniqueClasses.add(klass.Name);
				classCount++;
				
				for( MethodElem meth: klass.Methods )
				{
					uniqueMethods.add(meth.Name);
					overloadedMethods.add(pack.Name+":"+klass.Name+":"+meth.Name);

					methodCount++;
				}
			}
		}
		
		System.out.println("Unique classes: " + uniqueClasses.size());
		System.out.println("Total classes: " + classCount);
		
		System.out.println("Unique methods: " + uniqueMethods.size());
		System.out.println("Total methods(no overloads): " + overloadedMethods.size());
		System.out.println("Total methods: " + methodCount);

	}
}
