package anacrowd.api;

import anacrowd.api.parser.ApiParser;

public class AndroidApi extends AbstractApi
{
	public AndroidApi()
	{
		MainTag = "<android>";
		Parser = new ApiParser();
		Parser.PackagePrefix = "android";
		ClassDocPath = "classdoc/android";
	}
}
