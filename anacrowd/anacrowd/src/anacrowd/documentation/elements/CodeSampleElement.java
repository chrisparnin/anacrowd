package anacrowd.documentation.elements;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class CodeSampleElement 
{
	public PostElement Parent;
	public List<CodeBlock> Blocks = new ArrayList<CodeBlock>();
	public int Votes;
	
	public static class CodeBlock
	{		
		//public String Text;
		private byte[] compressedBody;
		public String getBody()
		{
			try
			{
				return decompress(compressedBody);
			}
			catch (IOException ex) 
			{
				ex.printStackTrace();
				return null;
			}
		}
		
		public void setBody(String body)
		{
			try 
			{
				compressedBody = compress(body);
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}

		// http://stackoverflow.com/questions/6717165/how-can-i-compress-a-string-using-gzipoutputstream-and-vice-versa
		public static byte[] compress(String string) throws IOException 
		{
		    ByteArrayOutputStream os = new ByteArrayOutputStream(string.length());
		    GZIPOutputStream gos = new GZIPOutputStream(os);
		    gos.write(string.getBytes());
		    gos.close();
		    byte[] compressed = os.toByteArray();
		    os.close();
		    return compressed;
		}
		
		public static String decompress(byte[] compressed) throws IOException 
		{
		    final int BUFFER_SIZE = 64;
		    ByteArrayInputStream is = new ByteArrayInputStream(compressed);
		    GZIPInputStream gis = new GZIPInputStream(is, BUFFER_SIZE);
		    StringBuilder string = new StringBuilder();
		    byte[] data = new byte[BUFFER_SIZE];
		    int bytesRead;
		    while ((bytesRead = gis.read(data)) != -1) {
		        string.append(new String(data, 0, bytesRead));
		    }
		    gis.close();
		    is.close();
		    return string.toString();
		}

		
		
		public List<CodeSpan> ClassMatches = new ArrayList<CodeSpan>();
	}

	public static class CodeSpan
	{
		public CodeSpan(int s,int e, String KlassMatch, String KlassFQN)
		{
			this.Start = s;
			this.End = e;
			this.KlassMatch = KlassMatch;
			this.KlassFQN = KlassFQN;
		}
		public int Start;
		public int End;
		
		public String KlassMatch;
		public String KlassFQN;
	}
}


