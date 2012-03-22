package anacrowd.documentation.generator;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;

import anacrowd.documentation.ClassDocumentation;
import anacrowd.documentation.elements.ClusteredLinkElement;
import anacrowd.documentation.elements.CodeSampleElement;
import anacrowd.documentation.elements.CodeSampleElement.CodeBlock;
import anacrowd.documentation.elements.LinkElement;
import anacrowd.documentation.elements.ThreadElement;

public class ExportDocumentation {

	public void Export( ClassDocumentation doc)
	{
		List<CodeSampleElement> top5code = doc.getCodeSampleElements().subList(0, 
				Math.min(doc.getCodeSampleElements().size(), 5));
		List<ClusteredLinkElement> top10links = doc.ClusteredLinks.subList(0, 
				Math.min(doc.ClusteredLinks.size(),10));
		List<ThreadElement> top5questions = doc.ThreadElements.subList(0, 
				Math.min(doc.ThreadElements.size(),5));

		ThereCanOnlyBeOne(top10links);
		
		Collections.sort(top10links, new Comparator<ClusteredLinkElement>()
		{
			@Override
			public int compare(ClusteredLinkElement a, ClusteredLinkElement b) 
			{
				return (b.TotalVotes+b.References) - (a.TotalVotes+a.References);
			}
		});
		
		List<ClusteredLinkElement> top5links = top10links.subList(0, 
				Math.min(top10links.size(),5));

		
		for( ThreadElement thread: top5questions )
		{
			System.out.println(thread.Votes + " " + thread.Question.Title + " " + thread.Answers.size() 
					+ " " + thread.ViewCount + " " + thread.FavoriteCount);
			System.out.println( thread.Question.GetStackOverflowUrl() );
		}
		
		System.out.println("Hrefs...");
		for( ClusteredLinkElement link : top5links )
		{
			System.out.println(link.References + " " + link.Href );
		}
		
		for( CodeSampleElement code : top5code )
		{
			System.out.println("%%%%%%%%%%%%%: Score: + " + code.Votes);
			for( CodeBlock block : code.Blocks )
			{
				System.out.println( block.getBody() );
			}
			System.out.println();System.out.println();
		}
		
		
		
		//List<CodeSampleElement> top5code = doc.getHrefs()().subList(0, 
		//		Math.min(doc.getCodeSampleElements().size(), 5));
		
	}
	
	public void ThereCanOnlyBeOne(List<ClusteredLinkElement> links)
	{
		List<ClusteredLinkElement> removeMe = new ArrayList<ClusteredLinkElement>();

		for( ClusteredLinkElement link : links )
		{
			for( ClusteredLinkElement other : links )
			{
				if( removeMe.contains(other))
					continue;

				String a = getDomainName(link.Href);
				String b = getDomainName(other.Href);
				
				if (a.length() > 0 && a.equals(b) && 
					other.Href.length() > link.Href.length())
				{
					// The quickening
					other.References += link.References;
					other.TotalVotes += link.TotalVotes;
					removeMe.add(link);
				}
			}
		}
		
		for( ClusteredLinkElement dead : removeMe )
		{
			links.remove(dead);
		}
	}
	
	static String getDomainName(String url)
    {
         URL u;
         try {
             u = new URL(url);
         } 
         catch (Exception e) { 
             return "";
         }
         return u.getHost();
    }

}
