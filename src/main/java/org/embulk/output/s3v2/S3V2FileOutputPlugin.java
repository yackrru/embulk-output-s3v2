package org.embulk.output.s3v2;

import java.util.List;
import org.embulk.config.ConfigDiff;
import org.embulk.config.ConfigSource;
import org.embulk.config.TaskReport;
import org.embulk.config.TaskSource;
import org.embulk.output.s3v2.strategy.BufferedStrategy;
import org.embulk.output.s3v2.strategy.FileOutputStrategy;
import org.embulk.spi.Exec;
import org.embulk.spi.FileOutputPlugin;
import org.embulk.spi.TransactionalFileOutput;

public class S3V2FileOutputPlugin
        implements FileOutputPlugin
{
    @Override
    public void cleanup(TaskSource taskSource,
            int taskCount, List<TaskReport> successTaskReports)
    {
        // Do nothing.
    }

    @Override
    public TransactionalFileOutput open(TaskSource taskSource, int taskIndex)
    {
        PluginTask task = taskSource.loadTask(PluginTask.class);
        if (task.getEnableTempFileOutput()) {
            return new FileOutputStrategy(task, taskIndex);
        }
        return new BufferedStrategy(task, taskIndex);
    }

    @Override
    public ConfigDiff resume(TaskSource taskSource,
            int taskCount, FileOutputPlugin.Control control)
    {
        control.run(taskSource);
        return Exec.newConfigDiff();
    }

    @Override
    public ConfigDiff transaction(ConfigSource config,
            int taskCount, FileOutputPlugin.Control control)
    {
        PluginTask task = config.loadConfig(PluginTask.class);
        return resume(task.dump(), taskCount, control);
    }
}
