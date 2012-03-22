package anacrowd.paperanalysis;

import anacrowd.api.AbstractApi;
import anacrowd.api.AndroidApi;
import anacrowd.api.Collision;
import anacrowd.api.JavaApi;
import anacrowd.crowd.CrowdAnalysis;
import anacrowd.crowd.ViewCountAnalysis;
import anacrowd.export.ApiInfo;
import anacrowd.export.CoverageData;
import anacrowd.export.ExportCoverage;
import anacrowd.export.ExportPosts;
import anacrowd.export.ExportUsers;
import anacrowd.export.Post2Api;
import anacrowd.formatters.ConsoleFormatter;
import anacrowd.metrics.coverage.Visitor;
import anacrowd.metrics.coverage.WordDistributions;
import anacrowd.sampling.SampleByWordNumber;

public class Driver 
{
	public static void main (String [] args) 
	{
		//AbstractApi api = new AndroidApi();
		//api.Parse("C:/Users/cp125/Desktop/repo/blogs/tools/stackoverflow/QuestionSnab/api_current.xml", api);

		//CrowdAnalysis crowd = new CrowdAnalysis();
		//crowd.SizeOfCrowd("android");
		//crowd.SpeedOfCrowd(api);
		//ViewCountAnalysis viewCount = new ViewCountAnalysis();
		//viewCount.ViewCountAndCrowdSize(api);
		
		//ApiCoverage("C:/Users/cp125/Desktop/repo/blogs/tools/stackoverflow/QuestionSnab/api_current.xml", new AndroidApi());
		//ApiCoverage("C:/schoolwork/activity/blogs/tools/stackoverflow/QuestionSnab/api_current.xml", new AndroidApi());
		
		//ExportUsers users = new ExportUsers();
		//users.DumpInfo("users.csv");
		
		//ExportPosts posts = new ExportPosts();
		//posts.DumpInfo("questions.csv","answers.csv", "android");

		ApiCoverage("/Users/gameweld/Documents/blogs/tools/stackoverflow/QuestionSnab/api_current.xml", new AndroidApi());
		
		//ApiCoverage("C:/Users/cp125/Desktop/repo/blogs/tools/stackoverflow/data/java.xml", new SwingApi());
		//ApiCoverage("C:/schoolwork/activity/blogs/tools/stackoverflow/data/java.xml", new SwingApi());
	}

	private static void ApiCoverage(String path, AbstractApi api) 
	{
		api.Parse(path);

		//new Collision().ReportCollision(api);
		
		//ApiInfo info = new ApiInfo();
		//info.DumpInfo(api, "android_api.csv");

		Visitor coverage = new Visitor();
		coverage.ClassQuestionsAnswers(api);
		//coverage.MethodQuestionsAnswers(api);

		
		//SampleByWordNumber sample = new SampleByWordNumber();
		//sample.SampleMatches(api, .1, 10);
		
		//WordDistributions dist = new WordDistributions();
		//dist.Distribution(api);
		
		
		//coverage.ClassCoveragePercentage(api);
		//coverage.MethodCoveragePercentage(api);
		
		//Post2Api data = new Post2Api();
		//data.DumpAPIUsageData(api, "post2api.csv");

		//new ExportCoverage().DumpCoverage(api, "androidcoverage.csv");
		ConsoleFormatter format = new ConsoleFormatter();
		format.FormatSummary(api);
	}
}
