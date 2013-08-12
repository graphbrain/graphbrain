package com.graphbrain.nlp;

/*	Please see the license information at the end of this file. */

/**	Factory for creating a HashMap3D.
 */

public class Map3DFactory
{
	/**	Create a new HashMap3D.
	 */

	public static<R extends Comparable, C extends Comparable,
		S extends Comparable, V> Map3D<R, C, S, V> createNewMap3D()
	{
		return new HashMap3D<R, C, S, V>();
	}

	/**	Create a new HashMap3D.
	 *
	 *	@param	capacity	Initial capacity.
	 */

	public static<R extends Comparable, C extends Comparable,
		S extends Comparable, V> Map3D<R, C, S, V> createNewMap3D( int capacity )
	{
		return new HashMap3D<R, C, S, V>( capacity );
	}

	/** Don't allow instantiation, do allow overrides. */

	protected Map3DFactory()
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



