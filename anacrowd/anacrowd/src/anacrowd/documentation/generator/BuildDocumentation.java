package anacrowd.documentation.generator;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import anacrowd.api.AbstractApi;
import anacrowd.api.AndroidApi;
import anacrowd.documentation.ClassDocumentation;
import anacrowd.documentation.elements.AnswerElement;
import anacrowd.documentation.elements.ClusteredLinkElement;
import anacrowd.documentation.elements.CodeSampleElement;
import anacrowd.documentation.elements.CodeSampleElement.CodeBlock;
import anacrowd.documentation.elements.CodeSampleElement.CodeSpan;
import anacrowd.documentation.elements.LinkElement_old;
import anacrowd.documentation.elements.ThreadElement;
import anacrowd.stackdb.DBInfo;
import anacrowd.stackdb.DBInfo.PostInfo;

public class BuildDocumentation 
{
	// Highly rated code samples
	// Titles of threads, ordered by ratings.
	// External Resources

	DBInfo db;
	public BuildDocumentation()
	{
		db = new DBInfo();
		db.Init();
	}
	public BuildDocumentation(String testMode)
	{
	}
	
	public List<ClassDocumentation> PrepareApi(AbstractApi api)
	{
		LinkDocumentation linker = new LinkDocumentation();
		return linker.LinkApi(api);
	}
	
	public void Build( ClassDocumentation doc, boolean parseCodeSnippets, boolean parseLinks)
	{
		// http://stackoverflow.com/questions/1497946/how-can-i-parse-a-html-string-in-java
		BuildInternal(doc, parseCodeSnippets, parseLinks);
	}
	
	public ClassDocumentation BuildForClass(AbstractApi api, String klass)
	{
		LinkDocumentation linker = new LinkDocumentation();
		ClassDocumentation doc = linker.LinkOnlyClass(api, klass);

		BuildInternal(doc, true,true);
		return doc;
	}
	
	private void BuildInternal(ClassDocumentation doc, boolean parseCodeSnippets, boolean parseLinks) 
	{
		// Threads
		for( ThreadElement thread : doc.ThreadElements)
		{
			PostInfo info = db.GetPostDetail(thread.Question.Id);
			thread.Question.Title = info.Title;
			thread.Votes = info.Score;
			thread.CreationDate = info.CreationDate;
			
			for( AnswerElement ans : thread.Answers )
			{
				if( ans.IsValidated(doc.Klass))
				{
					PostInfo ansInfo = db.GetPostDetail(ans.Id);
					ans.Votes = ansInfo.Score;
					ans.Date = ansInfo.CreationDate;
				}
			}
		}
		
		if( parseCodeSnippets)
		{
			BuildCodeSnippets(doc);
		}
		if( parseLinks)
		{
			BuildLinks(doc);
		}
	}
	
	public void BuildCodeSnippets(ClassDocumentation doc)
	{
		for( CodeSampleElement code: doc.getCodeSampleElements() )
		{
			PostInfo info = db.GetPostDetail(code.Parent.Id);
			//System.out.print(info.Title);
			code.Blocks = ParseFragments(info.getBody(), doc.Klass.Name);
			//System.out.println(" " + code.Blocks.size());
			code.Votes = info.Score;
		}
	}
	
	public void BuildLinks(ClassDocumentation doc)
	{
		Hashtable<String,ClusteredLinkElement> linkCount = new Hashtable<String,ClusteredLinkElement>();
//		for( LinkElement_old link : doc.ExternalResources )
//		{
//			for( String href : link.ExternalLinks )
//			{
//				if( !linkCount.containsKey(href))
//				{
//					ClusteredLinkElement clustered = new ClusteredLinkElement();
//					clustered.Href = href;
//					linkCount.put(href,clustered);
//				}
//				linkCount.get(href).References++;
//				linkCount.get(href).TotalVotes += link.Votes;
//			}
//		}

		doc.ClusteredLinks.addAll(linkCount.values());
	}
	
	public List<CodeBlock> ParseFragments(String body, String klass)
	{
		// http://jsoup.org/cookbook/input/parse-body-fragment
		Document doc = Jsoup.parseBodyFragment(body);
		Elements codeSnippets = doc.getElementsByTag("code");
		List<CodeBlock> blocks = new ArrayList<CodeBlock>();
		for( Element e : codeSnippets)
		{
			CodeBlock block = new CodeBlock();
			String text = e.text();
			block.setBody( text );
			
			Pattern p = Pattern.compile("\\b"+Pattern.quote(klass)+"\\b");
			Matcher matcher = p.matcher(text);
			int start=0;
			while(matcher.find(start))
			{
				//block.ClassMatches.add(new CodeSpan(matcher.start(),matcher.start()+klass.length()));
				start = matcher.start() + 1;
			}
			blocks.add(block);
		}
		return blocks;
	}
	
	public List<String> ParseLinks(String body, String klass)
	{
		Document doc = Jsoup.parseBodyFragment(body);
		Elements codeSnippets = doc.getElementsByTag("a");
		List<String> links = new ArrayList<String>();
		for( Element e : codeSnippets)
		{
			if( e.text().trim().equals(klass) )
			{
				links.add(e.attr("href"));
			}
		}
		return links;
	}

	public static String Test =
		"<div class=\"post-text\"><p>I found this somewhere (don't remember where):</p>"+
"<pre><code> public static DocumentFragment parseXml(Document doc, String fragment)" +
"{" +
"    // Wrap the fragment in an arbitrary element." +
"    fragment = \"&lt;fragment&gt;\"+fragment+\"&lt;/fragment&gt;\";" +
"    try" +
"    {" +
"        // Create a DOM builder and parse the fragment." +
"        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();"  +
"        Document d = factory.newDocumentBuilder().parse(" +
"                new InputSource(new StringReader(fragment)));" +
"        // Import the nodes of the new document into doc so that they" +
"        // will be compatible with doc." +
"        Node node = doc.importNode(d.getDocumentElement(), true);" +
"        // Create the document fragment node to hold the new nodes." +
"        DocumentFragment docfrag = doc.createDocumentFragment();" +
"        // Move the nodes into the fragment." +
"       while (node.hasChildNodes())" +
"        {" +
"            docfrag.appendChild(node.removeChild(node.getFirstChild()));" +
"        }" +
"        // Return the fragment." +
"        return docfrag;" +
"    }" +
"    catch (SAXException e)" +
"    {" +
"        // A parsing error occurred; the XML input is not valid." +
"    }" +
"    catch (ParserConfigurationException e)" +
"    {" +
"    }" +
"    catch (IOException e)" +
"    {" +
"    }" +
"    return null;" +
"}" +
"</code></pre>" +
"</div>";

	public static String TestLinks = 
		"<div class=\"post-text\"><p><a href=\"http://download.oracle.com/javase/6/docs/api/java/security/MessageDigest.html\" rel=\"nofollow\">MessageDigest</a> is your friend. Call <a href=\"http://download.oracle.com/javase/6/docs/api/java/security/MessageDigest.html#getInstance%28java.lang.String%29\" rel=\"nofollow\">getInstance(\"MD5\")</a> to get an MD5 message digest you can use.</p></div>";
	
	public void TestParse()
	{
		List<CodeBlock> blocks = this.ParseFragments(Test,"Document");
		
		for( CodeBlock block : blocks )
		{
			System.out.println(block.ClassMatches.size());
			System.out.println(block.getBody());
		}
		
		List<String> links = this.ParseLinks(TestLinks, "MessageDigest");
		for( String link : links )
		{
			System.out.println(link);
		}
	}
	
	public static void main(String[] args)
	{
		BuildDocumentation b = new BuildDocumentation();
		b.TestParse();
		
		//ClassDocumentation doc = b.BuildForClass(new AndroidApi(),"AccountManager");
		ClassDocumentation doc = b.BuildForClass(new AndroidApi(),"Drawable");
		RankDocumentation ranker = new RankDocumentation();
		ranker.Rank(doc);
		ExportDocumentation exporter = new ExportDocumentation();
		exporter.Export(doc);
	}
}
