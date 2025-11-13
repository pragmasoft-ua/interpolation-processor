package interpolation.processor.tests;

import interpolation.api.InterpolationMethod;

@InterpolationMethod("Xyz")
public class TestcaseInvalidUsageNoNoargConstructor {

    private String field;

    public  TestcaseInvalidUsageNoNoargConstructor (String arg) {
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }
}