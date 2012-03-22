package anacrowd.api;

import anacrowd.api.parser.ApiParser;

public class JavaApi extends AbstractApi
{
	public JavaApi()
	{
		MainTag = "<java>";
		Parser = new ApiParser();
		Parser.PackagePrefix = "java";
		ClassDocPath = "classdoc/java";
	}
}