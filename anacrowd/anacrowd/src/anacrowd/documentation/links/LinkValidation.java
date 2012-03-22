package anacrowd.documentation.links;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import anacrowd.api.elements.ClassElem;
import anacrowd.documentation.elements.LinkElement;
import anacrowd.documentation.elements.PostElement;
import anacrowd.documentation.links.CachedPostBody.SimpleHref;
import anacrowd.documentation.links.LinkSpan.LinkType;
import anacrowd.metrics.coverage.WordDistributions;
import anacrowd.stackdb.DBInfo.PostInfo;

public class LinkValidation
{	
	public List<LinkSpan> Validate(ClassElem klassElem, PostElement post, String title, List<LinkElement> /*out*/hrefs)
	{
		List<LinkSpan> allSpans = ParseBody(klassElem,post,title, hrefs);
		for( LinkSpan s: allSpans )
		{
			s.PostId = post.Id;
		}
		return allSpans;
	}
	
	public List<LinkSpan> ParseBody(ClassElem klassElem, PostElement post, String title, List<LinkElement>/*out*/ hrefLinks )
	{
		List<LinkSpan> allSpans = new ArrayList<LinkSpan>();
		int i = 0;

		CachedPostBody postBody = CachePostBodyIndex.getInstance().ParseOrGet(post);
		
		i = 0;
		for( String text : postBody.CodeBlocks )
		{
			List<LinkSpan> spans = GatherSpans(klassElem, text);
			for( LinkSpan s: spans )
			{
				//s.Site = text;
				s.SiteIndex = i;
				// Conservative: might misses class.method code markups
				if( text.trim().equals(klassElem.Name))
				{
					s.LinkType = LinkType.CodeMarkupLink;
				}
				else
				{
					s.LinkType = LinkType.CodeSampleLink;
				}
			}
			i++;
			allSpans.addAll(spans);
		}

		i = 0;
		for( SimpleHref href : postBody.Hrefs)
		{
			List<LinkSpan> spans = GatherSpans(klassElem, href.AnchorText);
			for( LinkSpan s: spans )
			{
				//s.Site = text;
				s.SiteIndex = i;
				s.LinkType = LinkType.HrefLink;
			}
			i++;
			allSpans.addAll(spans);
			
			// Let's also cache all hrefs, since we're here...
			LinkElement link = new LinkElement();
			link.Href = href.HrefText;
			link.AnchorText = href.AnchorText;
			hrefLinks.add(link);
		}
		
		WordDistributions wd = new WordDistributions();
		// We do not follow Word Links for words like "Intent", "Activity".
		if (wd.WordLength(klassElem.Name) > 1)
		{
			i = 0;
			for( String text : postBody.Paragraphs )
			{
				List<LinkSpan> spans = GatherSpans(klassElem, text);
				for( LinkSpan s: spans )
				{
					//s.Site = text;
					s.SiteIndex = i;
					s.LinkType = LinkType.WordLink;
				}
				i++;
				allSpans.addAll(spans);
			}
			
			if( title != null )
			{
				List<LinkSpan> spans = GatherSpans(klassElem, title);
				for( LinkSpan s: spans )
				{
					//s.Site = text;
					s.SiteIndex = -1;
					s.LinkType = LinkType.WordLink;
				}
				allSpans.addAll(spans);
			}
		}

		return allSpans;
	}

	private List<LinkSpan> GatherSpans(ClassElem klassElem, String text)
	{
		List<LinkSpan> spans = new ArrayList<LinkSpan>();

		String klassName = klassElem.Name;
		if( klassElem.HasClassNameCollision )
		{
			klassName = klassElem.ParentPackage.Name + "." + klassElem.Name;
		}
		
		Pattern p = Pattern.compile("\\b"+Pattern.quote(klassName)+"\\b");
		Matcher matcher = p.matcher(text);
		int start=0;
		
		while(matcher.find(start))
		{
			int s = matcher.start();
			int e = matcher.start()+klassName.length();
			
			LinkSpan span = new LinkSpan();
			span.Start = s;
			span.End = e;
			span.Validated = true;

			for( ClassElem otherClass : klassElem.MatchesAsSubset )
			{
				String sub = text.substring(s);
				if( DoesMatchSubset(klassName, otherClass.Name, sub) )
				{
					span.Validated = false;
				}
			}
			spans.add(span);
			start = matcher.start() + 1;
		}
		
		return spans;
	}
	
	public boolean DoesMatchSubset(String klass, String otherClassName, String text)
	{
		Pattern o = Pattern.compile("^\\b"+Pattern.quote(otherClassName)+"\\b");
		Matcher matcher = o.matcher(text);
		return matcher.find();
	}
}
