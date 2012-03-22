package anacrowd.paperanalysis.PrepareData;

import java.util.List;

import anacrowd.api.AbstractApi;
import anacrowd.documentation.ClassDocumentation;
import anacrowd.documentation.generator.LinkDocumentation;
import anacrowd.documentation.generator.SerializeDocumentation;
import anacrowd.paperanalysis.BaseAnalysis;

public class SerializeJson extends BaseAnalysis
{
	@Override
	public void Run()
	{
		
		LinkDocumentation linker = new LinkDocumentation();
		//List<ClassDocumentation> docs = linker.LinkAndSerializeApi(GWT, "classdoc/gwt");
		//List<ClassDocumentation> docs = linker.LinkAndSerializeApi(Android, "classdoc/android");
		List<ClassDocumentation> docs = linker.LinkAndSerializeApi(Java);
		
		int coverage1 = 0; 
		for( ClassDocumentation doc : docs )
		{
			if( doc.getValidatedThreads().size() > 0 )
				coverage1++;
		}
		System.out.println(coverage1);
				
		//TestDeserialize("classdoc/android",Android);
	}
	
	public void TestDeserialize(AbstractApi api)
	{
		int coverage1 = 0;
		List<ClassDocumentation> docs = DeserializeModel(api);
		for( ClassDocumentation doc : docs )
		{
			System.out.print(doc.Klass.getFQN() + ",");
			System.out.print(doc.getCodeSampleElements().size() + ",");
			System.out.print(doc.getValidatedLinks().size() + ",");
			System.out.print(doc.getValidatedThreads().size());
			System.out.println();
			if( doc.getValidatedThreads().size() > 0 )
				coverage1++;
		}
		System.out.println(coverage1);
	}
	
	public static void main(String[] args)
	{
		new SerializeJson().Run();
	}
}
