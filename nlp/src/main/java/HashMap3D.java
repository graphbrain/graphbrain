package com.graphbrain.nlp;

/*	Please see the license information at the end of this file. */

import java.util.*;

/**	Three dimensional Map.
 *
 *	<p>
 *	The three dimensional map is implemented using a regular map
 *	with keys formed by concatenating the toString() values of
 *	individual keys.
 *	</p>
 *
 *	<p>
 *	Example of use:
 *	</p>
 *
 *	<blockquote>
 *	<p>
 *	<code>
 *	HashMap3D map	= new HashMap3D();
 *	map.put( "a" , "b" , "c" , "my value" );
 *	String s	= map.get( "a" , "b" , "c" );
 *	</code>
 *	</p>
 *	</blockquote>
 *
 *	<p>
 *	Note: HashMap3D does not implement the Map interface.
 *	</p>
 */

public class HashMap3D<
		K1 extends Comparable,
		K2 extends Comparable,
		K3 extends Comparable,
		V>
	implements Map3D<K1, K2, K3, V>
{
	/**	The map used to hold the multikey data.
	 */

	protected Map<K1, Map<K2,Map<K3,V>>> localMap;

	/**	Create three dimensional hash map.
	 */

	public HashMap3D()
	{
		localMap	= MapFactory.createNewMap();
	}

	/**	Create three dimensional map with specified initial row capacity.
	 *
	 *	@param	initialCapacity
	 */

	public HashMap3D( int initialCapacity )
	{
		localMap	= MapFactory.createNewMap( initialCapacity );
	}

	/**	Clear all entries.
	 */

	public void clear()
	{
		Iterator<K1> iterator	= localMap.keySet().iterator();

		while ( iterator.hasNext() )
		{
			Map<K2,Map<K3,V>> columnMap	= localMap.get( iterator.next() );

			if ( columnMap != null )
			{
				Iterator<K2> iterator2	= columnMap.keySet().iterator();

				while ( iterator2.hasNext() )
				{
					Map<K3,V> sliceMap	= columnMap.get( iterator2.next() );

					if ( sliceMap != null )
					{
						sliceMap.clear();
					}
				}

				columnMap.clear();
			}
		}

		localMap.clear();
	}

	/**	Return number of entries.
	 *
	 *	@return		Number of entries in map.
	 *
	 *	<p>
	 *	The number of entries is given by the sum of the number of
	 *	entries for each submap.
	 *	</p>
	 */

	public int size()
	{
		int result	= 0;

		Iterator<K1> iterator	= localMap.keySet().iterator();

		while ( iterator.hasNext() )
		{
			Map<K2,Map<K3,V>> columnMap	= localMap.get( iterator.next() );

			if ( columnMap != null )
			{
				Iterator<K2> iterator2	= columnMap.keySet().iterator();

				while ( iterator2.hasNext() )
				{
					Map<K3,V> sliceMap	= columnMap.get( iterator2.next() );

					if ( sliceMap != null )
					{
						result	+= sliceMap.size();
					}
				}
			}
		}

		return result;
	}

    /**	Determine if map contains a key triple.
     *
     *	@param	rowKey		Row key.
     *	@param	columnKey	Column key.
     *	@param	sliceKey	Slice key.
     *
     *	@return					true if entry exists, false otherwise.
     */

	public boolean containsKeys
	(
		Object rowKey ,
		Object columnKey ,
		Object sliceKey
	)
	{
		boolean result	= false;

		Map<K2,Map<K3,V>> columnMap	= localMap.get( rowKey );

		if ( columnMap != null )
		{
			Map<K3,V> sliceMap	= columnMap.get( columnKey );

			if ( sliceMap != null )
			{
				result	= sliceMap.containsKey( sliceKey ) ;
			}
		}

		return result;
	}

    /**	Determine if map contains a compound key.
     *
     *	@param	key		Compound key.
     *
     *	@return				true if entry exists, false otherwise.
     */

	public boolean containsKey( CompoundKey key )
	{
		Comparable[] keyValues	= key.getKeyValues();

		return
			containsKeys
			(
				keyValues[ 0 ] ,
				keyValues[ 1 ] ,
				keyValues[ 2 ]
			);
	}

	/**	Get value at specified (rowKey, columnKey, sliceKey) position.
	 *
	 *	@param		rowKey		Row key.
	 *	@param		columnKey	Column key.
	 *	@param		sliceKey	Slice key.
	 *
	 *	@return			Value at (rowKey, columnKey, sliceKey) position.
	 */

	public V get
	(
		Object rowKey ,
		Object columnKey ,
		Object sliceKey
	)
	{
		V result	= null;

		Map<K2,Map<K3,V>> columnMap	= localMap.get( rowKey );

		if ( columnMap != null )
		{
			Map<K3,V> sliceMap	= columnMap.get( columnKey );

			if ( sliceMap != null )
			{
				result	= sliceMap.get( sliceKey ) ;
			}
		}

		return result;
	}

	/**	Get value at specified CompoundKey position.
	 *
	 *	@param		key		Compound key.
	 *
	 *	@return		The value at the specified compound key position.
	 */

	public V get( CompoundKey key )
	{
		Comparable[] keyValues	= key.getKeyValues();

		return get( keyValues[ 0 ] , keyValues[ 1 ] , keyValues[ 2 ]  );
	}

	/**	Add value for specified (rowKey, columnKey, sliceKey).
	 *
	 *	@param		rowKey		Row key.
	 *	@param		columnKey	Column key.
	 *	@param		sliceKey	Slice key.
	 *	@param		value		Value to store.
	 *
	 *	@return		Previous value for (rowKey, columnKey, sliceKey).
	 *				May be null.
	 */

	public V put
	(
		K1 rowKey ,
		K2 columnKey ,
		K3 sliceKey ,
		V value
	)
	{
		V previousValue	= null;

		Map<K2,Map<K3,V>> columnMap	= localMap.get( rowKey );
		Map<K3,V> sliceMap				= null;

		if ( columnMap != null )
		{
			sliceMap	= columnMap.get( columnKey );

			if ( sliceMap != null )
			{
				previousValue	= sliceMap.get( sliceKey );
			}
			else
			{
				sliceMap		= MapFactory.createNewMap();
			}
		}
		else
		{
			columnMap	= MapFactory.createNewMap();
			sliceMap	= MapFactory.createNewMap();
		}

		sliceMap.put( sliceKey , value );

		columnMap.put( columnKey , sliceMap );

								//	Add or update value in column map
								//	for specified column key.

		localMap.put( rowKey , columnMap );

								//	Return previous value.

		return previousValue;
	}

	/**	Remove entry at (rowKey, columnKey, sliceKey).
	 *
	 *	@param		rowKey		Row key.
	 *	@param		columnKey	Column key.
	 *	@param		sliceKey	Slice key.
	 *
	 *	@return		Previous value for (rowKey, columnKey, sliceKey).
	 *				May be null.
	 */

	public V remove
	(
		Object rowKey ,
		Object columnKey ,
		Object sliceKey
	)
	{
		V result	= null;

		Map<K2,Map<K3,V>> columnMap	= localMap.get( rowKey );

		if ( columnMap != null )
		{
			Map<K3,V> sliceMap	= columnMap.get( columnKey );

			if ( sliceMap != null )
			{
				result	= sliceMap.get( sliceKey ) ;

				sliceMap.remove( sliceKey );
/*
				if ( sliceMap.size() == 0 )
				{
					columnMap.remove( columnKey );
				}
*/
			}
		}

		return result;
	}

	/**	Get the compound key set.
	 *
	 *	@return		The compound key set.
	 */

	public Set<CompoundKey> keySet()
	{
		Set<CompoundKey> result	= SetFactory.createNewSet();

		for ( K1 rowKey : localMap.keySet() )
		{
			Map<K2,Map<K3,V>> columnMap	= localMap.get( rowKey );

			if ( columnMap != null )
			{
				for ( K2 columnKey : columnMap.keySet() )
				{
					Map<K3,V> sliceMap	= columnMap.get( columnKey );

					if ( sliceMap != null )
					{
						for ( K3 sliceKey : sliceMap.keySet() )
						{
							CompoundKey key	=
								new CompoundKey
								(
									(Comparable)rowKey ,
									(Comparable)columnKey ,
									(Comparable)sliceKey
								);

							result.add( key );
						}
					}
				}
			}
		}

		return result;
	}

	/**	Get row key set.
	 *
	 *		@return 	rows key set.
	 */

	@SuppressWarnings("unchecked")
	public Set<K1> rowKeySet()
	{
		Set<K1> rowSet	= SetFactory.createNewSet();

		Iterator<K1> iterator	= localMap.keySet().iterator();

		while( iterator.hasNext() )
		{
			rowSet.add( iterator.next() );
		}

		return rowSet;
	}

	/**	Get column  key set.
	 *
	 *		@return 	column key set.
	 */

	@SuppressWarnings("unchecked")
	public Set<K2> columnKeySet()
	{
		Set<K2> columnSet		= SetFactory.createNewSet();

		Iterator<K1> iterator	= localMap.keySet().iterator();

		while( iterator.hasNext() )
		{
			Iterator<K2> iterator2=
				localMap.get( iterator.next() ).keySet().iterator();

			while ( iterator2.hasNext() )
			{
				columnSet.add( iterator2.next() );
			}
		}

		return columnSet;
	}

	/**	Get slice  key set.
	 *
	 *		@return 	slice key set.
	 */

	@SuppressWarnings("unchecked")
	public Set<K3> sliceKeySet()
	{
		Set<K3> sliceSet		= SetFactory.createNewSet();

		Iterator<CompoundKey> iterator	= keySet().iterator();

		CompoundKey key;

		while( iterator.hasNext() )
		{
			key	= iterator.next();

			sliceSet.add( (K3)(key.getKeyValues()[ 2 ] ) );
		}

		return sliceSet;
	}

	/**	Get iterator over keys.
	 */

	public Iterator<CompoundKey> iterator()
	{
		return keySet().iterator();
	}

	/**	Return formatted string displaying all entries.
	 *
	 *	@return		Formatted string displaying all entries.
	 */

	public String toString()
	{
		StringBuffer sb	= new StringBuffer();

		Iterator<CompoundKey> iterator	= keySet().iterator();

		while ( iterator.hasNext() )
		{
			CompoundKey key	= iterator.next();

			if ( sb.length() > 0 )
			{
				sb.append( "; " );
			}

			sb.append( key.toString() );
			sb.append( "=" );
			sb.append( get( key ) );
		}

		return "[" + sb.toString() + "]";
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



