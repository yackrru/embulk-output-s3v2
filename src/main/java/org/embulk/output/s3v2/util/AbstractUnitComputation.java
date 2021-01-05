package org.embulk.output.s3v2.util;

abstract class AbstractUnitComputation
{
    protected enum ComputeUnits
    {
        KB(Math.pow(10, 3)),
        MB(Math.pow(10, 6)),
        GB(Math.pow(10, 9)),
        TB(Math.pow(10, 12));

        private final double unit;

        private ComputeUnits(double unit)
        {
            this.unit = unit;
        }

        public double getUnit()
        {
            return unit;
        }
    }

    protected static String getSize(String value)
    {
        return value.replaceAll("[^0-9]", "");
    }

    protected static String getUnit(String value)
    {
        return value.replaceAll("[0-9]", "");
    }

    protected static boolean validateValue(String value)
    {
        return value.matches(getSize(value) + getUnit(value));
    }
}
