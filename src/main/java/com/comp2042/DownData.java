package com.comp2042;

public final class DownData {
    private final RowClearResult rowClearResult;
    private final ViewData viewData;

    public DownData(RowClearResult rowClearResult, ViewData viewData) {
        this.rowClearResult = rowClearResult;
        this.viewData = viewData;
    }

    public RowClearResult getClearRow() {
        return rowClearResult;
    }

    public ViewData getViewData() {
        return viewData;
    }
}
