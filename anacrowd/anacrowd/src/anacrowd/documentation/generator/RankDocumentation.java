package anacrowd.documentation.generator;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.PriorityQueue;

import com.sun.accessibility.internal.resources.accessibility;

import anacrowd.documentation.ClassDocumentation;
import anacrowd.documentation.elements.ClusteredLinkElement;
import anacrowd.documentation.elements.CodeSampleElement;
import anacrowd.documentation.elements.LinkElement;
import anacrowd.documentation.elements.ThreadElement;
import anacrowd.documentation.elements.LinkElement_old;;


public class RankDocumentation 
{
	public void Rank(ClassDocumentation doc)
	{
		// TODO sort lost...
		Collections.sort(doc.getCodeSampleElements(), new Comparator<CodeSampleElement>()
		{
			@Override
			public int compare(CodeSampleElement a, CodeSampleElement b) 
			{
				return b.Votes - a.Votes;
			}
		});

		// May contain invalidated threads.
		Collections.sort(doc.ThreadElements, new Comparator<ThreadElement>()
		{
			@Override
			public int compare(ThreadElement a, ThreadElement b) 
			{
				return b.Votes - a.Votes;
			}
		});
		
		// Not built, do it here
		Hashtable<String,ClusteredLinkElement> linkCount = new Hashtable<String,ClusteredLinkElement>();
		for( LinkElement link : doc.getHrefs() )
		{
			if( !linkCount.containsKey(link.Href))
			{
				ClusteredLinkElement clustered = new ClusteredLinkElement();
				clustered.Href = link.Href;
				linkCount.put(link.Href,clustered);
			}
			linkCount.get(link.Href).References++;
		}
		
		List<ClusteredLinkElement> elements = new ArrayList<ClusteredLinkElement>(linkCount.values());
		
		doc.ClusteredLinks.clear();
		doc.ClusteredLinks.addAll(elements);
		
		Collections.sort(doc.ClusteredLinks, new Comparator<ClusteredLinkElement>()
		{
			@Override
			public int compare(ClusteredLinkElement a, ClusteredLinkElement b) 
			{
				return (b.TotalVotes+b.References) - (a.TotalVotes+a.References);
			}
		});
	}
	

}
