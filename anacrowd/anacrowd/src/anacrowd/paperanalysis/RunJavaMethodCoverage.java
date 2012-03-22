package anacrowd.paperanalysis;

import anacrowd.api.AbstractApi;
import anacrowd.api.AndroidApi;
import anacrowd.api.JavaApi;
import anacrowd.crowd.CrowdAnalysis;
import anacrowd.crowd.ViewCountAnalysis;
import anacrowd.export.ApiInfo;
import anacrowd.export.CoverageData;
import anacrowd.export.ExportPosts;
import anacrowd.export.ExportUsers;
import anacrowd.export.Post2Api;
import anacrowd.formatters.ConsoleFormatter;
import anacrowd.metrics.coverage.Visitor;
import anacrowd.metrics.coverage.WordDistributions;
import anacrowd.sampling.SampleByWordNumber;

public class RunJavaMethodCoverage 
{
	public static void main (String [] args) 
	{
		ApiCoverage("C:/Users/cp125/Desktop/repo/blogs/tools/stackoverflow/data/java.xml", new JavaApi());
	}

	private static void ApiCoverage(String path, AbstractApi api) 
	{
		api.Parse(path);

		Visitor coverage = new Visitor();
		coverage.MethodQuestionsAnswers(api,false);

		ConsoleFormatter format = new ConsoleFormatter();
		format.FormatSummary(api);
	}
}
