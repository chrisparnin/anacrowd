package anacrowd.paperanalysis.datacharacterization;

import anacrowd.api.elements.PackageElem;
import anacrowd.paperanalysis.BaseAnalysis;

public class ApiInfo extends BaseAnalysis
{
	@Override
	public void Run()
	{
		for(PackageElem pack: Java.Packages )
		{
			System.out.println(pack.Name + "," + pack.Classes.size());
		}
	}
	public static void main(String[] args)
	{
		new ApiInfo().Run();
	}
}
