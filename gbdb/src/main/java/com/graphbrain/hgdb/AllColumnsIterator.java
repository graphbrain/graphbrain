package com.graphbrain.hgdb;


import java.util.Iterator;
import java.util.List;

import me.prettyprint.hector.api.beans.ColumnSlice;
import me.prettyprint.hector.api.beans.HColumn;

import me.prettyprint.hector.api.exceptions.HectorException;
import me.prettyprint.hector.api.query.SliceQuery;


public class AllColumnsIterator<N, V> implements Iterator<HColumn<N, V>> {
    private N start;
    private int count;
    Iterator<HColumn<N, V>> columnsIterator;
    SliceQuery<?, N, V> query;
    private boolean isLastIteration;

    public AllColumnsIterator(SliceQuery<?, N, V> query) {
        start = null;
        count = 100;
        columnsIterator = null;
        this.query = query;
        isLastIteration = false;
    }

    public Iterator<HColumn<N, V>> iterator() {
        return this;
    }

    public boolean hasNext() {
        if (columnsIterator == null || !columnsIterator.hasNext()) {
            if (isLastIteration)
                return false;

            if (!fetchMore())
                return false;
        }
        return true;
    }

    public HColumn<N, V> next() {
        return columnsIterator.next();
    }

    private boolean fetchMore() {
        try {
            query.setRange(start, null, false, count);
            ColumnSlice<N, V> slice = query.execute().get();
            List<HColumn<N, V>> columns = slice.getColumns();
            int origSize = columns.size();

            if (origSize == 0) {
                return false;
            }

            if (origSize >= count)
                start = columns.remove(columns.size()-1).getName();

            columnsIterator = columns.iterator();

            if (origSize < count)
                isLastIteration = true;

            return true;
        } catch (HectorException e) {
            return false;
        }
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}