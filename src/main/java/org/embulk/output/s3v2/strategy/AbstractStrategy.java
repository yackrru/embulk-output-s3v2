package org.embulk.output.s3v2.strategy;

import org.embulk.output.s3v2.PluginTask;
import org.embulk.output.s3v2.s3.S3ClientManager;
import org.embulk.output.s3v2.s3.S3MultiPartStatus;
import org.embulk.spi.TransactionalFileOutput;

abstract class AbstractStrategy implements TransactionalFileOutput
{
    protected S3ClientManager s3;
    protected PluginTask task;
    protected int taskIndex;

    public AbstractStrategy(PluginTask task, int taskIndex)
    {
        s3 = new S3ClientManager(task.getRegion());
        this.task = task;
        this.taskIndex = taskIndex;

        if (!validate()) {
            throw new IllegalArgumentException("Unsupported parameters combination");
        }
    }

    /**
     * Validation for PluginTask parameters combination.
     * @see PluginTask
     */
    protected abstract boolean validate();

    protected final String getFileExtension()
    {
        return task.getExtension().startsWith(".") ? task.getExtension() : "." + task.getExtension();
    }

    protected final S3MultiPartStatus setUpS3MultiPartStatus()
    {
        return new S3MultiPartStatus(task.getMaxConcurrentRequests(), task.getMultipartChunksize(),
                task.getMultipartThreshold());
    }
}
