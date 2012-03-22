package anacrowd.documentation.links;

public class LinkSpan 
{
	public int PostId;
	public int Start;
	public int End;
	public boolean Validated;

	public String Site; // Temporary holding of body for CodeSample's Code Blocks.
	public int SiteIndex;
	public LinkType LinkType;
	
	public enum LinkType
	{
		WordLink,
		CodeSampleLink,
		HrefLink,
		CodeMarkupLink
	}
}
