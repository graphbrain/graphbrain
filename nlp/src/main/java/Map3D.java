package com.graphbrain.nlp;

/*	Please see the license information at the end of this file. */

import java.util.*;

/**	Interface for two dimensional map.
 */

public interface Map3D<R extends Comparable, C extends Comparable,
	S extends Comparable, V>
{
	/**	Clear all entries from this map and its child maps.
	 */

	public void clear();

	/**	Return number of entries.
	 *
	 *	@return		Number of entries in map.
	 */

	public int size();

    /**	Determine if map contains a key pair.
     *
     *	@param	rowKey		Row key.
     *	@param	columnKey	Column key.
     *	@param	sliceKey	Slice key.
     *
     *	@return				true if entry exists, false otherwise.
     */

	public boolean containsKeys
	(
		Object rowKey ,
		Object columnKey ,
		Object sliceKey
	);

    /**	Determine if map contains a compound key.
     *
     *	@param	key		Compound key.
     *
     *	@return			true if entry exists, false otherwise.
     */

	public boolean containsKey
	(
		CompoundKey key
	);

	/**	Get value at specified (rowKey, columnKey, sliceKey) position.
	 *
	 *	@param		rowKey		Row key.
	 *	@param		columnKey	Column key.
     *	@param		sliceKey	Slice key.
	 *
	 *	@return		The value at the specified (rowKey, columnKey, sliceKey)
	 *				position.
	 */

	public V get( Object rowKey , Object columnKey , Object sliceKey );

	/**	Get value at specified CompoundKey position.
	 *
	 *	@param		key		Compound key.
	 *
	 *	@return		The value at the specified compound key position.
	 */

	public V get( CompoundKey key );

	/**	Add value for specified (rowKey, columnKey) .
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
		R rowKey ,
		C columnKey ,
		S sliceKey ,
		V value
	);

	/**	Remove entry at (rowKey, columnKey).
	 *
	 *	@param		rowKey		Row key.
	 *	@param		columnKey	Column key.
	 *
	 *	@return		Previous value for (rowKey, columnKey).
	 *				May be null.
	 */

	public V remove( Object rowKey , Object columnKey , Object sliceKey );

	/**	Get the compound key set.
	 *
	 *	@return		The compound key set.
	 */

	public Set<CompoundKey> keySet();

	/**	Get row key set.
	 *
	 *	@return 	rows key set.
	 */

	public Set<R> rowKeySet();

	/**	Get column key set.
	 *
	 *	@return 	column key set.
	 */

	public Set<C> columnKeySet();

	/**	Get slice key set.
	 *
	 *	@return 			Slice key set.
	 */

	public Set<S> sliceKeySet();

	/**	Get compound key iterator.
	 *
	 *	@return		Compound key iterator.
	 */

	public Iterator<CompoundKey> iterator();
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



