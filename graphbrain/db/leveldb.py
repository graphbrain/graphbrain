#!/usr/bin/env python
#
# Copyright (C) 2012 Space Monkey, Inc.
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.
#

"""
    LevelDB Python interface via C-Types.
    http://code.google.com/p/leveldb-py/

    Missing still (but in progress):
      * custom comparators, filter policies, caches

    This interface requires nothing more than the leveldb shared object with
    the C api being installed.

    Now requires LevelDB 1.6 or newer.

    For most usages, you are likely to only be interested in the "DB" and maybe
    the "WriteBatch" classes for construction. The other classes are helper
    classes that you may end up using as part of those two root classes.

     * DBInterface - This class wraps a LevelDB. Created by either the DB or
            MemoryDB constructors
     * Iterator - this class is created by calls to DBInterface::iterator.
            Supports range requests, seeking, prefix searching, etc
     * WriteBatch - this class is a standalone object. You can perform writes
            and deletes on it, but nothing happens to your database until you
            write the writebatch to the database with DB::write
"""

__author__ = "JT Olds"
__email__ = "jt@spacemonkey.com"

import bisect
import ctypes
import ctypes.util
import weakref
import threading
from collections import namedtuple

_ldb = ctypes.CDLL(ctypes.util.find_library('leveldb'))

_ldb.leveldb_filterpolicy_create_bloom.argtypes = [ctypes.c_int]
_ldb.leveldb_filterpolicy_create_bloom.restype = ctypes.c_void_p
_ldb.leveldb_filterpolicy_destroy.argtypes = [ctypes.c_void_p]
_ldb.leveldb_filterpolicy_destroy.restype = None
_ldb.leveldb_cache_create_lru.argtypes = [ctypes.c_size_t]
_ldb.leveldb_cache_create_lru.restype = ctypes.c_void_p
_ldb.leveldb_cache_destroy.argtypes = [ctypes.c_void_p]
_ldb.leveldb_cache_destroy.restype = None

_ldb.leveldb_options_create.argtypes = []
_ldb.leveldb_options_create.restype = ctypes.c_void_p
_ldb.leveldb_options_set_filter_policy.argtypes = [ctypes.c_void_p,
        ctypes.c_void_p]
_ldb.leveldb_options_set_filter_policy.restype = None
_ldb.leveldb_options_set_create_if_missing.argtypes = [ctypes.c_void_p,
        ctypes.c_ubyte]
_ldb.leveldb_options_set_create_if_missing.restype = None
_ldb.leveldb_options_set_error_if_exists.argtypes = [ctypes.c_void_p,
        ctypes.c_ubyte]
_ldb.leveldb_options_set_error_if_exists.restype = None
_ldb.leveldb_options_set_paranoid_checks.argtypes = [ctypes.c_void_p,
        ctypes.c_ubyte]
_ldb.leveldb_options_set_paranoid_checks.restype = None
_ldb.leveldb_options_set_write_buffer_size.argtypes = [ctypes.c_void_p,
        ctypes.c_size_t]
_ldb.leveldb_options_set_write_buffer_size.restype = None
_ldb.leveldb_options_set_max_open_files.argtypes = [ctypes.c_void_p,
        ctypes.c_int]
_ldb.leveldb_options_set_max_open_files.restype = None
_ldb.leveldb_options_set_cache.argtypes = [ctypes.c_void_p, ctypes.c_void_p]
_ldb.leveldb_options_set_cache.restype = None
_ldb.leveldb_options_set_block_size.argtypes = [ctypes.c_void_p,
        ctypes.c_size_t]
_ldb.leveldb_options_set_block_size.restype = None
_ldb.leveldb_options_destroy.argtypes = [ctypes.c_void_p]
_ldb.leveldb_options_destroy.restype = None

_ldb.leveldb_open.argtypes = [ctypes.c_void_p, ctypes.c_char_p,
        ctypes.c_void_p]
_ldb.leveldb_open.restype = ctypes.c_void_p
_ldb.leveldb_close.argtypes = [ctypes.c_void_p]
_ldb.leveldb_close.restype = None
_ldb.leveldb_put.argtypes = [ctypes.c_void_p, ctypes.c_void_p,
        ctypes.c_void_p, ctypes.c_size_t, ctypes.c_void_p, ctypes.c_size_t,
        ctypes.c_void_p]
_ldb.leveldb_put.restype = None
_ldb.leveldb_delete.argtypes = [ctypes.c_void_p, ctypes.c_void_p,
        ctypes.c_void_p, ctypes.c_size_t, ctypes.c_void_p]
_ldb.leveldb_delete.restype = None
_ldb.leveldb_write.argtypes = [ctypes.c_void_p, ctypes.c_void_p,
        ctypes.c_void_p, ctypes.c_void_p]
_ldb.leveldb_write.restype = None
_ldb.leveldb_get.argtypes = [ctypes.c_void_p, ctypes.c_void_p,
        ctypes.c_void_p, ctypes.c_size_t, ctypes.c_void_p, ctypes.c_void_p]
_ldb.leveldb_get.restype = ctypes.POINTER(ctypes.c_char)

_ldb.leveldb_writeoptions_create.argtypes = []
_ldb.leveldb_writeoptions_create.restype = ctypes.c_void_p
_ldb.leveldb_writeoptions_destroy.argtypes = [ctypes.c_void_p]
_ldb.leveldb_writeoptions_destroy.restype = None
_ldb.leveldb_writeoptions_set_sync.argtypes = [ctypes.c_void_p,
        ctypes.c_ubyte]
_ldb.leveldb_writeoptions_set_sync.restype = None

_ldb.leveldb_readoptions_create.argtypes = []
_ldb.leveldb_readoptions_create.restype = ctypes.c_void_p
_ldb.leveldb_readoptions_destroy.argtypes = [ctypes.c_void_p]
_ldb.leveldb_readoptions_destroy.restype = None
_ldb.leveldb_readoptions_set_verify_checksums.argtypes = [ctypes.c_void_p,
        ctypes.c_ubyte]
_ldb.leveldb_readoptions_set_verify_checksums.restype = None
_ldb.leveldb_readoptions_set_fill_cache.argtypes = [ctypes.c_void_p,
        ctypes.c_ubyte]
_ldb.leveldb_readoptions_set_fill_cache.restype = None
_ldb.leveldb_readoptions_set_snapshot.argtypes = [ctypes.c_void_p,
        ctypes.c_void_p]
_ldb.leveldb_readoptions_set_snapshot.restype = None

_ldb.leveldb_create_iterator.argtypes = [ctypes.c_void_p, ctypes.c_void_p]
_ldb.leveldb_create_iterator.restype = ctypes.c_void_p
_ldb.leveldb_iter_destroy.argtypes = [ctypes.c_void_p]
_ldb.leveldb_iter_destroy.restype = None
_ldb.leveldb_iter_valid.argtypes = [ctypes.c_void_p]
_ldb.leveldb_iter_valid.restype = ctypes.c_bool
_ldb.leveldb_iter_key.argtypes = [ctypes.c_void_p,
        ctypes.POINTER(ctypes.c_size_t)]
_ldb.leveldb_iter_key.restype = ctypes.c_void_p
_ldb.leveldb_iter_value.argtypes = [ctypes.c_void_p,
        ctypes.POINTER(ctypes.c_size_t)]
_ldb.leveldb_iter_value.restype = ctypes.c_void_p
_ldb.leveldb_iter_next.argtypes = [ctypes.c_void_p]
_ldb.leveldb_iter_next.restype = None
_ldb.leveldb_iter_prev.argtypes = [ctypes.c_void_p]
_ldb.leveldb_iter_prev.restype = None
_ldb.leveldb_iter_seek_to_first.argtypes = [ctypes.c_void_p]
_ldb.leveldb_iter_seek_to_first.restype = None
_ldb.leveldb_iter_seek_to_last.argtypes = [ctypes.c_void_p]
_ldb.leveldb_iter_seek_to_last.restype = None
_ldb.leveldb_iter_seek.argtypes = [ctypes.c_void_p, ctypes.c_void_p,
        ctypes.c_size_t]
_ldb.leveldb_iter_seek.restype = None
_ldb.leveldb_iter_get_error.argtypes = [ctypes.c_void_p, ctypes.c_void_p]
_ldb.leveldb_iter_get_error.restype = None

_ldb.leveldb_writebatch_create.argtypes = []
_ldb.leveldb_writebatch_create.restype = ctypes.c_void_p
_ldb.leveldb_writebatch_destroy.argtypes = [ctypes.c_void_p]
_ldb.leveldb_writebatch_destroy.restype = None
_ldb.leveldb_writebatch_clear.argtypes = [ctypes.c_void_p]
_ldb.leveldb_writebatch_clear.restype = None

_ldb.leveldb_writebatch_put.argtypes = [ctypes.c_void_p, ctypes.c_void_p,
        ctypes.c_size_t, ctypes.c_void_p, ctypes.c_size_t]
_ldb.leveldb_writebatch_put.restype = None
_ldb.leveldb_writebatch_delete.argtypes = [ctypes.c_void_p, ctypes.c_void_p,
        ctypes.c_size_t]
_ldb.leveldb_writebatch_delete.restype = None

_ldb.leveldb_approximate_sizes.argtypes = [ctypes.c_void_p, ctypes.c_int,
        ctypes.c_void_p, ctypes.c_void_p, ctypes.c_void_p, ctypes.c_void_p,
        ctypes.c_void_p]
_ldb.leveldb_approximate_sizes.restype = None

_ldb.leveldb_compact_range.argtypes = [ctypes.c_void_p, ctypes.c_void_p,
        ctypes.c_size_t, ctypes.c_void_p, ctypes.c_size_t]
_ldb.leveldb_compact_range.restype = None

_ldb.leveldb_create_snapshot.argtypes = [ctypes.c_void_p]
_ldb.leveldb_create_snapshot.restype = ctypes.c_void_p
_ldb.leveldb_release_snapshot.argtypes = [ctypes.c_void_p, ctypes.c_void_p]
_ldb.leveldb_release_snapshot.restype = None

_ldb.leveldb_free.argtypes = [ctypes.c_void_p]
_ldb.leveldb_free.restype = None


Row = namedtuple('Row', 'key value')


class Error(Exception):
    pass


class Iterator(object):

    """This class is created by calling __iter__ or iterator on a DB interface
    """

    __slots__ = ["_prefix", "_impl", "_keys_only"]

    def __init__(self, impl, keys_only=False, prefix=None):
        self._impl = impl
        self._prefix = prefix
        self._keys_only = keys_only

    def valid(self):
        """Returns whether the iterator is valid or not

        @rtype: bool
        """
        valid = self._impl.valid()
        if not valid or self._prefix is None:
            return valid
        key = self._impl.key()
        return key[:len(self._prefix)] == self._prefix

    def seekFirst(self):
        """
        Jump to first key in database

        @return: self
        @rtype: Iter
        """
        if self._prefix is not None:
            self._impl.seek(self._prefix)
        else:
            self._impl.seekFirst()
        return self

    def seekLast(self):
        """
        Jump to last key in database

        @return: self
        @rtype: Iter
        """
        # if we have no prefix or the last possible prefix of this length, just
        # seek to the last key in the db.
        if self._prefix is None or self._prefix == "\xff" * len(self._prefix):
            self._impl.seekLast()
            return self

        # we have a prefix. see if there's anything after our prefix.
        # there's probably a much better way to calculate the next prefix.
        hex_prefix = self._prefix.encode('hex')
        next_prefix = hex(long(hex_prefix, 16) + 1)[2:].rstrip("L")
        next_prefix = next_prefix.rjust(len(hex_prefix), "0")
        next_prefix = next_prefix.decode("hex").rstrip("\x00")
        self._impl.seek(next_prefix)
        if self._impl.valid():
            # there is something after our prefix. we're on it, so step back
            self._impl.prev()
        else:
            # there is nothing after our prefix, just seek to the last key
            self._impl.seekLast()
        return self

    def seek(self, key):
        """Move the iterator to key. This may be called after StopIteration,
        allowing you to reuse an iterator safely.

        @param key: Where to position the iterator.
        @type key: str

        @return: self
        @rtype: Iter
        """
        if self._prefix is not None:
            key = self._prefix + key
        self._impl.seek(key)
        return self

    def key(self):
        """Returns the iterator's current key. You should be sure the iterator
        is currently valid first by calling valid()

        @rtype: string
        """
        key = self._impl.key()
        if self._prefix is not None:
            return key[len(self._prefix):]
        return key

    def value(self):
        """Returns the iterator's current value. You should be sure the
        iterator is currently valid first by calling valid()

        @rtype: string
        """
        return self._impl.val()

    def __iter__(self):
        return self

    def next(self):
        """Advances the iterator one step. Also returns the current value prior
        to moving the iterator

        @rtype: Row (namedtuple of key, value) if keys_only=False, otherwise
                string (the key)

        @raise StopIteration: if called on an iterator that is not valid
        """
        if not self.valid():
            raise StopIteration()
        if self._keys_only:
            rv = self.key()
        else:
            rv = Row(self.key(), self.value())
        self._impl.next()
        return rv

    def prev(self):
        """Backs the iterator up one step. Also returns the current value prior
        to moving the iterator.

        @rtype: Row (namedtuple of key, value) if keys_only=False, otherwise
                string (the key)

        @raise StopIteration: if called on an iterator that is not valid
        """
        if not self.valid():
            raise StopIteration()
        if self._keys_only:
            rv = self.key()
        else:
            rv = Row(self.key(), self.value())
        self._impl.prev()
        return rv

    def stepForward(self):
        """Same as next but does not return any data or check for validity"""
        self._impl.next()

    def stepBackward(self):
        """Same as prev but does not return any data or check for validity"""
        self._impl.prev()

    def range(self, start_key=None, end_key=None, start_inclusive=True,
            end_inclusive=False):
        """A generator for some range of rows"""
        if start_key is not None:
            self.seek(start_key)
            if not start_inclusive and self.key() == start_key:
                self._impl.next()
        else:
            self.seekFirst()
        for row in self:
            if end_key is not None and (row.key > end_key or (
                    not end_inclusive and row.key == end_key)):
                break
            yield row

    def keys(self):
        while self.valid():
            yield self.key()
            self.stepForward()

    def values(self):
        while self.valid():
            yield self.value()
            self.stepForward()

    def close(self):
        self._impl.close()


class _OpaqueWriteBatch(object):

    """This is an opaque write batch that must be written to using the putTo
    and deleteFrom methods on DBInterface.
    """

    def __init__(self):
        self._puts = {}
        self._deletes = set()
        self._private = True

    def clear(self):
        self._puts = {}
        self._deletes = set()


class WriteBatch(_OpaqueWriteBatch):

    """This class is created stand-alone, but then written to some existing
    DBInterface
    """

    def __init__(self):
        _OpaqueWriteBatch.__init__(self)
        self._private = False

    def put(self, key, val):
        self._deletes.discard(key)
        self._puts[key] = val

    def delete(self, key):
        self._puts.pop(key, None)
        self._deletes.add(key)


class DBInterface(object):

    """This class is created through a few different means:

    Initially, it can be created using either the DB() or MemoryDB()
    module-level methods. In almost every case, you want the DB() method.

    You can then get new DBInterfaces from an existing DBInterface by calling
    snapshot or scope.
    """

    __slots__ = ["_impl", "_prefix", "_allow_close", "_default_sync",
                 "_default_verify_checksums", "_default_fill_cache"]

    def __init__(self, impl, prefix=None, allow_close=False,
                 default_sync=False, default_verify_checksums=False,
                 default_fill_cache=True):
        self._impl = impl
        self._prefix = prefix
        self._allow_close = allow_close
        self._default_sync = default_sync
        self._default_verify_checksums = default_verify_checksums
        self._default_fill_cache = default_fill_cache

    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc_value, traceback):
        self.close()

    def close(self):
        if self._allow_close:
            self._impl.close()

    def newBatch(self):
        return _OpaqueWriteBatch()

    def put(self, key, val, sync=None):
        if sync is None:
            sync = self._default_sync
        if self._prefix is not None:
            key = self._prefix + key
        self._impl.put(key, val, sync=sync)

    # pylint: disable=W0212
    def putTo(self, batch, key, val):
        if not batch._private:
            raise ValueError("batch not from DBInterface.newBatch")
        if self._prefix is not None:
            key = self._prefix + key
        batch._deletes.discard(key)
        batch._puts[key] = val

    def delete(self, key, sync=None):
        if sync is None:
            sync = self._default_sync
        if self._prefix is not None:
            key = self._prefix + key
        self._impl.delete(key, sync=sync)

    # pylint: disable=W0212
    def deleteFrom(self, batch, key):
        if not batch._private:
            raise ValueError("batch not from DBInterface.newBatch")
        if self._prefix is not None:
            key = self._prefix + key
        batch._puts.pop(key, None)
        batch._deletes.add(key)

    def get(self, key, verify_checksums=None, fill_cache=None):
        if verify_checksums is None:
            verify_checksums = self._default_verify_checksums
        if fill_cache is None:
            fill_cache = self._default_fill_cache
        if self._prefix is not None:
            key = self._prefix + key
        return self._impl.get(key, verify_checksums=verify_checksums,
                fill_cache=fill_cache)

    # pylint: disable=W0212
    def write(self, batch, sync=None):
        if sync is None:
            sync = self._default_sync
        if self._prefix is not None and not batch._private:
            unscoped_batch = _OpaqueWriteBatch()
            for key, value in batch._puts.iteritems():
                unscoped_batch._puts[self._prefix + key] = value
            for key in batch._deletes:
                unscoped_batch._deletes.add(self._prefix + key)
            batch = unscoped_batch
        return self._impl.write(batch, sync=sync)

    def iterator(self, verify_checksums=None, fill_cache=None, prefix=None,
                 keys_only=False):
        if verify_checksums is None:
            verify_checksums = self._default_verify_checksums
        if fill_cache is None:
            fill_cache = self._default_fill_cache
        if self._prefix is not None:
            if prefix is None:
                prefix = self._prefix
            else:
                prefix = self._prefix + prefix
        return Iterator(
                self._impl.iterator(verify_checksums=verify_checksums,
                                    fill_cache=fill_cache),
                keys_only=keys_only, prefix=prefix)

    def snapshot(self, default_sync=None, default_verify_checksums=None,
                 default_fill_cache=None):
        if default_sync is None:
            default_sync = self._default_sync
        if default_verify_checksums is None:
            default_verify_checksums = self._default_verify_checksums
        if default_fill_cache is None:
            default_fill_cache = self._default_fill_cache
        return DBInterface(self._impl.snapshot(), prefix=self._prefix,
                allow_close=False, default_sync=default_sync,
                default_verify_checksums=default_verify_checksums,
                default_fill_cache=default_fill_cache)

    def __iter__(self):
        return self.iterator().seekFirst()

    def __getitem__(self, k):
        v = self.get(k)
        if v is None:
            raise KeyError(k)
        return v

    def __setitem__(self, k, v):
        self.put(k, v)

    def __delitem__(self, k):
        self.delete(k)

    def __contains__(self, key):
        return self.has(key)

    def has(self, key, verify_checksums=None, fill_cache=None):
        if verify_checksums is None:
            verify_checksums = self._default_verify_checksums
        if fill_cache is None:
            fill_cache = self._default_fill_cache
        it = self.iterator(verify_checksums=verify_checksums,
                           fill_cache=fill_cache).seek(key)
        return it.valid() and it.key() == key

    def scope(self, prefix, default_sync=None, default_verify_checksums=None,
                 default_fill_cache=None):
        if default_sync is None:
            default_sync = self._default_sync
        if default_verify_checksums is None:
            default_verify_checksums = self._default_verify_checksums
        if default_fill_cache is None:
            default_fill_cache = self._default_fill_cache
        if self._prefix is not None:
            prefix = self._prefix + prefix
        return DBInterface(self._impl, prefix=prefix, allow_close=False,
                default_sync=default_sync,
                default_verify_checksums=default_verify_checksums,
                default_fill_cache=default_fill_cache)

    def range(self, start_key=None, end_key=None, start_inclusive=True,
            end_inclusive=False, verify_checksums=None, fill_cache=None):
        if verify_checksums is None:
            verify_checksums = self._default_verify_checksums
        if fill_cache is None:
            fill_cache = self._default_fill_cache
        return self.iterator(verify_checksums=verify_checksums,
                fill_cache=fill_cache).range(start_key=start_key,
                        end_key=end_key, start_inclusive=start_inclusive,
                        end_inclusive=end_inclusive)

    def keys(self, verify_checksums=None, fill_cache=None, prefix=None):
        if verify_checksums is None:
            verify_checksums = self._default_verify_checksums
        if fill_cache is None:
            fill_cache = self._default_fill_cache
        return self.iterator(verify_checksums=verify_checksums,
                fill_cache=fill_cache, prefix=prefix).seekFirst().keys()

    def values(self, verify_checksums=None, fill_cache=None, prefix=None):
        if verify_checksums is None:
            verify_checksums = self._default_verify_checksums
        if fill_cache is None:
            fill_cache = self._default_fill_cache
        return self.iterator(verify_checksums=verify_checksums,
                fill_cache=fill_cache, prefix=prefix).seekFirst().values()

    def approximateDiskSizes(self, *ranges):
        return self._impl.approximateDiskSizes(*ranges)

    def compactRange(self, start_key, end_key):
        return self._impl.compactRange(start_key, end_key)


def MemoryDB(*_args, **kwargs):
    """This is primarily for unit testing. If you are doing anything serious,
    you definitely are more interested in the standard DB class.

    Arguments are ignored.

    TODO: if the LevelDB C api ever allows for other environments, actually
          use LevelDB code for this, instead of reimplementing it all in
          Python.
    """
    assert kwargs.get("create_if_missing", True)
    return DBInterface(_MemoryDBImpl(), allow_close=True)


class _IteratorMemImpl(object):

    __slots__ = ["_data", "_idx"]

    def __init__(self, memdb_data):
        self._data = memdb_data
        self._idx = -1

    def valid(self):
        return 0 <= self._idx < len(self._data)

    def key(self):
        return self._data[self._idx][0]

    def val(self):
        return self._data[self._idx][1]

    def seek(self, key):
        self._idx = bisect.bisect_left(self._data, (key, ""))

    def seekFirst(self):
        self._idx = 0

    def seekLast(self):
        self._idx = len(self._data) - 1

    def prev(self):
        self._idx -= 1

    def next(self):
        self._idx += 1

    def close(self):
      self._data = []
      self._idx = -1


class _MemoryDBImpl(object):

    __slots__ = ["_data", "_lock", "_is_snapshot"]

    def __init__(self, data=None, is_snapshot=False):
        if data is None:
            self._data = []
        else:
            self._data = data
        self._lock = threading.RLock()
        self._is_snapshot = is_snapshot

    def close(self):
        with self._lock:
            self._data = []

    def put(self, key, val, **_kwargs):
        if self._is_snapshot:
            raise TypeError("cannot put on leveldb snapshot")
        assert isinstance(key, str)
        assert isinstance(val, str)
        with self._lock:
            idx = bisect.bisect_left(self._data, (key, ""))
            if 0 <= idx < len(self._data) and self._data[idx][0] == key:
                self._data[idx] = (key, val)
            else:
                self._data.insert(idx, (key, val))

    def delete(self, key, **_kwargs):
        if self._is_snapshot:
            raise TypeError("cannot delete on leveldb snapshot")
        with self._lock:
            idx = bisect.bisect_left(self._data, (key, ""))
            if 0 <= idx < len(self._data) and self._data[idx][0] == key:
                del self._data[idx]

    def get(self, key, **_kwargs):
        with self._lock:
            idx = bisect.bisect_left(self._data, (key, ""))
            if 0 <= idx < len(self._data) and self._data[idx][0] == key:
                return self._data[idx][1]
            return None

    # pylint: disable=W0212
    def write(self, batch, **_kwargs):
        if self._is_snapshot:
            raise TypeError("cannot write on leveldb snapshot")
        with self._lock:
            for key, val in batch._puts.iteritems():
                self.put(key, val)
            for key in batch._deletes:
                self.delete(key)

    def iterator(self, **_kwargs):
        # WARNING: huge performance hit.
        # leveldb iterators are actually lightweight snapshots of the data. in
        # real leveldb, an iterator won't change its idea of the full database
        # even if puts or deletes happen while the iterator is in use. to
        # simulate this, there isn't anything simple we can do for now besides
        # just copy the whole thing.
        with self._lock:
            return _IteratorMemImpl(self._data[:])

    def approximateDiskSizes(self, *ranges):
        if self._is_snapshot:
            raise TypeError("cannot calculate disk sizes on leveldb snapshot")
        return [0] * len(ranges)

    def compactRange(self, start_key, end_key):
        pass

    def snapshot(self):
        if self._is_snapshot:
            return self
        with self._lock:
            return _MemoryDBImpl(data=self._data[:], is_snapshot=True)


class _PointerRef(object):

    __slots__ = ["ref", "_close", "_referrers", "__weakref__"]

    def __init__(self, ref, close_cb):
        self.ref = ref
        self._close = close_cb
        self._referrers = weakref.WeakValueDictionary()

    def addReferrer(self, referrer):
        self._referrers[id(referrer)] = referrer

    def close(self):
        ref, self.ref = self.ref, None
        close, self._close = self._close, None
        referrers = self._referrers
        self._referrers = weakref.WeakValueDictionary()
        for referrer in referrers.valuerefs():
            referrer = referrer()
            if referrer is not None:
                referrer.close()
        if ref is not None and close is not None:
            close(ref)

    __del__ = close


def _checkError(error):
    if bool(error):
        message = ctypes.string_at(error)
        _ldb.leveldb_free(ctypes.cast(error, ctypes.c_void_p))
        raise Error(message)


class _IteratorDbImpl(object):

    __slots__ = ["_ref"]

    def __init__(self, iterator_ref):
        self._ref = iterator_ref

    def valid(self):
        return _ldb.leveldb_iter_valid(self._ref.ref)

    def key(self):
        length = ctypes.c_size_t(0)
        val_p = _ldb.leveldb_iter_key(self._ref.ref, ctypes.byref(length))
        assert bool(val_p)
        return ctypes.string_at(val_p, length.value)

    def val(self):
        length = ctypes.c_size_t(0)
        val_p = _ldb.leveldb_iter_value(self._ref.ref, ctypes.byref(length))
        assert bool(val_p)
        return ctypes.string_at(val_p, length.value)

    def seek(self, key):
        _ldb.leveldb_iter_seek(self._ref.ref, key, len(key))
        self._checkError()

    def seekFirst(self):
        _ldb.leveldb_iter_seek_to_first(self._ref.ref)
        self._checkError()

    def seekLast(self):
        _ldb.leveldb_iter_seek_to_last(self._ref.ref)
        self._checkError()

    def prev(self):
        _ldb.leveldb_iter_prev(self._ref.ref)
        self._checkError()

    def next(self):
        _ldb.leveldb_iter_next(self._ref.ref)
        self._checkError()

    def _checkError(self):
        error = ctypes.POINTER(ctypes.c_char)()
        _ldb.leveldb_iter_get_error(self._ref.ref, ctypes.byref(error))
        _checkError(error)

    def close(self):
      self._ref.close()


def DB(path, bloom_filter_size=10, create_if_missing=False,
       error_if_exists=False, paranoid_checks=False,
       write_buffer_size=(4 * 1024 * 1024), max_open_files=1000,
       block_cache_size=(8 * 1024 * 1024), block_size=(4 * 1024),
       default_sync=False, default_verify_checksums=False,
       default_fill_cache=True):
    """This is the expected way to open a database. Returns a DBInterface.
    """

    filter_policy = _PointerRef(
            _ldb.leveldb_filterpolicy_create_bloom(bloom_filter_size),
            _ldb.leveldb_filterpolicy_destroy)
    cache = _PointerRef(
            _ldb.leveldb_cache_create_lru(block_cache_size),
            _ldb.leveldb_cache_destroy)

    options = _ldb.leveldb_options_create()
    _ldb.leveldb_options_set_filter_policy(
            options, filter_policy.ref)
    _ldb.leveldb_options_set_create_if_missing(options, create_if_missing)
    _ldb.leveldb_options_set_error_if_exists(options, error_if_exists)
    _ldb.leveldb_options_set_paranoid_checks(options, paranoid_checks)
    _ldb.leveldb_options_set_write_buffer_size(options, write_buffer_size)
    _ldb.leveldb_options_set_max_open_files(options, max_open_files)
    _ldb.leveldb_options_set_cache(options, cache.ref)
    _ldb.leveldb_options_set_block_size(options, block_size)

    error = ctypes.POINTER(ctypes.c_char)()
    db = _ldb.leveldb_open(options, path, ctypes.byref(error))
    _ldb.leveldb_options_destroy(options)
    _checkError(error)

    db = _PointerRef(db, _ldb.leveldb_close)
    filter_policy.addReferrer(db)
    cache.addReferrer(db)

    return DBInterface(_LevelDBImpl(db, other_objects=(filter_policy, cache)),
                       allow_close=True, default_sync=default_sync,
                       default_verify_checksums=default_verify_checksums,
                       default_fill_cache=default_fill_cache)


class _LevelDBImpl(object):

    __slots__ = ["_objs", "_db", "_snapshot"]

    def __init__(self, db_ref, snapshot_ref=None, other_objects=()):
        self._objs = other_objects
        self._db = db_ref
        self._snapshot = snapshot_ref

    def close(self):
        db, self._db = self._db, None
        objs, self._objs = self._objs, ()
        if db is not None:
            db.close()
        for obj in objs:
            obj.close()

    def put(self, key, val, sync=False):
        if self._snapshot is not None:
            raise TypeError("cannot put on leveldb snapshot")
        error = ctypes.POINTER(ctypes.c_char)()
        options = _ldb.leveldb_writeoptions_create()
        _ldb.leveldb_writeoptions_set_sync(options, sync)
        _ldb.leveldb_put(self._db.ref, options, key, len(key), val, len(val),
                ctypes.byref(error))
        _ldb.leveldb_writeoptions_destroy(options)
        _checkError(error)

    def delete(self, key, sync=False):
        if self._snapshot is not None:
            raise TypeError("cannot delete on leveldb snapshot")
        error = ctypes.POINTER(ctypes.c_char)()
        options = _ldb.leveldb_writeoptions_create()
        _ldb.leveldb_writeoptions_set_sync(options, sync)
        _ldb.leveldb_delete(self._db.ref, options, key, len(key),
                ctypes.byref(error))
        _ldb.leveldb_writeoptions_destroy(options)
        _checkError(error)

    def get(self, key, verify_checksums=False, fill_cache=True):
        error = ctypes.POINTER(ctypes.c_char)()
        options = _ldb.leveldb_readoptions_create()
        _ldb.leveldb_readoptions_set_verify_checksums(options,
                verify_checksums)
        _ldb.leveldb_readoptions_set_fill_cache(options, fill_cache)
        if self._snapshot is not None:
            _ldb.leveldb_readoptions_set_snapshot(options, self._snapshot.ref)
        size = ctypes.c_size_t(0)
        val_p = _ldb.leveldb_get(self._db.ref, options, key, len(key),
                ctypes.byref(size), ctypes.byref(error))
        if bool(val_p):
            val = ctypes.string_at(val_p, size.value)
            _ldb.leveldb_free(ctypes.cast(val_p, ctypes.c_void_p))
        else:
            val = None
        _ldb.leveldb_readoptions_destroy(options)
        _checkError(error)
        return val

    # pylint: disable=W0212
    def write(self, batch, sync=False):
        if self._snapshot is not None:
            raise TypeError("cannot delete on leveldb snapshot")
        real_batch = _ldb.leveldb_writebatch_create()
        for key, val in batch._puts.iteritems():
            _ldb.leveldb_writebatch_put(real_batch, key, len(key), val,
                    len(val))
        for key in batch._deletes:
            _ldb.leveldb_writebatch_delete(real_batch, key, len(key))
        error = ctypes.POINTER(ctypes.c_char)()
        options = _ldb.leveldb_writeoptions_create()
        _ldb.leveldb_writeoptions_set_sync(options, sync)
        _ldb.leveldb_write(self._db.ref, options, real_batch,
                ctypes.byref(error))
        _ldb.leveldb_writeoptions_destroy(options)
        _ldb.leveldb_writebatch_destroy(real_batch)
        _checkError(error)

    def iterator(self, verify_checksums=False, fill_cache=True):
        options = _ldb.leveldb_readoptions_create()
        if self._snapshot is not None:
            _ldb.leveldb_readoptions_set_snapshot(options, self._snapshot.ref)
        _ldb.leveldb_readoptions_set_verify_checksums(
                options, verify_checksums)
        _ldb.leveldb_readoptions_set_fill_cache(options, fill_cache)
        it_ref = _PointerRef(
                _ldb.leveldb_create_iterator(self._db.ref, options),
                _ldb.leveldb_iter_destroy)
        _ldb.leveldb_readoptions_destroy(options)
        self._db.addReferrer(it_ref)
        return _IteratorDbImpl(it_ref)

    def approximateDiskSizes(self, *ranges):
        if self._snapshot is not None:
            raise TypeError("cannot calculate disk sizes on leveldb snapshot")
        assert len(ranges) > 0
        key_type = ctypes.c_void_p * len(ranges)
        len_type = ctypes.c_size_t * len(ranges)
        start_keys, start_lens = key_type(), len_type()
        end_keys, end_lens = key_type(), len_type()
        sizes = (ctypes.c_uint64 * len(ranges))()
        for i, range_ in enumerate(ranges):
            assert isinstance(range_, tuple) and len(range_) == 2
            assert isinstance(range_[0], str) and isinstance(range_[1], str)
            start_keys[i] = ctypes.cast(range_[0], ctypes.c_void_p)
            end_keys[i] = ctypes.cast(range_[1], ctypes.c_void_p)
            start_lens[i], end_lens[i] = len(range_[0]), len(range_[1])
        _ldb.leveldb_approximate_sizes(self._db.ref, len(ranges), start_keys,
                start_lens, end_keys, end_lens, sizes)
        return list(sizes)

    def compactRange(self, start_key, end_key):
        assert isinstance(start_key, str) and isinstance(end_key, str)
        _ldb.leveldb_compact_range(self._db.ref, start_key, len(start_key),
                end_key, len(end_key))

    def snapshot(self):
        snapshot_ref = _PointerRef(
                _ldb.leveldb_create_snapshot(self._db.ref),
                lambda ref: _ldb.leveldb_release_snapshot(self._db.ref, ref))
        self._db.addReferrer(snapshot_ref)
        return _LevelDBImpl(self._db, snapshot_ref=snapshot_ref,
                            other_objects=self._objs)