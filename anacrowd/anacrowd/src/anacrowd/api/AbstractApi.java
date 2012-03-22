package anacrowd.api;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import anacrowd.api.elements.ClassElem;
import anacrowd.api.elements.PackageElem;
import anacrowd.api.parser.ApiParser;

public class AbstractApi
{
	public List<PackageElem> Packages;
	public String MainTag;
	public String ClassDocPath;
	public ApiParser Parser;

	public void Parse(String path)
	{
		Parser.Parse(path);
		this.Packages = Parser.ParsedData;

		MarkClassNameCollisions();
		MarkClassSubsets();
	}
	
	public void MarkClassNameCollisions()
	{
		Hashtable<String,Integer> classSet = new Hashtable<String,Integer>();
		for( PackageElem pack: this.Packages)
		{
			for( ClassElem klass : pack.Classes )
			{
				if( !classSet.containsKey(klass.Name))
				{
					classSet.put(klass.Name, 0);
				}
				classSet.put(klass.Name, classSet.get(klass.Name)+1);
			}
		}

		for( PackageElem pack: this.Packages)
		{
			for( ClassElem klass : pack.Classes )
			{
				klass.HasClassNameCollision = classSet.get(klass.Name) > 1;
			}
		}
	}

	// Get the class names that this would intersect with:
	public void MarkClassSubsets()
	{
		// e.g. System, or R intersects with inner classes.
		for( PackageElem pack: this.Packages)
		{
			for( ClassElem klass : pack.Classes )
			{
				MarkClassSubsets(klass);
			}
		}
	}
	
	public void MarkClassSubsets(ClassElem check)
	{
		for( PackageElem pack: this.Packages)
		{
			for( ClassElem klass : pack.Classes )
			{
				// check: R
				// other: R.drawable
				if( !check.Name.equals(klass.Name) && klass.Name.startsWith(check.Name +"."))
				{
					check.MatchesAsSubset.add(klass);
				}
			}
		}
	}
}