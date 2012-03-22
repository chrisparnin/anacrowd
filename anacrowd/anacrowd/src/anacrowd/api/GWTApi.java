package anacrowd.api;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import anacrowd.api.elements.ClassElem;
import anacrowd.api.elements.PackageElem;
import anacrowd.api.parser.ApiParser;

public class GWTApi extends AbstractApi
{
	public GWTApi()
	{
		MainTag = "<gwt>";
		Parser = new GWTHackParser();
		Parser.PackagePrefix = "";
		ClassDocPath = "classdoc/gwt";
	}
	
	public static class GWTHackParser extends ApiParser
	{
		@Override
		public List<PackageElem> Parse(String path)
		{
			Hashtable<String,PackageElem> packages = new Hashtable<String,PackageElem>();
			
			try
			{
				String contents = readStream(new FileInputStream(path));
				String[] lines = contents.split(System.getProperty("line.separator"));
				for( String line : lines)
				{
					String[] tuple = line.split("\t");
					String namespace = tuple[0].trim();
					String className = tuple[1].trim();
					
					if( !packages.containsKey(namespace))
					{
						PackageElem p = new PackageElem();
						p.Name = namespace;
						packages.put(namespace, p);
					}
					ClassElem klass = new ClassElem();
					klass.Name = className;
					klass.ParentPackage = packages.get(namespace);
					packages.get(namespace).Classes.add(klass);
				}
			} 
			catch (FileNotFoundException e) 
			{
				e.printStackTrace();
			}
			this.ParsedData = new ArrayList<PackageElem>(packages.values());
			return this.ParsedData;
		}
		
		public static String readStream(InputStream is)
		{
		    StringBuilder sb = new StringBuilder(512);
		    try {
		        Reader r = new InputStreamReader(is, "UTF-8");
		        int c = 0;
		        while (c != -1) {
		            c = r.read();
		            sb.append((char) c);
		        }
		    } catch (IOException e) {
		        throw new RuntimeException(e);
		    }
		    return sb.toString();
		}
	}
}
