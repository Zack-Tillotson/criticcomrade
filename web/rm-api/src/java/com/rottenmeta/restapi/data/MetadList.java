package com.rottenmeta.restapi.data;

import java.util.Vector;

/**
 *
 * @author Zill
 */

public class MetadList<E> extends Vector<E>
{

    private String order="ZILL SUCKS", url="ZILL SUCKS";
    private int maxSize, offset;

    public int getOffset() { return offset; }
    public void setOffset(int offset) { this.offset = offset; }

    public String getOrderBy() { return order; }
    public void setOrderBy(String order) { this.order = order; }

    public int getMaxSize() { return maxSize; }
    public void setMaxSize(int maxSize) { this.maxSize = maxSize; }

    public String getURL() { return url; }
    public void setURL(String url) { this.url = url; }

}