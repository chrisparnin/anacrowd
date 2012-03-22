package anacrowd.documentation.links;

import java.util.Hashtable;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import anacrowd.documentation.elements.PostElement;
import anacrowd.documentation.links.CachedPostBody.SimpleHref;

public class CachePostBodyIndex 
{
	private static CachePostBodyIndex Index;
	
	// This should be one-to-one to a POST id
	private Hashtable<Integer,CachedPostBody> Cache = new Hashtable<Integer,CachedPostBody>();
	
	public static CachePostBodyIndex getInstance()
	{
		if( Index == null )
		{
			Index = new CachePostBodyIndex();
		}
		return Index;
	}
	
	public CachedPostBody ParseOrGet(PostElement post)
	{
		if( !Cache.containsKey(post.Id))
		{
			Cache.put(post.Id, ParsePostElement(post) );
		}
		
		return Cache.get(post.Id);
	}

	private CachedPostBody ParsePostElement(PostElement post) 
	{
		CachedPostBody cachedBody = new CachedPostBody();
		Document doc = Jsoup.parseBodyFragment(post.Body);
		
		Elements codeSnippets = doc.getElementsByTag("code");
		for( Element e : codeSnippets)
		{
			cachedBody.CodeBlocks.add(e.text());
			e.remove();
		}
		
		Elements hrefs = doc.getElementsByTag("a");
		for( Element e : hrefs)
		{
			SimpleHref href = new SimpleHref();
			href.AnchorText = e.text();
			href.HrefText = e.attr("href");
			cachedBody.Hrefs.add( href );
			e.remove();
		}
		
		Elements paragraphs = doc.getElementsByTag("p");
		for( Element e : paragraphs)
		{
			cachedBody.Paragraphs.add( e.text() );
			e.remove();
		}
		return cachedBody;
	}
}
