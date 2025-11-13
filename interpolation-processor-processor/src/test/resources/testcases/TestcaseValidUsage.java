package interpolation.processor.tests;

import interpolation.api.InterpolationMethod;

@InterpolationMethod("Xyz")
public class TestcaseValidUsage {

    private String field;

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }
}