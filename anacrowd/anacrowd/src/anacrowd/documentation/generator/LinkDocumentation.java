package anacrowd.documentation.generator;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import anacrowd.api.AbstractApi;
import anacrowd.api.AndroidApi;
import anacrowd.api.GWTApi;
import anacrowd.api.JavaApi;
import anacrowd.api.elements.ClassElem;
import anacrowd.api.elements.PackageElem;
import anacrowd.documentation.ClassDocumentation;
import anacrowd.documentation.ClassIndex;
import anacrowd.documentation.elements.AnswerElement;
import anacrowd.documentation.elements.CodeSampleElement;
import anacrowd.documentation.elements.CodeSampleElement.CodeBlock;
import anacrowd.documentation.elements.CodeSampleElement.CodeSpan;
import anacrowd.documentation.elements.LinkElement;
import anacrowd.documentation.elements.LinkElement_old;
import anacrowd.documentation.elements.PostElement;
import anacrowd.documentation.elements.QuestionElement;
import anacrowd.documentation.elements.ThreadElement;
import anacrowd.documentation.links.CachePostBodyIndex;
import anacrowd.documentation.links.CachedPostBody;
import anacrowd.documentation.links.LinkSpan;
import anacrowd.documentation.links.LinkSpan.LinkType;
import anacrowd.documentation.links.LinkValidation;
import anacrowd.metrics.coverage.Visitor;
import anacrowd.metrics.coverage.WordDistributions;
import anacrowd.stackdb.DBInfo;
import anacrowd.stackdb.Search;
import anacrowd.stackdb.DBInfo.PostInfo;


public class LinkDocumentation 
{
	Search search = new Search();
	DBInfo db = new DBInfo();
	
	public ClassDocumentation Link(ClassElem klass, Hashtable<Integer,ThreadElement> ThreadMap)
	{
		ClassDocumentation klassDoc = new ClassDocumentation();
		WordDistributions wd = new WordDistributions();
		List<Integer> wordQMatches = new ArrayList<Integer>();
		List<Integer> wordAMatches = new ArrayList<Integer>();

		// Fully qualified name for ambiguous class names (often very few).
		String klassName = klass.Name;
		if( klass.HasClassNameCollision )
		{
			klassName = klass.ParentPackage.Name + "." + klass.Name;
		}
		
		// Questions
		if( wd.WordLength(klassName) > 1 )
		{
			wordQMatches = search.GetIdsViaWordMatches(this.QuestionsTempTableName, klassName);
		}
		List<Integer> codeQMatches = search.GetIdsViaCodeMatches(this.QuestionsTempTableName, klassName);
		List<Integer> linkQMatches = search.GetIdsViaLinkMatches(this.QuestionsTempTableName, klassName);

		// Answers
		if( wd.WordLength(klass.Name) > 1)
		{
			wordAMatches = search.GetIdsViaWordMatches(this.AnswersTempTableName, klassName);
		}
		List<Integer> codeAMatches = search.GetIdsViaCodeMatches(this.AnswersTempTableName, klassName);
		List<Integer> linkAMatches = search.GetIdsViaLinkMatches(this.AnswersTempTableName, klassName);

		// Parent questions of matched answers
		HashSet<Integer> ansSet = new HashSet<Integer>();
		ansSet.addAll(wordAMatches);
		ansSet.addAll(codeAMatches);
		ansSet.addAll(linkAMatches);

		Hashtable<Integer,Integer> AnswerIdToParentId = new Hashtable<Integer,Integer>();
		for( int answerId: ansSet )
		{
			AnswerIdToParentId.put(answerId, search.GetParentId(this.AnswersTempTableName, answerId));
		}
				
		// Intersect questions		
		HashSet<Integer> qSet = new HashSet<Integer>();
		qSet.addAll(wordQMatches);
		qSet.addAll(codeQMatches);
		qSet.addAll(linkQMatches);

		BuildModel(klass, klassDoc, codeQMatches, codeAMatches, linkAMatches,
				ansSet, AnswerIdToParentId, qSet, ThreadMap);
		
		return klassDoc;
	}

	private void BuildModel(ClassElem klass, ClassDocumentation klassDoc,
			List<Integer> codeQMatches, List<Integer> codeAMatches,
			List<Integer> linkAMatches, HashSet<Integer> ansSet,
			Hashtable<Integer, Integer> AnswerIdToParentId,
			HashSet<Integer> qSet,
			Hashtable<Integer,ThreadElement> ThreadMap // Shared across all ClassDocs
			) 
	{
		List<Integer> myThreads = new ArrayList<Integer>();
		
		// Build threads
		for( Integer id : qSet )
		{
			ThreadElement thread = new ThreadElement();
			thread.Question = new QuestionElement();
			thread.Question.Id = id;
			thread.Question.IsLinked = true;
	
			// Thread detail
			PostInfo info = db.GetPostDetail(thread.Question.Id);
			thread.Question.Title = info.Title;
			thread.Question.UserId = info.OwnerUserId;
			thread.Question.Body = info.getBody();
			thread.Question.Parent = thread;
			
			thread.Votes = info.Score;
			thread.FavoriteCount = info.FavoriteCount;
			thread.ViewCount = info.ViewCount;
			thread.CreationDate = info.CreationDate;

			if( info.AcceptedAnswerId != null )
			{
				thread.Question.HasAcceptedAnswer = true;
				thread.Question.AcceptedAnswerId = info.AcceptedAnswerId;
			}

			// Validate Links
			LinkValidation validate = new LinkValidation();
			List<LinkElement> hrefs = new ArrayList<LinkElement>();
			List<LinkSpan> spans = validate.Validate(klass, thread.Question, thread.Question.Title, hrefs);
			thread.Question.setHrefs( hrefs, klass );
			thread.Question.setLinks( spans, klass );
			
			// Side effect
			CodeSampleElement sample = GetOrBuildCodeSample(klass, thread.Question, spans);
			sample.Votes = thread.Votes;
			
			ThreadMap.put(id, thread);
			myThreads.add(id);
		}
		
		for( Integer answerId : ansSet )
		{
			int parentQ = AnswerIdToParentId.get( answerId );
			if( !ThreadMap.containsKey(parentQ) )
			{
				// Non-linked Q, add thread
				ThreadElement thread = new ThreadElement();
				thread.Question = new QuestionElement();
				thread.Question.Id = parentQ;
				thread.Question.IsLinked = false;
				//thread.Question.Links = new ArrayList<LinkSpan>();
				//thread.Question.Hrefs = new ArrayList<LinkElement>();
				
				// Detail
				PostInfo tinfo = db.GetPostDetail(parentQ);
				thread.Question.Title = tinfo.Title;
				thread.Question.UserId = tinfo.OwnerUserId;
				thread.Question.Body = tinfo.getBody();
				thread.Question.Parent = thread;
				
				thread.Votes = tinfo.Score;
				thread.FavoriteCount = tinfo.FavoriteCount;
				thread.ViewCount = tinfo.ViewCount;
				thread.CreationDate = tinfo.CreationDate;

				if( tinfo.AcceptedAnswerId != null )
				{
					thread.Question.HasAcceptedAnswer = true;
					thread.Question.AcceptedAnswerId = tinfo.AcceptedAnswerId;
				}
				
				ThreadMap.put(parentQ, thread);
				myThreads.add(parentQ);
			}
		}
		
		klassDoc.Klass = klass;
		//klassDoc.ThreadElements.addAll(ThreadMap.values());
		for(Integer id : myThreads)
		{
			klassDoc.ThreadElements.add(ThreadMap.get(id));
		}

		// All Answers
		for( ThreadElement thread : klassDoc.ThreadElements )
		{
			for( PostInfo info : db.Answers(thread.Question.Id) )
			{
				AnswerElement ans = new AnswerElement();
				ans.Id = info.Id;

				// Thread detail
				ans.Votes = info.Score;
				ans.Date = info.CreationDate;
				ans.Body = info.getBody();
				ans.UserId = info.OwnerUserId;
				ans.BountyAmount = db.GetBountyAmount(ans.Id);
				ans.Parent = thread;
				
				if( ansSet.contains(info.Id) )
				{
					// Validate Links
					LinkValidation validate = new LinkValidation();
					List<LinkElement> hrefs = new ArrayList<LinkElement>();
					List<LinkSpan> spans = validate.Validate(klass, ans,null, hrefs);
					ans.setHrefs( hrefs, klass);
					ans.setLinks( spans, klass );
					//ans.setCodeSample(BuildCodeSample(spans,ans), klass );

					// Side effect
					CodeSampleElement sample = GetOrBuildCodeSample(klass, ans, spans);
					sample.Votes = ans.Votes;
				}
				thread.Answers.add(ans);
			}
		}
	}
	
	private boolean HasValidatedCodeLink(List<LinkSpan> spans)
	{
		for( LinkSpan span : spans )
		{
			if( span.LinkType == LinkType.CodeSampleLink && span.Validated)
			{
				return true;
			}
		}
		return false;
	}
	
	public CodeSampleElement GetOrBuildCodeSample(ClassElem klass, PostElement parent, List<LinkSpan> spans)
	{
		CodeSampleElement codeSample = null;

		if( !ClassIndex.getInstance().SampleMap.containsKey(parent.Id) )
		{
			codeSample = new CodeSampleElement();
			codeSample.Parent = parent;
			
			Hashtable<Integer,CodeBlock> blocks = new Hashtable<Integer,CodeBlock>();
			CachedPostBody postBody = CachePostBodyIndex.getInstance().ParseOrGet(parent);

			for( String blockBody : postBody.CodeBlocks )
			{
				CodeBlock block = new CodeBlock();
				block.setBody( blockBody );
				codeSample.Blocks.add(block);
			}
			ClassIndex.getInstance().SampleMap.put(parent.Id, codeSample);
		}
		codeSample = ClassIndex.getInstance().SampleMap.get(parent.Id);
		
		int b = 0;
		for( CodeBlock block : codeSample.Blocks )
		{
			for( LinkSpan span : spans )
			{
				if( span.Validated && span.LinkType == LinkType.CodeSampleLink && span.SiteIndex == b)
				{
					block.ClassMatches.add(new CodeSpan(span.Start,span.End,klass.Name,klass.getFQN()));
				}
			}
			b++;
		}
		
		return ClassIndex.getInstance().SampleMap.get(parent.Id);
	}
	

	public String QuestionsTempTableName;
	public String AnswersTempTableName;
		
	public void PrepareApiForSearch(Search info, AbstractApi api)
	{
		String qTable = api.MainTag + "questions_table";
		String aTable = api.MainTag + "answers_table";
		info.CreateTemporaryTable(api.MainTag, qTable);
		info.CreateTemporaryRelatedTable(api.MainTag, aTable, qTable);
		
		this.AnswersTempTableName = aTable;
		this.QuestionsTempTableName = qTable;
	}

	public ClassDocumentation LinkOnlyClass(AbstractApi api, String klass)
	{
		search.Init();
		PrepareApiForSearch(search,api);
		
		ClassElem k = new ClassElem();
		k.Name = klass;
		
		ClassDocumentation doc = Link(k, new Hashtable<Integer,ThreadElement>());
		
		search.Close();
		return doc;
	}

	public List<ClassDocumentation> LinkApi(AbstractApi api)
	{
		search.Init();
		db.Init();

		PrepareApiForSearch(search,api);
		List<ClassDocumentation>  list = new ArrayList<ClassDocumentation>();
		Hashtable<Integer,ThreadElement> ThreadMap = new Hashtable<Integer,ThreadElement>();

		for( PackageElem pack : api.Packages )
		{
			for( ClassElem klass : pack.Classes )
			{
				ClassDocumentation doc = Link(klass, ThreadMap);
				list.add(doc);
			}
		}
		search.Close();
		db.Close();
		return list;
	}

	/// Use if could not complete had to terminate LinkAndSerialize due to memory limitations.
	/// This will deserialize classes and then build thread pool.
	public List<ClassDocumentation> PatchThreadPool(AbstractApi api, String path)
	{
		SerializeDocumentation serializer = new SerializeDocumentation();
		List<ClassDocumentation>  list = new ArrayList<ClassDocumentation>();
		Hashtable<Integer,ThreadElement> ThreadMap = new Hashtable<Integer,ThreadElement>();

		search.Init();
		db.Init();

		System.out.println("Preparing temporary SQL tables");
		PrepareApiForSearch(search,api);
		
		for( PackageElem pack : api.Packages )
		{
			for( ClassElem klass : pack.Classes )
			{
				System.out.println("Linking," +klass.Name + " ");
				ClassDocumentation doc = Link(klass,ThreadMap);

				for( ThreadElement t : doc.ThreadElements )
				{
					ThreadMap.put(t.Question.Id, t);
				}
				
				doc = null;
			}
		}
		
		// Thread Pool
		System.out.println("Serializing thread pool");
		serializer.SerializeThreadElementsPool(ThreadMap, path);
		
		search.Close();
		db.Close();
		return list;
	}
	
	public List<ClassDocumentation> LinkAndSerializeApi(AbstractApi api)
	{
		String path = api.ClassDocPath;
		SerializeDocumentation serializer = new SerializeDocumentation();
		List<ClassDocumentation>  list = new ArrayList<ClassDocumentation>();

		search.Init();
		db.Init();

		System.out.println("Preparing temporary SQL tables");
		PrepareApiForSearch(search,api);
		
		for( PackageElem pack : api.Packages )
		{
			for( ClassElem klass : pack.Classes )
			{
				File outputFile = new File(path + java.io.File.separator + klass.ParentPackage.Name + "." + klass.Name + ".json");
				//if( outputFile.exists())
				//	continue;
				
				System.out.print("Linking," +klass.Name + " ");
				ClassDocumentation doc = Link(klass, ClassIndex.getInstance().ThreadMap);

				// Gather threads used by doc
				//for( ThreadElement t : doc.ThreadElements )
				//{
				//	ClassIndex.getInstance().ThreadMap.put(t.Question.Id, t);
				//}

				list.add(doc);				
				System.out.println(doc.getCodeSampleElements().size()+","+
						doc.getValidatedThreads().size() +","+
						doc.ThreadElements.size() +","+
						doc.getValidatedLinks().size()
						);

				// Serialize
				System.out.print("Serializing," + klass.Name + ",");
				serializer.SerializeClassDoc(path,doc,outputFile);
				System.out.println("done");
			}
		}
		
		// Thread Pool
		System.out.println("Serializing thread pool");
		serializer.SerializeThreadElementsPool(ClassIndex.getInstance().ThreadMap, path);

		// Code Sample Pool
		System.out.println("Serializing code example pool");
		serializer.SerializeCodeSampleElementsPool(ClassIndex.getInstance().SampleMap, path);
		
		search.Close();
		db.Close();
		return list;
	}
}