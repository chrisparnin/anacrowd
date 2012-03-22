package anacrowd.paperanalysis;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import anacrowd.api.AbstractApi;
import anacrowd.api.elements.ClassElem;
import anacrowd.api.elements.PackageElem;
import anacrowd.documentation.ClassDocumentation;
import anacrowd.documentation.elements.CodeSampleElement;
import anacrowd.documentation.generator.BuildDocumentation;

public class AnalyzeDocumentation extends BaseAnalysis 
{
	@Override
	public void Run()
	{
		//super.Run();
		AbstractApi api = Android;
		BuildDocumentation builder = new BuildDocumentation();
		List<ClassDocumentation> list = builder.PrepareApi(api);
		List<ClassDocumentation> subList = list.subList(700, list.size()-1);
		for( ClassDocumentation doc : subList )
		{
			System.out.print(doc.Klass.Name + ",");
			builder.Build(doc,true, true);
			System.out.println( doc.getCodeSampleElements().size() + "," + doc.ClusteredLinks.size() + "," + doc.ThreadElements.size() );
			
			// Clear out details in docs...to manage memory...
			//doc.CodeSampleElements = null;
			doc.ThreadElements = null;
			//doc.ExternalResources = null;
			doc.ClusteredLinks = null;
		}
	}
	
	public static void main(String[] args) 
	{
		BaseAnalysis run = new AnalyzeDocumentation();
		run.Run();
	}

}
