package anacrowd.formatters;

import anacrowd.api.AbstractApi;
import anacrowd.api.elements.ClassElem;
import anacrowd.api.elements.MethodElem;
import anacrowd.api.elements.PackageElem;

public class ConsoleFormatter 
{
	public void FormatSummary( AbstractApi api)
	{
		System.out.println( api.MainTag );
		
		int sum = 0;
		int coverageCount = 0;
		int klassCount = 0;
		
		int methodCoverageCount = 0;
		int methodCount = 0;
		int methodSum = 0;
		
		for( PackageElem pack : api.Packages )
		{
			for( ClassElem klass : pack.Classes )
			{
				int num = klass.QuestionIds.size() + klass.AnswerIds.size();
				if( num > 0 )
				{
					coverageCount++;
				}
				sum += num;
				klassCount++;
				
				for( MethodElem meth : klass.Methods )
				{
					int mNum = meth.QuestionIds.size() + meth.AnswerIds.size();
					if( mNum > 0 )
					{
						methodCoverageCount++;
					}
					methodCount++;
					methodSum += mNum;
				}
			}
		}
		
		System.out.println("Class coverage " + coverageCount + "/" + klassCount );
		System.out.println("Class coverage percentage " + (double)coverageCount / klassCount );
		System.out.println("Class question average " + (double)sum / klassCount );
		
		System.out.println("Method coverage " + methodCoverageCount + "/" + methodCount );
		System.out.println("Method coverage percentage " + (double)methodCoverageCount / methodCount );
		System.out.println("Method question average " + (double)methodSum / methodCount );

	}
}