package anacrowd.documentation.links;

import java.util.ArrayList;
import java.util.List;

public class CachedPostBody 
{
	public List<String> CodeBlocks = new ArrayList<String>();
	public List<SimpleHref> Hrefs = new ArrayList<SimpleHref>();
	public List<String> Paragraphs = new ArrayList<String>();

	public static class SimpleHref
	{
		public String AnchorText;
		public String HrefText;
	}
}