package anacrowd.api.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import anacrowd.api.elements.ClassElem;
import anacrowd.api.elements.MethodElem;
import anacrowd.api.elements.PackageElem;

public class ApiParser extends DefaultHandler
{
	public List<PackageElem> ParsedData;
	public String PackagePrefix;

	List<PackageElem> Packages;
	PackageElem CurrentPackage;
	ClassElem CurrentClass;
	
	public List<PackageElem> Parse(String path)
	{
		try 
		{
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			XMLReader xr = parser.getXMLReader();
			xr.setContentHandler(this);
			
			xr.parse(path);
			
			return ParsedData;
		} 
		catch (ParserConfigurationException e) {
			e.printStackTrace();
		} 
		catch (SAXException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void startDocument()
	{
		Packages = new ArrayList<PackageElem>();
	}

	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException 
	{
		if( qName.equals("package"))
		{
			String name = attributes.getValue("name");
			CurrentPackage = new PackageElem();
			CurrentPackage.Name = name;
		}
		
		if( qName.equals("class") )
		{
			String name = attributes.getValue("name");
			CurrentClass = new ClassElem();
			CurrentClass.Name = name;
			CurrentClass.ParentPackage = CurrentPackage;
			
			CurrentPackage.Classes.add(CurrentClass);
		}
		
		if( qName.equals("method") )
		{
			String name = attributes.getValue("name");
			boolean isPublic = attributes.getValue("visibility").equals("public");

			MethodElem meth = new MethodElem();
			meth.Name = name;
			meth.IsPublic = isPublic;
			
			
			if( !CurrentClass.MethodsSet.contains(meth.Name) )
			{
				CurrentClass.Methods.add(meth);
			}
			else
			{
				/*for( MethodElem m : CurrentClass.Methods )
				{
					if( m.Name.equals(meth.Name) )
					{
						m.Overloads++;
					}
				}*/
			}
			CurrentClass.MethodsSet.add(meth.Name);

		}
	}
	
	public void endElement(String ur, String localName, String qName)
	{
		if( qName.equals("package"))
		{
			if( CurrentPackage.Name.startsWith(this.PackagePrefix) )
			{
				this.Packages.add(CurrentPackage);
			}
		}
	}
	
	public void endDocument()
	{
		ParsedData = this.Packages;
	}
}
