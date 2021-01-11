package org.embulk.output.s3v2;

import org.embulk.config.Config;
import org.embulk.config.ConfigDefault;
import org.embulk.config.Task;

public interface PluginTask
        extends Task
{
    @Config("region")
    public String getRegion();

    @Config("enable_profile")
    @ConfigDefault("false")
    public boolean getEnableProfile();

    @Config("profile")
    @ConfigDefault("\"default\"")
    public String getProfile();

    @Config("bucket")
    public String getBucket();

    @Config("object_key_prefix")
    public String getObjectKeyPrefix();

    @Config("enable_multi_part_upload")
    @ConfigDefault("false")
    public boolean getEnableMultiPartUpload();

    @Config("max_concurrent_requests")
    @ConfigDefault("10")
    public int getMaxConcurrentRequests();

    /**
     * Max: 2GB
     * Min: 5MB
     */
    @Config("multipart_chunksize")
    @ConfigDefault("\"8MB\"")
    public String getMultipartChunksize();

    @Config("multipart_threshold")
    @ConfigDefault("\"8MB\"")
    public String getMultipartThreshold();

    @Config("extension")
    public String getExtension();

    @Config("enable_temp_file_output")
    @ConfigDefault("true")
    public boolean getEnableTempFileOutput();

    @Config("temp_path")
    @ConfigDefault("\"/tmp\"")
    public String getTempPath();

    @Config("temp_file_prefix")
    @ConfigDefault("\"embulk-output-s3v2\"")
    public String getTempFilePrefix();
}
