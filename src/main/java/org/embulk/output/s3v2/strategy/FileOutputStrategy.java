package org.embulk.output.s3v2.strategy;

import org.embulk.config.TaskReport;
import org.embulk.output.s3v2.PluginTask;
import org.embulk.output.s3v2.s3.S3MultiPartStatus;
import org.embulk.output.s3v2.util.ThresholdComputation;
import org.embulk.spi.Buffer;
import org.embulk.spi.Exec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class FileOutputStrategy extends AbstractStrategy
{
    private final Logger logger = LoggerFactory.getLogger(FileOutputStrategy.class);
    private final String bucket;
    private String s3ObjectKey;
    private BufferedOutputStream bufferStream;
    private Path tempFilePath;

    public FileOutputStrategy(PluginTask task, int taskIndex)
    {
        super(task, taskIndex);
        this.bucket = task.getBucket();
    }

    @Override
    protected boolean validate()
    {
        return true;
    }

    @Override
    public void nextFile()
    {
        String ext = getFileExtension();
        createTempFile(ext);
        s3ObjectKey = task.getObjectKeyPrefix() + "-" + taskIndex + ext;
    }

    private void createTempFile(String ext)
    {
        try {
            Path tempPath = Paths.get(task.getTempPath());
            if (Files.notExists(tempPath)) {
                Files.createDirectory(tempPath);
            }

            String dirSeparator = tempPath.endsWith("/") ? "" : "/";
            tempFilePath = Paths.get(task.getTempPath() + dirSeparator
                    + task.getTempFilePrefix() + "-" + taskIndex
                    + ext);
            if (Files.exists(tempFilePath)) {
                Files.delete(tempFilePath);
                logger.info("Deleted previous: " + tempFilePath);
            }
            Files.createFile(tempFilePath);
            logger.info("Created: " + tempFilePath);

            bufferStream = new BufferedOutputStream(new FileOutputStream(tempFilePath.toFile()));
        }
        catch (IOException ex) {
            closeBuffer();
            ex.printStackTrace();
            throw new RuntimeException("Failed to create temp file: " + tempFilePath);
        }
    }

    @Override
    public void add(Buffer buffer)
    {
        try {
            bufferStream.write(buffer.array(), buffer.offset(), buffer.limit());
        }
        catch (IOException ex) {
            closeBuffer();
            ex.printStackTrace();
            throw new RuntimeException("Failed to buffer data.");
        }
    }

    @Override
    public void finish()
    {
        try {
            bufferStream.flush();
        }
        catch (IOException ex) {
            closeBuffer();
            ex.printStackTrace();
            throw new RuntimeException("Failed to write out data.");
        }
    }

    @Override
    public void close()
    {
        closeBuffer();
    }

    @Override
    public void abort()
    {
        if (s3.existsObject(bucket, s3ObjectKey)) {
            s3.deleteObject(bucket, s3ObjectKey);
            logger.info("Deleted s3://" + bucket + "/" + s3ObjectKey);
        }

        try {
            Files.delete(tempFilePath);
            logger.info("Deleted " + tempFilePath);
        }
        catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Failed to delete: " + tempFilePath);
        }
        finally {
            closeBuffer();
        }
    }

    @Override
    public TaskReport commit()
    {
        long start = System.currentTimeMillis();
        try {
            if (task.getEnableMultiPartUpload()
                    && ThresholdComputation.largerThanThreshold(task.getMultipartThreshold(), Files.size(tempFilePath))) {
                S3MultiPartStatus status = setUpS3MultiPartStatus();
                s3.multiPartUpload(bucket, s3ObjectKey, tempFilePath, status);
                logger.info("[Done] Multipart upload: s3://" + bucket + "/" + s3ObjectKey);
            }
            else {
                s3.putObject(bucket, s3ObjectKey, tempFilePath);
                logger.info("[Done] Put: s3://" + bucket + "/" + s3ObjectKey);
            }
        }
        catch (IOException ex) {
            ex.printStackTrace();
            abort();
        }
        long end = System.currentTimeMillis();
        logger.info("Time taken to upload s3://" + bucket + "/" + s3ObjectKey + ": " + (end - start) + "ms");
        return Exec.newTaskReport();
    }

    private void closeBuffer()
    {
        if (!Objects.isNull(bufferStream)) {
            try {
                bufferStream.close();
            }
            catch (IOException ex) {
                ex.printStackTrace();
                throw new RuntimeException("Failed to close BufferedOutputStream.");
            }
        }
    }
}
