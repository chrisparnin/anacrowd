package anacrowd.documentation.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import anacrowd.api.AbstractApi;
import anacrowd.api.elements.ClassElem;
import anacrowd.api.elements.PackageElem;
import anacrowd.documentation.ClassDocumentation;
import anacrowd.documentation.ClassIndex;
import anacrowd.documentation.elements.AnswerElement;
import anacrowd.documentation.elements.ClusteredLinkElement;
import anacrowd.documentation.elements.CodeSampleElement;
import anacrowd.documentation.elements.CodeSampleElement.CodeBlock;
import anacrowd.documentation.elements.CodeSampleElement.CodeSpan;
import anacrowd.documentation.elements.LinkElement;
import anacrowd.documentation.elements.LinkElement_old;
import anacrowd.documentation.elements.PostElement;
import anacrowd.documentation.elements.QuestionElement;
import anacrowd.documentation.elements.ThreadElement;
import anacrowd.documentation.links.LinkSpan;
import anacrowd.documentation.links.LinkSpan.LinkType;

public class SerializeDocumentation 
{	
	public List<ClassDocumentation> DeserializeClassDocumentation(String path, AbstractApi api) 
			throws Exception
	{
		List<ClassDocumentation> list = new ArrayList<ClassDocumentation>();
		//Hashtable<Integer,ThreadElement> ThreadMap = DeserializeThreadElementsPool(path);
		ClassIndex.getInstance().ThreadMap = DeserializeThreadElementsPool(path);
		ClassIndex.getInstance().SampleMap = DeserializeCodeSampleElementsPool(path);
		
		for( PackageElem pack : api.Packages )
		{
			for( ClassElem klass : pack.Classes )
			{
				File filePath = new File(path + File.separator + pack.Name + "."  + klass.Name + ".json");
				if( !filePath.exists() )
				{
					throw new Exception("File does not exist: " + filePath.getCanonicalPath());
				}
				System.out.println("Deserializing: " + klass.Name);
				list.add(Deserialize(readStream(new FileInputStream(filePath)),ClassIndex.getInstance().ThreadMap));
			}
		}
		return list;
	}
	
	public static String readStream(InputStream is) {
	    StringBuilder sb = new StringBuilder(512);
	    try {
	        Reader r = new InputStreamReader(is, "UTF-8");
	        int c = 0;
	        while (c != -1) {
	            c = r.read();
	            sb.append((char) c);
	        }
	    } catch (IOException e) {
	        throw new RuntimeException(e);
	    }
	    return sb.toString();
	}
	

	public void SerializeClassDoc(String path, ClassDocumentation doc,
			File outputFile) {
		try
		{
			String serializedObj = Serialize(doc);
			File outputDir = new File(path);
			if( !outputDir.exists() )
			{
				outputDir.mkdirs();
			}
			
			FileWriter writer = new FileWriter(outputFile);
			writer.append(serializedObj);
			writer.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public String Serialize(ClassDocumentation doc) throws JSONException
	{
		JSONObject obj = new JSONObject();

		//doc.Klass
		obj.put("ParentPackageName",doc.Klass.ParentPackage.Name);
		obj.put("ClassName", doc.Klass.Name);
		
		// Don't serialize code samples, rebuild from scratch...
		//obj.put("CodeSampleElements", SerializeCodeSamples(doc.CodeSampleElements));		
		
		//obj.put("ExternalResources", SerializeExternalResources(doc.ExternalResources));
		obj.put("ClusteredLinks", SerializeClusteredLinks(doc.ClusteredLinks));
		
		// We only serialize thread ids now, and later retrieve thread from thread pool.
		//obj.put("ThreadElements", SerializeThreadElements(doc.ThreadElements));
		List<Integer> threadIds = new ArrayList<Integer>();
		for( ThreadElement t : doc.ThreadElements )
		{
			threadIds.add(t.Question.Id);
		}
		obj.put("ThreadElements", threadIds);

		List<Integer> sampleIds = new ArrayList<Integer>();
		for( CodeSampleElement s : doc.getCodeSampleElements() )
		{
			sampleIds.add(s.Parent.Id);
		}
		obj.put("Samples", sampleIds);
		
		// Links get moved to Class Object.
		List<JSONObject> links = new ArrayList<JSONObject>();
		for( ThreadElement t: doc.ThreadElements )
		{
			for( LinkSpan link : t.Question.getLinks(doc.Klass) )
			{
				links.add(SerializeLinkSpan(link,t,t.Question));
			}
			
			for( AnswerElement ans : t.Answers )
			{
				for( LinkSpan link : ans.getLinks(doc.Klass) )
				{
					links.add(SerializeLinkSpan(link,t,ans));
				}
			}
		}
		obj.put("Links", links);

		
		// Hrefs get moved to Class Object.
		List<JSONObject> hrefs = new ArrayList<JSONObject>();
		for( ThreadElement t: doc.ThreadElements )
		{
			for( LinkElement href : t.Question.getHrefs(doc.Klass) )
			{
				hrefs.add(SerializeHref(href,t,t.Question));
			}
			
			for( AnswerElement ans : t.Answers )
			{
				for( LinkElement href : ans.getHrefs(doc.Klass) )
				{
					hrefs.add(SerializeHref(href,t,ans));
				}
			}
		}
		obj.put("Hrefs", hrefs);
			
		return obj.toString(); 
	}
	
	public ClassDocumentation Deserialize(String source, Hashtable<Integer,ThreadElement> threadMap) throws JSONException
	{
		JSONObject obj = new JSONObject(source);
		ClassDocumentation doc = new ClassDocumentation();

		//doc.Klass
		doc.Klass = new ClassElem();
		doc.Klass.ParentPackage = new PackageElem();
		doc.Klass.ParentPackage.Name = obj.getString("ParentPackageName");
		doc.Klass.Name = obj.getString("ClassName");

		//doc.ExternalResources = DeserializeExternalResources(obj.getJSONArray("ExternalResources"));
		doc.ClusteredLinks = DeserializeClusteredLinks(obj.getJSONArray("ClusteredLinks"));
		
		// ThreadElements no longer per class. Now only id is stored,
		// and retrieved from thread pool.
		//doc.ThreadElements = DeserializeThreadElements();

		doc.ThreadElements = new ArrayList<ThreadElement>();
		JSONArray array = obj.getJSONArray("ThreadElements");
		for( int i = 0; i < array.length(); i++ )
		{
			int id = array.getInt(i);
			doc.ThreadElements.add(threadMap.get(id));
		}

		// Rebuild Code Samples from threads, not serialized.
		
		JSONArray links = obj.getJSONArray("Links");
		for( int i = 0; i < links.length(); i++ )
		{
			JSONObject jsonLink = links.getJSONObject(i);
			LinkSpan link = DeserializeLinkSpan(jsonLink);

			int tId = jsonLink.getInt("Thread");
			int pId = jsonLink.getInt("Post");
			
			String key = ClassIndex.getKey(tId, pId, doc.Klass);
			if(!ClassIndex.getInstance().TraceabilityLinks.containsKey(key))
			{
				ClassIndex.getInstance().TraceabilityLinks.put(key, new ArrayList<LinkSpan>());
			}
			ClassIndex.getInstance().TraceabilityLinks.get(key).add(link);
		}

		// hrefs
		JSONArray hrefs = obj.getJSONArray("Hrefs");
		for( int i = 0; i < hrefs.length(); i++ )
		{
			JSONObject jsonHref = hrefs.getJSONObject(i);
			LinkElement link = DeserializeHref(jsonHref);

			int tId = jsonHref.getInt("Thread");
			int pId = jsonHref.getInt("Post");
			
			String key = ClassIndex.getKey(tId, pId, doc.Klass);
			if(!ClassIndex.getInstance().Hrefs.containsKey(key))
			{
				ClassIndex.getInstance().Hrefs.put(key, new ArrayList<LinkElement>());
			}
			ClassIndex.getInstance().Hrefs.get(key).add(link);
		}

		
		return doc;
	}

	
	public List<JSONObject> SerializeCodeSamples(List<CodeSampleElement> links ) throws JSONException
	{
		List<JSONObject> codeSampleObjects = new ArrayList<JSONObject>();
		for( CodeSampleElement l : links )
		{
			codeSampleObjects.add(SerializeCodeSampleElement(l));
		}
		return codeSampleObjects;
	}
	
	public List<CodeSampleElement> DeserializeCodeSamples(JSONArray codeSampleObjects ) throws JSONException
	{
		List<CodeSampleElement> samples = new ArrayList<CodeSampleElement>();
		for( int i=0; i < codeSampleObjects.length(); i++ )
		{
			samples.add(DeserializeCodeSampleElement(codeSampleObjects.getJSONObject(i)));
		}
		return samples;
	}

	
	public JSONObject SerializeCodeSampleElement(CodeSampleElement code) throws JSONException
	{
		if( code == null )
			return null;
		JSONObject obj = new JSONObject();
		obj.put("Parent", SerializePostElement(code.Parent));
		
		List<JSONObject> blockObjects = new ArrayList<JSONObject>();
		for( CodeBlock block : code.Blocks )
		{
			blockObjects.add(SerializeCodeBlock(block));
		}
		
		obj.put("Blocks", blockObjects);
		obj.put("Votes", code.Votes);
		return obj;
	}
	
	public CodeSampleElement DeserializeCodeSampleElement(JSONObject obj) throws JSONException
	{
		CodeSampleElement code = new CodeSampleElement();
		if( obj == null)
			return null;
		code.Parent = DeserializePostElement(obj.getJSONObject("Parent"));
				
		JSONArray blockObjectsJson = obj.getJSONArray("Blocks");
		List<CodeBlock> blockObjects = new ArrayList<CodeBlock>();
		for( int i = 0; i < blockObjectsJson.length(); i++ )
		{
			blockObjects.add(DeserializeCodeBlock(blockObjectsJson.getJSONObject(i)));
		}
		
		code.Blocks = blockObjects;
		code.Votes = obj.getInt("Votes");
		return code;
	}


	public JSONObject SerializePostElement(PostElement post) throws JSONException
	{
		JSONObject obj = new JSONObject();
		obj.put("Id", post.Id);
		return obj;
	}
	
	public PostElement DeserializePostElement(JSONObject obj) throws JSONException
	{
		PostElement post = new PostElement();
		post.Id = obj.getInt("Id");
		return post;
	}

	
	public JSONObject SerializeCodeBlock(CodeBlock block) throws JSONException
	{
		JSONObject obj = new JSONObject();
		List<JSONObject> spanObjects = new ArrayList<JSONObject>();
		for( CodeSpan span : block.ClassMatches )
		{
			spanObjects.add(SerializeClassMatch(span));
		}
		
		obj.put("ClassMatches", spanObjects);
		obj.put("Body", block.getBody());
		return obj;
	}
	
	public CodeBlock DeserializeCodeBlock(JSONObject obj) throws JSONException
	{
		CodeBlock block = new CodeBlock();
		List<CodeSpan> spans = new ArrayList<CodeSpan>();
		
		JSONArray classMatchesJson = obj.getJSONArray("ClassMatches");
		for( int i = 0; i < classMatchesJson.length(); i++ )
		{
			spans.add(DeserializeClassMatch(classMatchesJson.getJSONObject(i)));
		}
		
		block.ClassMatches = spans;
		block.setBody(obj.getString("Body"));
		return block;
	}

	
	public JSONObject SerializeClassMatch(CodeSpan span) throws JSONException
	{
		JSONObject obj = new JSONObject();
		obj.put("Start", span.Start);
		obj.put("End", span.End);
		obj.put("KlassMatch", span.KlassMatch);
		obj.put("KlassFQN", span.KlassFQN);

		return obj;
	}
	
	public CodeSpan DeserializeClassMatch(JSONObject obj) throws JSONException
	{
		return new CodeSpan(obj.getInt("Start"), obj.getInt("End"), obj.getString("KlassMatch"), obj.getString("KlassFQN") );
	}

	
	public List<JSONObject> SerializeExternalResources(List<LinkElement_old> links ) throws JSONException
	{
		List<JSONObject> linkObjects = new ArrayList<JSONObject>();
		for( LinkElement_old l : links )
		{
			linkObjects.add(SerializeExternalResources(l));
		}
		return linkObjects;
	}
	
	public List<LinkElement_old> DeserializeExternalResources(JSONArray linkObjects ) throws JSONException
	{
		List<LinkElement_old> links  = new ArrayList<LinkElement_old>();
		for( int i = 0; i < linkObjects.length(); i++ )
		{
			links.add(DeserializeExternalResources(linkObjects.getJSONObject(i)));
		}
		return links;
	}

	
	public JSONObject SerializeExternalResources(LinkElement_old link) throws JSONException
	{
		JSONObject obj = new JSONObject();
		obj.put("Votes", link.Votes);
		obj.put("ExternalLinks", link.ExternalLinks);
		return obj;
	}
	
	public LinkElement_old DeserializeExternalResources(JSONObject obj) throws JSONException
	{
		LinkElement_old link = new LinkElement_old();
		link.Votes = obj.getInt("Votes");
		JSONArray extLinks = obj.getJSONArray("ExternalLinks");
		link.ExternalLinks = new ArrayList<String>();
		for( int i = 0; i < extLinks.length(); i++ )
		{
			link.ExternalLinks.add(extLinks.getString(i));
		}
		return link;
	}


	public List<JSONObject> SerializeClusteredLinks(List<ClusteredLinkElement> links ) throws JSONException
	{
		List<JSONObject> linkObjects = new ArrayList<JSONObject>();
		for( ClusteredLinkElement l : links )
		{
			linkObjects.add(SerializeClusteredLinkElement(l));
		}
		return linkObjects;
	}

	public List<ClusteredLinkElement> DeserializeClusteredLinks(JSONArray linkObjects) throws JSONException
	{
		List<ClusteredLinkElement> links = new ArrayList<ClusteredLinkElement>();
		for( int i = 0; i < linkObjects.length(); i++ )
		{
			links.add(DeserializeClusteredLinkElement(linkObjects.getJSONObject(i)));
		}
		return links;
	}
	
	public JSONObject SerializeClusteredLinkElement(ClusteredLinkElement link) throws JSONException
	{
		JSONObject obj = new JSONObject();
		obj.put("References", link.References);
		obj.put("TotalVotes", link.TotalVotes);
		obj.put("Href", link.Href);
		
		return obj;
	}
	
	public ClusteredLinkElement DeserializeClusteredLinkElement(JSONObject obj) throws JSONException
	{
		ClusteredLinkElement link = new ClusteredLinkElement();
		link.References = obj.getInt("References");
		link.TotalVotes = obj.getInt("TotalVotes");
		link.Href = obj.getString("Href");
		
		return link;
	}

	
	public List<JSONObject> SerializeThreadElements(Collection<ThreadElement> collection ) throws JSONException
	{
		List<JSONObject> threadObjects = new ArrayList<JSONObject>();
		for( ThreadElement t : collection )
		{
			threadObjects.add(SerializeThreadElement(t));
		}
		return threadObjects;
	}
	
	public List<ThreadElement> DeserializeThreadElements(JSONArray threadObjects ) throws JSONException
	{
		List<ThreadElement> threads = new ArrayList<ThreadElement>();
		for( int i = 0; i < threadObjects.length(); i++ )
		{
			threads.add(DeserializeThreadElement(threadObjects.getJSONObject(i)));
		}
		return threads;
	}

	
	public JSONObject SerializeThreadElement(ThreadElement thread) throws JSONException
	{
		JSONObject obj = new JSONObject();
		obj.put("Votes", thread.Votes);
		obj.put("FavoriteCount", thread.FavoriteCount);
		obj.put("ViewCount", thread.ViewCount);

		obj.put("CreationDate", thread.CreationDate.toString());

//		List<JSONObject> linkedAnswers = new ArrayList<JSONObject>();
//		for( AnswerElement ans : thread.LinkedAnswers )
//		{
//			linkedAnswers.add(SerializeAnswerElement(ans));
//		}
//		obj.put("LinkedAnswers", linkedAnswers);

		List<JSONObject> answers = new ArrayList<JSONObject>();
		for( AnswerElement ans : thread.Answers )
		{
			answers.add(SerializeAnswerElement(ans));
		}
		obj.put("Answers", answers);

		obj.put("Question", SerializeQuestionElement(thread.Question));
		
		return obj;
	}
	
	public ThreadElement DeserializeThreadElement(JSONObject obj) throws JSONException
	{
		ThreadElement thread = new ThreadElement();
		thread.Votes = obj.getInt("Votes");
		thread.FavoriteCount = obj.getInt("FavoriteCount");
		thread.ViewCount = obj.getInt("ViewCount");

		thread.CreationDate = getDate(obj,"CreationDate");

//		List<AnswerElement> linkedAnswers = new ArrayList<AnswerElement>();
//		JSONArray linked = obj.getJSONArray("LinkedAnswers");
//		for( int i=0 ; i < linked.length(); i++  )
//		{
//			linkedAnswers.add(DeserializeAnswerElement(linked.getJSONObject(i)));
//		}
//		
//		thread.LinkedAnswers = linkedAnswers;

		List<AnswerElement> answers = new ArrayList<AnswerElement>();
		JSONArray jsonAns = obj.getJSONArray("Answers");
		for( int i=0; i < jsonAns.length(); i++  )
		{
			answers.add(DeserializeAnswerElement(jsonAns.getJSONObject(i)));
		}
		
		thread.Answers = answers;
		thread.Question = DeserializeQuestionElement(obj.getJSONObject("Question"));

		thread.Question.Parent = thread;
		
		for( AnswerElement ans : thread.Answers )
		{
			ans.setIsAcceptedAnswer(ans.Id == thread.Question.AcceptedAnswerId);
			ans.Parent = thread;
		}

		//for( AnswerElement ans : thread.LinkedAnswers )
		//{
		//	ans.setIsAcceptedAnswer(ans.Id == thread.Question.AcceptedAnswerId);
		//}

		
		return thread;
	}

	
	public JSONObject SerializeAnswerElement(AnswerElement answer) throws JSONException
	{
		JSONObject obj = new JSONObject();
		obj.put("id", answer.Id);
		obj.put("Date", answer.Date.toString());
		obj.put("Votes", answer.Votes);
		obj.put("UserId", answer.UserId);
		obj.put("BountyAmount", answer.BountyAmount);
		
		//obj.put("CodeSample", SerializeCodeSampleElement( answer.CodeSample ));
		//obj.put("Hrefs",SerializeHrefs(answer.Hrefs));
		//obj.put("Links",SerializeLinkSpans(answer.Links));

		return obj;
	}
	
	public AnswerElement DeserializeAnswerElement(JSONObject obj) throws JSONException
	{
		AnswerElement answer = new AnswerElement();
		answer.Id = obj.getInt("id");
		answer.Date = getDate(obj,"Date");
		answer.Votes = obj.getInt("Votes");
		answer.UserId = obj.getInt("UserId");
//		try
//		{
//			answer.CodeSample = DeserializeCodeSampleElement( obj.getJSONObject("CodeSample"));
//			answer.CodeSample.Parent = answer;
//		}
//		catch(JSONException ex)
//		{
//			answer.CodeSample = null;
//		}
		
		try
		{
			answer.BountyAmount = obj.getInt("BountyAmount");
		}
		catch(JSONException ex)
		{
			answer.BountyAmount = null;
		}
		
		//answer.Hrefs = DeserializeHrefs(obj.getJSONArray("Hrefs"));
		//answer.Links = DeserializeLinkSpans(obj.getJSONArray("Links"));

		return answer;
	}
	
	private Date getDate(JSONObject obj, String field)
	{
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		try 
		{
			return format.parse(obj.getString(field));
		} 
		catch (ParseException e) 
		{
			e.printStackTrace();
			return null;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	
	public JSONObject SerializeQuestionElement(QuestionElement q) throws JSONException
	{
		JSONObject obj = new JSONObject();
		obj.put("id", q.Id);
		obj.put("IsLinked", q.IsLinked);
		obj.put("Title", q.Title);
		obj.put("UserId", q.UserId);
		obj.put("AcceptedAnswerId", q.AcceptedAnswerId);
		obj.put("HasAcceptedAnswer", q.HasAcceptedAnswer);
		
		//obj.put("CodeSample", SerializeCodeSampleElement( q.CodeSample ));		
		//obj.put("Hrefs",SerializeHrefs(q.Hrefs));
		//obj.put("Links",SerializeLinkSpans(q.Links));
		
		return obj;
	}
	
	public QuestionElement DeserializeQuestionElement(JSONObject obj) throws JSONException
	{
		QuestionElement q = new QuestionElement();
		q.Id = obj.getInt("id");
		q.IsLinked = obj.getBoolean("IsLinked");
		q.Title = obj.getString("Title");
		q.UserId = obj.getInt("UserId");

//		try
//		{
//			q.CodeSample = DeserializeCodeSampleElement( obj.getJSONObject("CodeSample"));
//			q.CodeSample.Parent = q;
//		}
//		catch( JSONException ex)
//		{
//			q.CodeSample = null;
//		}
		//q.Hrefs = DeserializeHrefs(obj.getJSONArray("Hrefs"));
		//q.Links = DeserializeLinkSpans(obj.getJSONArray("Links"));
		
		q.HasAcceptedAnswer = obj.getBoolean("HasAcceptedAnswer");
		if( q.HasAcceptedAnswer )
		{
			q.AcceptedAnswerId = obj.getInt("AcceptedAnswerId");
		}
		return q;
	}
	
//	public List<JSONObject> SerializeHrefs(List<LinkElement> hrefs) throws JSONException
//	{
//		List<JSONObject> hrefsObjects = new ArrayList<JSONObject>();
//		for( LinkElement href : hrefs )
//		{
//			hrefsObjects.add(SerializeHref(href));
//		}
//		return hrefsObjects;
//	}
//	
//	public List<LinkElement> DeserializeHrefs(JSONArray objects) throws JSONException
//	{
//		List<LinkElement> hrefs = new ArrayList<LinkElement>();
//		for( int i = 0; i < objects.length(); i++ )
//		{
//			hrefs.add(DeserializeHref(objects.getJSONObject(i)));
//		}
//		return hrefs;
//	}
	
	public JSONObject SerializeHref(LinkElement href, ThreadElement t, PostElement p) throws JSONException
	{
		JSONObject obj = new JSONObject();
		obj.put("AnchorText", href.AnchorText);
		obj.put("Href", href.Href);
		
		// Stuff only in serialized version:
		obj.put("Thread", t.Question.Id);
		obj.put("Post", p.Id);
		return obj;
	}

	public LinkElement DeserializeHref(JSONObject obj) throws JSONException
	{
		LinkElement href = new LinkElement();
		href.AnchorText = obj.getString("AnchorText");
		href.Href = obj.getString("Href");
		
		return href;
	}

//	public List<JSONObject> SerializeLinkSpans(List<LinkSpan> links) throws JSONException
//	{
//		List<JSONObject> linkObjects = new ArrayList<JSONObject>();
//		for( LinkSpan link : links )
//		{
//			linkObjects.add(SerializeLinkSpan(link,null,null));
//		}
//		return linkObjects;
//	}
//	
//	public List<LinkSpan> DeserializeLinkSpans(JSONArray objects) throws JSONException
//	{
//		List<LinkSpan> links = new ArrayList<LinkSpan>();
//		for( int i = 0; i < objects.length(); i++ )
//		{
//			links.add(DeserializeLinkSpan(objects.getJSONObject(i)));
//		}
//		return links;
//	}
	
	public JSONObject SerializeLinkSpan(LinkSpan span, ThreadElement t, PostElement p) throws JSONException
	{
		JSONObject obj = new JSONObject();
		obj.put("Start", span.Start);
		obj.put("End", span.End);
		//obj.put("Site", span.Site);
		obj.put("SiteIndex", span.SiteIndex);
		obj.put("PostId", span.PostId);
		obj.put("Validated", span.Validated);
		obj.put("LinkType", span.LinkType);

		// Stuff only in serialized version:
		obj.put("Thread", t.Question.Id);
		obj.put("Post", p.Id);
		return obj;
	}
	
	public LinkSpan DeserializeLinkSpan(JSONObject obj) throws JSONException
	{
		LinkSpan span = new LinkSpan();
		span.Start = obj.getInt("Start");
		span.End = obj.getInt("End");
		try
		{
			span.Site = obj.getString("Site");
		}
		catch(JSONException ex)
		{
			span.Site = "";
		}
		span.SiteIndex = obj.getInt("SiteIndex");
		span.PostId = obj.getInt("PostId");
		span.Validated = obj.getBoolean("Validated");
		span.LinkType = transduceLinkType(obj.getString("LinkType"));
		return span;
	}
	
	public LinkType transduceLinkType(String type )
	{
		if( type.equals("CodeMarkupLink"))
			return LinkType.CodeMarkupLink;
		if( type.equals("CodeSampleLink"))
			return LinkType.CodeSampleLink;
		if( type.equals("HrefLink"))
			return LinkType.HrefLink;
		if( type.equals("WordLink"))
			return LinkType.WordLink;
		return null;
	}

	public Hashtable<Integer, CodeSampleElement> DeserializeCodeSampleElementsPool( String path) throws JSONException
	{
		Hashtable<Integer, CodeSampleElement> sampleMap = new Hashtable<Integer, CodeSampleElement>();
		try 
		{
			File outputDir = new File(path + File.separator + "samplepool");
			
			for( String filePath : outputDir.list())
			{
				if( !filePath.endsWith(".json") )
				{
					continue;
				}

				String fullPath = outputDir.getAbsolutePath() + File.separator + filePath;
				
				JSONObject obj = new JSONObject(readStream(new FileInputStream(fullPath)));
				CodeSampleElement s = DeserializeCodeSampleElement(obj);
				sampleMap.put(s.Parent.Id, s);
			}
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		}
		
		return sampleMap;
	}
	
	public Hashtable<Integer, ThreadElement> DeserializeThreadElementsPool(String path) throws JSONException 
	{
		Hashtable<Integer, ThreadElement> threadMap = new Hashtable<Integer, ThreadElement>();
		try 
		{
			File outputDir = new File(path + File.separator + "threadpool");
			
			for( String filePath : outputDir.list())
			{
				if( !filePath.endsWith(".json") )
				{
					continue;
				}
				String fullPath = outputDir.getAbsolutePath() + File.separator + filePath;
				
				JSONObject obj = new JSONObject(readStream(new FileInputStream(fullPath)));
				ThreadElement t = DeserializeThreadElement(obj);
				threadMap.put(t.Question.Id, t);
			}
			return threadMap;
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		}
		
		return threadMap;
	}

	public void SerializeCodeSampleElementsPool(Hashtable<Integer, CodeSampleElement> codeSamples, String outputPath)
	{
		File outputDir = new File(outputPath + File.separator + "samplepool");
		if( !outputDir.exists() )
		{
			outputDir.mkdirs();
		}

		try
		{
			for( CodeSampleElement sample : codeSamples.values() )
			{
				System.out.println("Wrote sample: " + sample.Parent.Id);
				File outputFile = new File(outputDir.getAbsolutePath() + File.separator + "sample" + sample.Parent.Id + ".json");
				JSONObject obj = SerializeCodeSampleElement(sample);
				
				FileWriter writer = new FileWriter(outputFile);
				writer.append(obj.toString());
				writer.close();
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public void SerializeThreadElementsPool(Hashtable<Integer, ThreadElement> threadMap, String outputPath) 
	{
		File outputDir = new File(outputPath + File.separator + "threadpool");
		if( !outputDir.exists() )
		{
			outputDir.mkdirs();
		}

		try
		{
			for( ThreadElement thread : threadMap.values() )
			{
				System.out.println("Wrote thread: " + thread.Question.Id);
				File outputFile = new File(outputDir.getAbsolutePath() + File.separator + "thread" + thread.Question.Id + ".json");
				JSONObject obj = SerializeThreadElement(thread);
				
				FileWriter writer = new FileWriter(outputFile);
				writer.append(obj.toString());
				writer.close();
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
}