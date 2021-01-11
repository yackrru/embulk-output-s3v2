# Amazon S3 output plugin for Embulk
![](https://github.com/ttksm/embulk-output-s3v2/workflows/Java%20CI%20with%20Gradle/badge.svg)
![](https://github.com/ttksm/embulk-output-s3v2/workflows/Release%20gem/badge.svg)
<br>
embulk-output-s3v2 is a plugin for Embulk, which based on [aws-sdk-java-v2](https://github.com/aws/aws-sdk-java-v2).
Files stores on Amazon S3.

## Overview
- Plugin type: output
- Load all or nothing: no
- Resume supported: no
- Cleanup supported: yes, but development in progress

## Configuration
- **region**: AWS region name. (string, required)
- **enable_profile**: If true, AWS credentials profile will be used when authenticating AWS. (boolean, default: `false`)
  - Supported in v0.2.0 or later
- **profile**: AWS credentials profile name. If `enable_profile` is false, this parameter will be ignored. (string, default: `default`)
  - Supported in v0.2.0 or later
- **bucket**: S3 bucket name. (string, required)
- **object_key_prefix**: Prefix of S3 Objects key name. (string, required)
- **enable_multi_part_upload**: If true, multipart upload will be enable. (boolean, default: `false`)
  - If `enable_temp_file_output` is `false`, this parameter must be `false` or are not specified.
- **max_concurrent_requests**: Maximum concurrently requests to upload an object divided into multipart. If `enable_multi_part_upload` is false, this parameter will be ignored. (int, default: `10`)
- **multipart_chunksize**: Once the operation have decided to use multipart operation, the file will be divided into chunks specified this parameter. If `enable_multi_part_upload` is false, this parameter will be ignored. (string, default: `8MB`)
  - Minimum size: `5MB`
  - Maximum size: `2GB`
  - Enable semantics
    - Same as that of `multipart_threshold`
- **multipart_threshold**: If `enable_multi_part_upload` is false, this parameter will be ignored. (string, default: `8MB`)
  - Enable semantics
    - `KB`
    - `MB`
    - `GB`
    - `TB`
- **extension**: File extension. (string, required)
- **enable_temp_file_output**: If true, temp file will be created in `temp_path` directory. If false, bulk data will be treated on only buffer. (boolean, default: `true`)
- **temp_path**: Directory for temp file output. (string, default: `/tmp`)
- **temp_file_prefix**: Prefix of temp file name. (string, default: `embulk-output-s3v2`)
### Example
```yaml
out:
  type: s3v2
  region: ap-northeast-1
  bucket: s3-bucket-name
  object_key_prefix: embulk/embulk-output-s3v2
  temp_path: /tmp
  extension: .csv
  formatter:
    type: csv
    delimeter: ","
```

## Usage
### Build
```
$ ./gradlew gem
```
