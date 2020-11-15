package org.embulk.output.s3v2.util;

public class ThresholdComputation extends AbstractUnitComputation
{
    private ThresholdComputation()
    {
        // Do nothing.
    }

    public static boolean largerThanThreshold(String threshold, long computationTargetSize)
    {
        if (!validateValue(threshold)) {
            throw new IllegalArgumentException("Unrecognized value of multipart_threshold: " + threshold);
        }

        String thresholdSize = getSize(threshold);
        String thresholdUnit = getUnit(threshold);
        ComputeUnits e = ComputeUnits.valueOf(thresholdUnit.toUpperCase());
        double thresholdValue = (Double.valueOf(thresholdSize) * e.getUnit());

        return (double) computationTargetSize > thresholdValue;
    }
}
