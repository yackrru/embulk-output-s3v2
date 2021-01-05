package org.embulk.output.s3v2.strategy;

import org.embulk.output.s3v2.PluginTask;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @see BufferedStrategy
 */
@ExtendWith(MockitoExtension.class)
public class BufferedStrategyTests
{
    private PluginTask task;

    @BeforeEach
    public void setUp()
    {
        task = Mockito.mock(PluginTask.class);
        Mockito.doReturn("ap-northeast-1").when(task).getRegion();
    }

    @Test
    @DisplayName("Test validate true")
    public void testValidateTrue() throws Exception
    {
        Mockito.doReturn(false).when(task).getEnableMultiPartUpload();
        BufferedStrategy output = new BufferedStrategy(task, 0);
        Assertions.assertTrue(output.validate());
    }

    @Test
    @DisplayName("Test validate false")
    public void testValidateInvalidCase() throws Exception
    {
        Mockito.doReturn(true).when(task).getEnableMultiPartUpload();
        UnsupportedOperationException ex = Assertions.assertThrows(UnsupportedOperationException.class,
                () -> new BufferedStrategy(task, 0));
        Assertions.assertEquals("Buffering strategy does not support S3 multi-part upload.", ex.getMessage());
    }
}
