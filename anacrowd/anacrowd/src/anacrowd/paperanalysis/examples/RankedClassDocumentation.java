package anacrowd.paperanalysis.examples;

import java.util.List;

import anacrowd.documentation.ClassDocumentation;
import anacrowd.documentation.generator.ExportDocumentation;
import anacrowd.documentation.generator.RankDocumentation;
import anacrowd.paperanalysis.BaseAnalysis;

public class RankedClassDocumentation extends BaseAnalysis
{
	@Override
	public void Run()
	{
		List<ClassDocumentation> docs = DeserializeModel(Java);

		RankDocumentation ranker = new RankDocumentation();
		ExportDocumentation exporter = new ExportDocumentation();
		for( ClassDocumentation doc: docs )
		{
			if( doc.Klass.Name.equals("MessageDigest") )
			{
				ranker.Rank(doc);
				exporter.Export(doc);				
				break;
			}
		}
	}
	
	public static void main(String args[])
	{
		new RankedClassDocumentation().Run();
	}
}
