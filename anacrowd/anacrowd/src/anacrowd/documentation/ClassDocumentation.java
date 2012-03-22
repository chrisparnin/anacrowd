package anacrowd.documentation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import anacrowd.api.elements.ClassElem;
import anacrowd.documentation.elements.AnswerElement;
import anacrowd.documentation.elements.ClusteredLinkElement;
import anacrowd.documentation.elements.CodeSampleElement;
import anacrowd.documentation.elements.CodeSampleElement.CodeBlock;
import anacrowd.documentation.elements.CodeSampleElement.CodeSpan;
import anacrowd.documentation.elements.LinkElement;
import anacrowd.documentation.elements.LinkElement_old;
import anacrowd.documentation.elements.ThreadElement;
import anacrowd.documentation.links.LinkSpan;
import anacrowd.stackdb.DBInfo;

public class ClassDocumentation
{
	public ClassElem Klass;

	//public List<CodeSampleElement> CodeSampleElements = new ArrayList<CodeSampleElement>();
	public List<LinkElement> LinkElements = new ArrayList<LinkElement>();
	public List<ClusteredLinkElement> ClusteredLinks = new ArrayList<ClusteredLinkElement>();

	public List<ThreadElement> ThreadElements = new ArrayList<ThreadElement>();

	public List<LinkElement> getHrefs()
	{
		List<LinkElement> hrefs = new ArrayList<LinkElement>();

		for( ThreadElement thread : this.getValidatedThreads() )
		{
			List<LinkElement> links = thread.Question.getHrefs(this.Klass);
			hrefs.addAll(links);
			for( AnswerElement ans : thread.Answers )
			{
				hrefs.addAll( ans.getHrefs(this.Klass));
			}
		}

		return hrefs;
	}

	
	
	public List<CodeSampleElement> getCodeSampleElements()
	{
		List<CodeSampleElement> samples = new ArrayList<CodeSampleElement>();

		for( ThreadElement thread : this.ThreadElements )
		{
			CodeSampleElement elem = thread.Question.getCodeSample();
			if( elem != null && isCodeSampleLinked(elem))
			{
				samples.add(elem);
			}
			for( AnswerElement ans : thread.Answers )
			{
				elem = ans.getCodeSample();
				if( elem != null && isCodeSampleLinked(elem))
				{
					samples.add(elem);
				}
			}
		}

		return samples;
	}
	
	public boolean isCodeSampleLinked(CodeSampleElement elem)
	{
		for( CodeBlock block : elem.Blocks )
		{
			for( CodeSpan span : block.ClassMatches )
			{
				if( span.KlassFQN.equals(this.Klass.getFQN()))
					return true;
			}
		}
		return false;
	}
	
	public List<LinkSpan> getValidatedLinks()
	{
		List<LinkSpan> validatedLinks = new ArrayList<LinkSpan>();
		
		for( ThreadElement thread : this.ThreadElements )
		{
			for( LinkSpan link : thread.Question.getLinks(Klass) )
			{
				if( link.Validated )
				{
					validatedLinks.add(link);
				}
			}
			for( AnswerElement ans : thread.Answers )
			{
				for( LinkSpan link : ans.getLinks(Klass) )
				{
					if( link.Validated )
					{
						validatedLinks.add(link);
					}
				}
			}
		}
		return validatedLinks;
	}

	
	public List<ThreadElement> getValidatedThreads()
	{
		List<ThreadElement> validatedThreads = new ArrayList<ThreadElement>();
		for( ThreadElement thread : this.ThreadElements )
		{
			if( thread.Question.IsLinked && thread.Question.IsValidated(Klass) )
			{
				validatedThreads.add(thread);
				continue;
			}
			for( AnswerElement ans : thread.Answers )
			{
				if( ans.IsValidated(Klass) )
				{
					validatedThreads.add(thread);
					break;
				}
			}
		}
		return validatedThreads;
	}
}

// tool: crowd doc
// tool: treemap of doc (doc vis)