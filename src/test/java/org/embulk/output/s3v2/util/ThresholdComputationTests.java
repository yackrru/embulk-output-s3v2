package org.embulk.output.s3v2.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.ByteBuffer;

/**
 * @see ThresholdComputation
 */
@ExtendWith(MockitoExtension.class)
public class ThresholdComputationTests
{
    @Test
    @DisplayName("Threshold check returns true")
    public void testThresholdCheckReturnsTrue() throws Exception
    {
        ByteBuffer bf = ByteBuffer.allocate(1000001);
        Assertions.assertTrue(ThresholdComputation.largerThanThreshold("1MB", bf.capacity()));
    }

    @Test
    @DisplayName("Threshold check returns false")
    public void testThresholdCheckReturnsFalse() throws Exception
    {
        ByteBuffer bf = ByteBuffer.allocate(1000000);
        Assertions.assertFalse(ThresholdComputation.largerThanThreshold("1MB", bf.capacity()));
    }

    @Test
    @DisplayName("Invalid threshold string")
    public void testInvalidThresholdString() throws Exception
    {
        ByteBuffer bf = ByteBuffer.allocate(1000000);
        String threshold = "1MB1MB";
        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class,
                () -> ThresholdComputation.largerThanThreshold(threshold, bf.capacity()));
        Assertions.assertEquals("Unrecognized value of multipart_threshold: " + threshold, ex.getMessage());
    }
}
