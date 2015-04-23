package org.jprelude.csv.base;

public class CsvField {
    private final String val;
    
    public CsvField(final String value) {
        this.val = value;
    }

    public boolean isNull() {
        return this.val == null;
    }

    public boolean isNotNull() {
        return this.val != null;
    }

    public boolean hasLength(final int n) {
        return (this.val == null && n == 0)
                || (this.val != null && this.val.length() == n);
    }

    public boolean isGreater(final float x) {
        return !this.isNull() && this.isFloat() && Float.parseFloat(this.val) > x;
    }

    public boolean isInteger() {
        boolean ret;

        try {
            Integer.parseInt(this.val);
            ret = true;
        } catch (final NumberFormatException e) {
            ret = false;
        }

        return ret;
    }

    public boolean isFloat() {
        boolean ret;

        try {
            Float.parseFloat(this.val);
            ret = true;
        } catch (final NumberFormatException e) {
            ret = false;
        }

        return ret;
    }

    public boolean isDouble() {
        boolean ret;

        try {
            Double.parseDouble(this.val);
            ret = true;
        } catch (final NumberFormatException e) {
            ret = false;
        }

        return ret;
    }
}
