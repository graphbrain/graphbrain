package com.graphbrain.nlp;
/*	Please see the license information at the end of this file. */

import java.io.InputStream;
import java.util.*;

/** English language conjugator.
 */

public class EnglishConjugator implements Conjugator
{
	/**	Set of verbs whose final consonant is doubled in inflected forms. */

	protected static Set<String> doublingVerbs	= null;

	/**	Resource path to list of doubled consonant verbs. */

	public static final String doublingVerbsPath =
		"doublingverbs.txt";

	/**	Present participle replacement patterns. */

	/*protected static final PatternReplacer presentParticiplePattern1	=
		new PatternReplacer( "(.[bcdfghjklmnpqrstvwxyz])eing$" , "$1ing" );*/

	/*protected static final PatternReplacer presentParticiplePattern2	=
		new PatternReplacer( "ieing$" , "ying" );*/

	/**	Map3D of irregular verbs.
	 *
	 *	<p>
	 *	First key is the infinitive.<br />
	 *	Second key is the person.<br />
	 *	Third key is the verb tense.<br />
	 *	Value is the conjugated verb form.
	 *	</p>
	 */

	protected Map3D<String, String, String, String> irregularVerbs = null;

	/**	Resource path to map of irregular verbs. */

	public static final String irregularVerbsPath = "irregularverbs.txt";

	/**	Create an English conjugator. */

	public EnglishConjugator() {
		//	Load consonant doubling verbs.
		if (doublingVerbs == null) {
			try {
                InputStream is = getClass().getClassLoader().getResourceAsStream(EnglishConjugator.doublingVerbsPath);
				doublingVerbs =
					SetUtils.loadSet(is);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		//	Load irregular verb forms.
		if (irregularVerbs == null) {
			try {
                InputStream is = getClass().getClassLoader().getResourceAsStream(EnglishConjugator.irregularVerbsPath);
				irregularVerbs =
					Map3DUtils.loadMap3D(is, "\t", "", "utf-8" );
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**	Conjugate a verb from its lemma (infinitive), tense, and person.
	 *
	 *	@param	infinitive	The infinitive of the verb to inflect.
	 *	@param	tense		The verb tense to generate.
	 *	@param	person		The person (1st, 2nd, 3rd) to generate.
	 *
	 *	@return				The English inflected form of the verb
	 *						in all lower case.
	 */

	public String conjugate
	(
		String infinitive ,
		VerbTense tense ,
		Person person
	)
	{
								//	We only work with lower case verbs.

		String verb	= infinitive.toLowerCase();
		
								//	Check for irregular verb.
		String result	=
			irregularVerbs.get
			(
				verb ,
				person.toString() ,
				tense.toString()
			);

		if ( result == null )
		{
			result	=
				irregularVerbs.get
				(
					verb ,
					"*" ,
					tense.toString()
				);
		}
								//	If we found an irregular verb form,
								//	we're done.

		if ( result != null ) return result;

		result	= verb;
								//	If we didn't find an irregular
								//	verb form, proceeed assuming we
								//	have a regular verb.
		switch ( tense )
		{
			case PRESENT:
				if ( person == Person.THIRD_PERSON_SINGULAR )
				{
					if ( result.matches( ".*(ch|s|sh|x|z)$" ) )
					{
						result	+= "es";
					}
/*
					else if ( result.matches( ".*(ay|ey|oy|uy)$" ) )
					{
						result	+= "s";
					}
*/
					else if ( result.matches( ".*[^aeiou]y" ) )
					{
						result	=
							result.substring( 0 , result.length() - 1 ) +
							"ies";
					}
					else
					{
						result	+= "s";
					}
				}
				break;

			case PRESENT_PARTICIPLE:
				if ( result.matches( ".*[^aeiou]e" ) )
				{
					result	=
						result.substring( 0 , result.length() - 1 );
				}
				else if ( result.endsWith( "ie" ) )
				{
					result	=
						result.substring( 0 , result.length() - 2 ) + "y";
				}
				else if ( result.matches( ".*[aou]e" ) )
				{
					result	=
						result.substring( 0 , result.length() - 1 );
				}
				else if ( doublingVerbs.contains( verb ) )
				{
					result	+=
						result.substring(
							result.length() - 1 , result.length() );
				}

				result	+= "ing";
//				result	= presentParticiplePattern1.replace( result );
//				result	= presentParticiplePattern2.replace( result );

				break;

			case PAST:
			case PAST_PARTICIPLE:
				if ( result.endsWith( "e" ) )
				{
					result	+= "d";
				}
				else if ( result.matches( ".*[^aeiou]y" ) )
				{
					result	=
						result.substring( 0 , result.length() - 1 ) +
						"ied";
				}
				else
				{
					if ( doublingVerbs.contains( verb ) )
					{
						result	+=
							result.substring(
								result.length() - 1 , result.length() );
					}
						result	+= "ed";
				}

				break;
		}

		return result;
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



