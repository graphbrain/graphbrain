package com.graphbrain.eco.conjugator;

/*	Please see the license information at the end of this file. */

import java.io.Serializable;
import java.util.*;

/**	A compound key class for collections.
 */

public class CompoundKey implements XCloneable , Comparable, Serializable
{
    /** Object array of key values.  Key values should be simple objects.
     *
     *	<p>
     *	The key value is declared final because its values should not
     *	be changed once it is defined.
     *	</p>
     */

	protected final Comparable keyValues[];

    /**	Create a CompoundKey.
     *
     *	@param	keyValues	Values of the key as an array.
     *
     *	<p>
     *	Each key value must implement the comparable interface.
     *	</p>
     */

	public CompoundKey( Comparable keyValues[] )
    {
		this.keyValues = keyValues;
	}

    /**	Create a CompoundKey with one value.
     *
     *	@param	row    	"Row" value.
     */

	public CompoundKey( Comparable row )
	{
		this( new Comparable[]{ row } );
	}

    /**	Create a CompoundKey with two values.
     *
     *	@param	row    	"Row" value.
     *	@param	column	"Column" value.
     */

	public CompoundKey( Comparable row , Comparable column )
	{
		this( new Comparable[]{ row , column } );
	}

    /**	Create a CompoundKey with two values.
     *
     *	@param	row    	"Row" value.
     *	@param	column	"Column" value.
     *	@param	slice	"Slice" value.
     */

	public CompoundKey
	(
		Comparable row ,
		Comparable column ,
		Comparable slice
	)
	{
		this( new Comparable[]{ row , column , slice } );
	}

    /**	Check if another object is equal to this one.
     *
     *	@param	object  Other object to test for equality.
     *
     *	@return			true if other object is equal to this one.
     */

	public boolean equals( Object object )
	{
		return
			java.util.Arrays.equals
			(
				this.keyValues ,
				((CompoundKey)object).keyValues
			);
	}

 	/**	Compare this key with another.
 	 *
 	 *	@param	object		The other CompoundKey.
 	 *
	 *	@return				> 0 if the other key is less than this one,
	 *						= 0 if the two keys are equal,
	 *						< 0 if the other key is greater than this one.
 	 *
 	 *	<p>
 	 *	We use compareTo on the array entries in the key.
 	 *	This may not give the desired result if the array entries
 	 *	are themselves arrays.
 	 *	</p>
 	 */

	@SuppressWarnings("unchecked")
	public int compareTo( Object object )
	{
		int result	= 0;

		if ( ( object == null ) ||
			!( object instanceof CompoundKey ) )
		{
			result	= Integer.MIN_VALUE;
		}
		else
		{
			CompoundKey otherKey	= (CompoundKey)object;

			int minKeyValuesLength	=
				Math.min( keyValues.length , otherKey.keyValues.length );

			for ( int i = 0 ; i < minKeyValuesLength ; i++ )
			{
				result	=
					keyValues[ i ].compareTo( otherKey.keyValues[ i ] );

				if ( result != 0 ) break;
			}

			if ( result == 0 )
			{
				if ( keyValues.length > otherKey.keyValues.length )
				{
					result	= 1;
				}
				else if ( keyValues.length < otherKey.keyValues.length )
				{
					result	= -1;
				}
			}
		}

		return result;
	}

    /**	Get the hash code of the keys.
     *
     *	@return		The hash code.
     */

	public int hashCode()
	{
		return java.util.Arrays.hashCode( keyValues );
	}

	/**	Return a string representation of this object.
	 *
	 *	@return		A string representation of this object.
	 */

	public String toString()
	{
		return "" + Arrays.asList( keyValues );
	}

	/**	Get key values.
	 *
	 *	@return		Object array of key values.
	 */

	public Comparable[] getKeyValues()
	{
		return keyValues;
	}

	/**	Return a shallow cloned copy of this object.
	 *
	 *	@return		Shallow clone of this object.
	 *
	 *	@throws		CloneNotSupportedException which should never happen.
	 */

	public Object clone()
	{
		try
		{
			return super.clone();
		}
		catch ( CloneNotSupportedException e )
		{
			throw new InternalError();
		}
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



