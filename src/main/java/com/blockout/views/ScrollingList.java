package com.blockout.views;

import com.blockout.Loader;
import com.blockout.Pane;
import com.blockout.PaneParams;

import java.util.List;

/*
 * A ScrollingList is a View which can contain 0 or more children of a specific Pane or View type
 * and are ordered sequentially
 *
 * All children are set to a Top version of their alignment, and have their Y coordinates overwritten
 */
public class ScrollingList extends ScrollingView
{
    public static interface DataProvider
    {
        /**
         * Override this to provide the number of rows
         *
         * @return number of rows in the list
         */
        int getElementCount();

        /**
         * Override this to update the Panes for a given row
         *
         * @param index the index of the row/list element
         * @param rowPane the parent Pane for the row, containing the elements to update
         */
        void updateElement(int index, Pane rowPane);
    }

    //  Runtime
    protected DataProvider dataProvider;
    private PaneParams     listNodeParams;

    public ScrollingList() { super(); }

    /**
     * Constructs a ScrollingList from PaneParams
     *
     * @param params Params for the ScrollingList
     */
    public ScrollingList(PaneParams params)
    {
        super(params);
    }

    public void setDataProvider(DataProvider p)
    {
        dataProvider = p;
        refreshElementPanes();
    }

    public void refreshElementPanes()
    {
        ((ScrollingListContainer)container).refreshElementPanes(dataProvider, listNodeParams);
    }

    @Override
    protected ScrollingContainer createScrollingContainer()
    {
        return new ScrollingListContainer(this);
    }

    @Override
    public void parseChildren(PaneParams params)
    {
        List<PaneParams> childNodes = params.getChildren();
        if (childNodes == null) return;

        //  Get the PaneParams for this child, because we'll need it in the future
        //  to create more nodes
        listNodeParams = childNodes.get(0);
    }

    public int getListElementIndexByPane(Pane pane)
    {
        return ((ScrollingListContainer)container).getListElementIndexByPane(pane);
    }
}