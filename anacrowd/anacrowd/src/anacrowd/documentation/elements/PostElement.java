package anacrowd.documentation.elements;

import java.util.ArrayList;
import java.util.List;

import anacrowd.api.elements.ClassElem;
import anacrowd.documentation.ClassIndex;
import anacrowd.documentation.links.LinkSpan;

public class PostElement 
{
	public int Id;
	public int UserId;
	public String Body;
	
	//public CodeSampleElement CodeSample;
	//public List<LinkSpan> Links = new ArrayList<LinkSpan>();
	//public List<LinkElement> Hrefs = new ArrayList<LinkElement>();

	public String GetStackOverflowUrl()
	{
		return "http://stackoverflow.com/questions/" + Parent.Question.Id;
	}
	
	public ThreadElement Parent;
	
	public String getKey(ClassElem klass)
	{
		return Parent.Question.Id + "." + Id + "." + klass.getFQN();
	}
	
	public CodeSampleElement getCodeSample()
	{
		if( !ClassIndex.getInstance().SampleMap.containsKey( Id ))
		{
			return null;
		}
		CodeSampleElement you = ClassIndex.getInstance().SampleMap.get( Id );
		you.Parent = this;
		return you;
	}
	
	//public void setCodeSample(CodeSampleElement sample, ClassElem klass)
	//{
	//	ClassIndex.getInstance().CodeSamples.put(getKey(klass), sample);
	//}
	
	public List<LinkElement> getHrefs(ClassElem klass)
	{
		if( !ClassIndex.getInstance().Hrefs.containsKey(getKey(klass)) )
		{
			return new ArrayList<LinkElement>();
		}
		return ClassIndex.getInstance().Hrefs.get(getKey(klass));
	}
	
	public void setHrefs(List<LinkElement> set, ClassElem klass)
	{
		ClassIndex.getInstance().Hrefs.put(getKey(klass), set);
	}
	
	public List<LinkSpan> getLinks(ClassElem klass)
	{
		if( !ClassIndex.getInstance().TraceabilityLinks.containsKey(getKey(klass)) )
		{
			return new ArrayList<LinkSpan>();
		}
		return ClassIndex.getInstance().TraceabilityLinks.get(getKey(klass));
	}
	
	public void setLinks(List<LinkSpan> links, ClassElem klass)
	{
		ClassIndex.getInstance().TraceabilityLinks.put(getKey(klass), links);
	}
	
	public boolean IsValidated(ClassElem klass)
	{
		for( LinkSpan span : this.getLinks(klass) )
		{
			if( span.Validated )
				return true;
		}
		return false;
	}
}
