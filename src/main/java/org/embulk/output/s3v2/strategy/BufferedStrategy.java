package org.embulk.output.s3v2.strategy;

import org.embulk.config.TaskReport;
import org.embulk.output.s3v2.PluginTask;
import org.embulk.spi.Buffer;
import org.embulk.spi.Exec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

public class BufferedStrategy extends AbstractStrategy
{
    private final Logger logger = LoggerFactory.getLogger(BufferedStrategy.class);
    private final String bucket;
    private String s3ObjectKey;
    private ByteBuffer byteBuffer;

    public BufferedStrategy(PluginTask task, int taskIndex)
    {
        super(task, taskIndex);
        this.bucket = task.getBucket();
    }

    @Override
    protected boolean validate()
    {
        if (task.getEnableMultiPartUpload()) {
            throw new UnsupportedOperationException("Buffering strategy does not support S3 multi-part upload.");
        }
        return true;
    }

    @Override
    public void nextFile()
    {
        String ext = getFileExtension();
        s3ObjectKey = task.getObjectKeyPrefix() + "-" + taskIndex + ext;
        logger.info("[task:" + taskIndex + "] Temporary file is not created.");
    }

    @Override
    public void add(Buffer buffer)
    {
        byteBuffer = ByteBuffer.wrap(buffer.array(), buffer.offset(), buffer.limit());
    }

    @Override
    public void finish()
    {
        // Do nothing.
    }

    @Override
    public void close()
    {
        // Do nothing.
    }

    @Override
    public void abort()
    {
        if (s3.existsObject(bucket, s3ObjectKey)) {
            s3.deleteObject(bucket, s3ObjectKey);
            logger.info("Delete s3://" + bucket + "/" + s3ObjectKey);
        }
    }

    @Override
    public TaskReport commit()
    {
        s3.putObject(bucket, s3ObjectKey, byteBuffer);
        logger.info("Put into s3://" + bucket + "/" + s3ObjectKey);
        return Exec.newTaskReport();
    }
}
