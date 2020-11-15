package org.embulk.output.s3v2.util;

public class ChunksizeComputation extends AbstractUnitComputation
{
    private ChunksizeComputation()
    {
        // Do nothing.
    }

    public static int getChunksizeBytes(String chunksize)
    {
        if (!validateValue(chunksize)) {
            throw new IllegalArgumentException("Unrecognized value of multipart_chunksize: " + chunksize);
        }

        String sizePartOfChunksize = getSize(chunksize);
        String unitPartOfChunksize = getUnit(chunksize);
        ComputeUnits e = ComputeUnits.valueOf(unitPartOfChunksize.toUpperCase());
        double chunksizeValue = (Double.valueOf(sizePartOfChunksize) * e.getUnit());
        if (chunksizeValue < 5.0 * ComputeUnits.MB.getUnit() || 2.0 * ComputeUnits.GB.getUnit() < chunksizeValue) {
            throw new IllegalArgumentException("Unrecognized range of value multipart_chunksize: " + chunksize);
        }

        return (int) chunksizeValue;
    }
}
