package com.graphbrain.eco.conjugator;

import java.io.*;
import java.net.*;
import java.util.*;

/**	Set utilities.
 */

public class SetUtils
{
	public static Set<String> loadSet(BufferedReader bufferedReader)
	    throws IOException {

        Set<String> set	= SetFactory.createNewSet();

		if (bufferedReader != null) {
			String inputLine = bufferedReader.readLine();

			while (inputLine != null) {
				set.add(inputLine);
				inputLine = bufferedReader.readLine();
			}

			bufferedReader.close();
		}

		return set;
	}

    public static Set<String> loadSet(InputStream is)
            throws IOException {

        if (is != null) {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            return loadSet(br);
        }

        return null;
    }

    /**	Load string set from a URL.
     *
     *	@param	setURL		URL for set file.
     *	@param	encoding	Character encoding for the file.
     *
     *	@throws FileNotFoundException	If input file does not exist.
     *	@throws IOException				If input file cannot be opened.
     *
     *	@return				Set with values read from file.
     */
    public static Set<String> loadSet(URL setURL, String encoding)
            throws IOException {

        if (setURL != null) {
            BufferedReader bufferedReader = new BufferedReader(
                    new UnicodeReader(setURL.openStream(), encoding));

            return loadSet(bufferedReader);
        }

        return null;
    }

	/**	Load string set from a file.
	 *
	 *	@param	setFile		File from which to load set.
	 *	@param	encoding	Character encoding for the file.
	 *
	 *	@throws FileNotFoundException	If input file does not exist.
	 *	@throws IOException				If input file cannot be opened.
	 *
	 *	@return				Set with values read from file.
	 */

	public static Set<String> loadSet(File setFile, String encoding)
		throws IOException {

		return loadSet(setFile.toURI().toURL(), encoding);
	}

	/**	Load string set from a file name.
	 *
	 *	@param	setFileName		File name from which to load set.
	 *	@param	encoding		Character encoding for the file.
	 *
	 *	@throws FileNotFoundException	If input file does not exist.
	 *	@throws IOException				If input file cannot be opened.
	 *
	 *	@return					Set with values read from file name.
	 */

	public static Set<String> loadSet(String setFileName, String encoding)
		throws IOException {

        return loadSet(new File(setFileName), encoding );
	}

	/**	Save set as string to a file.
	 *
	 *	@param	set				Set to save.
	 *	@param	setFile			Output file name.
	 *	@param	encoding		Character encoding for the file.
	 *
	 *	@throws IOException		If output file has error.
	 */

	public static void saveSet
	(
		Set<?> set ,
		File setFile ,
		String encoding
	)
		throws IOException , FileNotFoundException
	{
		if ( set != null )
		{
			PrintWriter printWriter	= new PrintWriter( setFile , "utf-8" );

			Iterator<?> iterator		= set.iterator();

			while ( iterator.hasNext() )
			{
				String value	= iterator.next().toString();

				printWriter.println( value );
			}

			printWriter.flush();
			printWriter.close();
		}
	}

	/**	Save set as string to a file name.
	 *
	 *	@param	set				Set to save.
	 *	@param	setFileName		Output file name.
	 *	@param	encoding		Character encoding for the file.
	 *
	 *	@throws IOException		If output file has error.
	 */

	public static void saveSet
	(
		Set<?> set ,
		String setFileName ,
		String encoding
	)
		throws IOException , FileNotFoundException
	{
		saveSet( set , new File( setFileName ) , encoding );
	}

	/**	Add array entries to a set.
	 *
	 *	@param	set	Set to which to add array entries.
	 *	@param	t	Array whose entries are to be added.
	 */

	public static<T> void addAll
	(
		Set<T> set ,
		T[] t
	)
		throws IOException , FileNotFoundException
	{
		set.addAll( Arrays.asList( t ) );
	}

	/** Don't allow instantiation, do allow overrides. */

	protected SetUtils()
	{
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



