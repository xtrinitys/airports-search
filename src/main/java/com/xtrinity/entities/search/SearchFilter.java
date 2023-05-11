package com.xtrinity.entities.search;


public class SearchFilter {
    private Integer filterIndex;
    private Integer column;
    private String sign;
    private Number numValue = null;
    private String strValue = null;

    public Integer getColumn() {
        return column;
    }

    public SearchFilter(Integer filterIndex) {
        this.filterIndex = filterIndex;
    }

    @Override
    public String toString() {
        return "SearchFilter{" +
                "filterIndex=" + filterIndex +
                ", column=" + column +
                ", sign='" + sign + '\'' +
                ", numValue=" + numValue +
                ", strValue='" + strValue + '\'' +
                '}';
    }

    public void setColumn(Integer column) {
        this.column = column;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public Number getNumValue() {
        return numValue;
    }

    public void setNumValue(Number numValue) {
        this.numValue = numValue;
    }

    public String getStrValue() {
        return strValue;
    }

    public void setStrValue(String strValue) {
        this.strValue = strValue;
    }

    public Integer getFilterIndex() {
        return filterIndex;
    }

    public void setFilterIndex(Integer filterIndex) {
        this.filterIndex = filterIndex;
    }
}
