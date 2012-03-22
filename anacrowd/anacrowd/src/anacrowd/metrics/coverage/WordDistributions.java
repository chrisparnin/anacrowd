package anacrowd.metrics.coverage;

import java.util.List;

import com.sun.org.apache.xerces.internal.impl.xs.identity.Selector.Matcher;

import anacrowd.api.AbstractApi;
import anacrowd.api.elements.ClassElem;
import anacrowd.api.elements.MethodElem;
import anacrowd.api.elements.PackageElem;

public class WordDistributions 
{
	public void Distribution( AbstractApi api )
	{
		for( PackageElem pack : api.Packages )
		{
			for( ClassElem klass : pack.Classes )
			{
				//System.out.println( klass.Name + " => " + WordSplit(klass.Name) + "(" + WordLength(klass.Name)+ ")");
				System.out.println( "class" + " " + klass.Name + " " + WordLength(klass.Name) );
				for( MethodElem meth : klass.Methods )
				{
					// System.out.println( meth.Name + " => " + WordSplit(meth.Name) + "(" + WordLength(meth.Name)+ ")");
					System.out.println( "method" + " " + meth.Name + " " + WordLength(meth.Name) );
				}
			}
		}
	}
	
	// * http://stackoverflow.com/questions/2559759/how-do-i-convert-camelcase-into-human-readable-names-in-java
	public int WordLength( String id )
	{
		//id = id.replaceAll("[.]", "");
		id = id.replaceAll("_", "");
		String words = id.replaceAll("(?<=[A-Z])(?=[A-Z][a-z])|" + "(?<=[^A-Z])(?=[A-Z])|" + "(?<=[A-Za-z])(?=[^A-Za-z])",
			      " ");
		return words.split(" ").length;
	}
	
	public String WordSplit( String id )
	{
		id = id.replaceAll("_", "");
		return id.replaceAll("(?<=[A-Z])(?=[A-Z][a-z])|" + "(?<=[^A-Z])(?=[A-Z])|" + "(?<=[A-Za-z])(?=[^A-Za-z])"," ");
	}
}
