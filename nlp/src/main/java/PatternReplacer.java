package com.graphbrain.nlp;

/*	Please see the license information at the end of this file. */

import java.util.regex.*;

/**	Defines a pattern replacer.
 *
 *	<p>
 *	Defines a source pattern (regular expresssion) and its replacement
 *	string, along with a method for performing the replacement.
 *	</p>
 */

public class PatternReplacer
{
	/**	Source pattern string. */

	protected String sourcePattern;

	/**	Compiled source pattern matcher. */

	protected Matcher sourcePatternMatcher;

	/**	Replacement. */

	protected String replacementPattern;

	/**	Create a pattern replacer definition.
	 *
	 *	@param	sourcePattern		Source pattern string as a
	 *								regular expression.
	 *
	 *	@param	replacementPattern	Replacement pattern string
	 *								as a regular expression
	 *								replacement expression.
	 */

	public PatternReplacer
	(
		String sourcePattern ,
		String replacementPattern
	)
	{
		this.sourcePattern			= sourcePattern;
		this.replacementPattern		= replacementPattern;

		this.sourcePatternMatcher	=
			Pattern.compile( sourcePattern ).matcher( "" );
	}

	/**	Return matched groups.
	 *
	 *	@param	s	String to match.
	 *
	 *	@return		String array of matched groups.
	 *				Null if match fails.
	 */

	public String[] matchGroups( String s )
	{
		String[] result	= null;

		if ( sourcePatternMatcher.reset( s ).find() )
		{
			int groupCount	= sourcePatternMatcher.groupCount();

			result	= new String[ groupCount + 1 ];

			for ( int i = 0 ; i <= groupCount ; i++ )
			{
				result[ i ]	= sourcePatternMatcher.group( i );
			}
		}

		return result;
	}

	/**	Perform replacement.
	 *
	 *	@param	s	String in which to perform replacement.
	 *
	 *	@return		String with source pattern replaced.
	 */

	public String replace( String s )
	{
		return
			sourcePatternMatcher.reset( s ).replaceAll( replacementPattern );
	}

	/**	Display pattern replacer as string.
	 *
	 *	@return		Pattern replacer as string.
	 */

	public String toString()
	{
		return sourcePattern + " -> " + replacementPattern;
	}
}

/*
Copyright (c) 2008, 2009 by Northwestern University.
All rights reserved.

Developed by:
   Academic and Research Technologies
   Northwestern University
   http://www.it.northwestern.edu/about/departments/at/

Permission is hereby granted, free of charge, to any person
obtaining a copy of this software and associated documentation
files (the "Software"), to deal with the Software without
restriction, including without limitation the rights to use,
copy, modify, merge, publish, distribute, sublicense, and/or
sell copies of the Software, and to permit persons to whom the
Software is furnished to do so, subject to the following
conditions:

    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimers.

    * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the following
      disclaimers in the documentation and/or other materials provided
      with the distribution.

    * Neither the names of Academic and Research Technologies,
      Northwestern University, nor the names of its contributors may be
      used to endorse or promote products derived from this Software
      without specific prior written permission.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE CONTRIBUTORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
*/



