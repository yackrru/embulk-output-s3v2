package org.embulk.output.s3v2.s3;

public class S3MultiPartStatus
{
    private final int maxConcurrentRequests;
    private final String multipartChunksize;
    private final String multipartThreshold;

    public S3MultiPartStatus(int maxConcurrentRequests, String multipartChunksize, String multipartThreshold)
    {
        this.maxConcurrentRequests = maxConcurrentRequests;
        this.multipartChunksize = multipartChunksize;
        this.multipartThreshold = multipartThreshold;
    }

    public int getMaxConcurrentRequests()
    {
        return maxConcurrentRequests;
    }

    public String getMultipartChunksize()
    {
        return multipartChunksize;
    }

    public String getMultipartThreshold()
    {
        return multipartThreshold;
    }
}
