package ru.bgcrm.dynamic.model;

import java.util.ArrayList;
import java.util.List;

public class CompilationResult
{
	private List<CompilationMessage> errors = new ArrayList<CompilationMessage>();
	private List<CompilationMessage> warnings = new ArrayList<CompilationMessage>();
	
	public List<CompilationMessage> getWarnings()
    {
	    return warnings;
    }
	
	public List<CompilationMessage> getErrors()
    {
	    return errors;
    }
	
	public String getLogString()
	{
		StringBuilder result = new StringBuilder( 1000 );
		
		addMessages( errors, "\nErrors", result );
		addMessages( warnings, "\nWarnings", result );
		
		return result.toString(); 
	}
	
	private void addMessages( List<CompilationMessage> errors, String prefix, StringBuilder result )
	{
		result.append( prefix );
		result.append( " (" );
		result.append( errors.size() );
		result.append( "):\n" );
		
		for( CompilationMessage message : errors )
		{
			result.append( message );
			result.append( "\n" );
		}
	}
}
