package org.embulk.output.s3v2.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @see ChunksizeComputation
 */
@ExtendWith(MockitoExtension.class)
public class ChunksizeComputationTests
{
    @Test
    @DisplayName("[Correct] Min chunksize")
    public void testMinChunksizeCorrect() throws Exception
    {
        Assertions.assertEquals(5 * Math.pow(10, 6), ChunksizeComputation.getChunksizeBytes("5MB"));
    }

    @Test
    @DisplayName("[Invalid] Lower than min chunksize")
    public void testLowerThanMinChunksizeInvalid() throws Exception
    {
        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class,
                () -> ChunksizeComputation.getChunksizeBytes("1MB"));
        Assertions.assertEquals("Unrecognized range of value multipart_chunksize: 1MB", ex.getMessage());
    }

    @Test
    @DisplayName("[Correct] Max chunksize")
    public void testMaxChunksizeCorrect() throws Exception
    {
        Assertions.assertEquals(2.0 * Math.pow(10, 9), ChunksizeComputation.getChunksizeBytes("2GB"));
    }

    @Test
    @DisplayName("[Invalid] Higher than max chunksize")
    public void testHigherThanMaxChunksizeInvalid() throws Exception
    {
        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class,
                () -> ChunksizeComputation.getChunksizeBytes("1TB"));
        Assertions.assertEquals("Unrecognized range of value multipart_chunksize: 1TB", ex.getMessage());
    }

    @Test
    @DisplayName("[Correct] Any chunksize")
    public void testAnyChunksizeCorrect() throws Exception
    {
        Assertions.assertEquals(1060 * Math.pow(10, 6), ChunksizeComputation.getChunksizeBytes("1060MB"));
    }
}
