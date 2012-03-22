package anacrowd.documentation;

import java.util.Hashtable;
import java.util.List;

import anacrowd.api.elements.ClassElem;
import anacrowd.documentation.elements.CodeSampleElement;
import anacrowd.documentation.elements.LinkElement;
import anacrowd.documentation.elements.ThreadElement;
import anacrowd.documentation.links.LinkSpan;

public class ClassIndex 
{
	public Hashtable<String,List<LinkSpan>> TraceabilityLinks = new Hashtable<String,List<LinkSpan>>();
	public Hashtable<String,List<LinkElement>> Hrefs = new Hashtable<String,List<LinkElement>>();
	//public Hashtable<String,CodeSampleElement> CodeSamples = new Hashtable<String,CodeSampleElement>();

	public Hashtable<Integer,ThreadElement> ThreadMap = new Hashtable<Integer,ThreadElement>();
	public Hashtable<Integer,CodeSampleElement> SampleMap = new Hashtable<Integer,CodeSampleElement>();
	
	private static ClassIndex index;
	public static ClassIndex getInstance()
	{
		if( index == null )
		{
			index = new ClassIndex();
		}
		return index;
	}
	
	public static String getKey(int threadId, int postId, ClassElem klass )
	{
		return threadId + "." + postId + "." + klass.getFQN();
	}
	
	
}