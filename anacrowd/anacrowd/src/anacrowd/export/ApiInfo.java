package anacrowd.export;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import anacrowd.api.AbstractApi;
import anacrowd.api.elements.ClassElem;
import anacrowd.api.elements.MethodElem;
import anacrowd.api.elements.PackageElem;

public class ApiInfo 
{
	public void DumpInfo( AbstractApi api, String outFile )
	{
		try 
		{
			FileWriter fw = new FileWriter(outFile);
		
			for( PackageElem pack : api.Packages )
			{
				for( ClassElem klass : pack.Classes )
				{
					fw.write( join( new String[]{pack.Name +":" +klass.Name, klass.Methods.size()+""}, ","));
					fw.write("\n");
				}
			}
		
			
			fw.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	 static String join(String[] stringsA, String delimiter) {
	     StringBuilder builder = new StringBuilder();
	     Collection<String> strings = Arrays.asList(stringsA);
	     Iterator iter = strings.iterator();
	     while (iter.hasNext()) {
	         builder.append(iter.next());
	         if (!iter.hasNext()) {
	           break;                  
	         }
	         builder.append(delimiter);
	     }
	     return builder.toString();
	 }
}

 