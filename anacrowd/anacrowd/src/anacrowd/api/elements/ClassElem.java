package anacrowd.api.elements;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

public class ClassElem 
{
	public PackageElem ParentPackage;
	public String Name;
	public List<MethodElem> Methods = new ArrayList<MethodElem>();
	public HashSet<String> MethodsSet = new HashSet<String>();

	// Sanity checking
	public boolean HasClassNameCollision;
	public List<ClassElem> MatchesAsSubset = new ArrayList<ClassElem>();
	
	public String getFQN()
	{
		return this.ParentPackage.Name + ":" + this.Name;
	}

	
	
	// Analytics... TODO depecrated
	public int NumQuestions;
	public List<Integer> QuestionIds = new ArrayList<Integer>();
	public List<Integer> AnswerIds = new ArrayList<Integer>();

	public Hashtable<Integer,Integer> AnswerIdToParentId = new Hashtable<Integer,Integer>();
}