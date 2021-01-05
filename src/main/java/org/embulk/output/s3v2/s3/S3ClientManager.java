package org.embulk.output.s3v2.s3;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.embulk.output.s3v2.util.ChunksizeComputation;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;

public class S3ClientManager
{
    private final S3Client s3;

    public S3ClientManager(String regionName)
    {
        Region region = Region.of(regionName);
        if (Region.regions().stream().noneMatch(o -> o.equals(region))) {
            throw new IllegalArgumentException("Not found aws region: " + regionName);
        }

        s3 = S3Client.builder()
                .region(region)
                .build();
    }

    /**
     * Note that listObjectsV2() returns up to 1,000 objects.
     */
    public boolean existsObject(String bucket, String objectKey)
    {
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix(objectKey)
                .build();
        ListObjectsV2Response response = s3.listObjectsV2(request);

        for (S3Object content : response.contents()) {
            if (content.key().equals(objectKey)) {
                return true;
            }
        }

        return false;
    }

    public void multiPartUpload(String bucket, String objectKey, Path sourceFile, S3MultiPartStatus status)
    {
        CreateMultipartUploadRequest createMultipartUploadRequest = CreateMultipartUploadRequest.builder()
                .bucket(bucket).key(objectKey)
                .build();
        CreateMultipartUploadResponse response = s3.createMultipartUpload(createMultipartUploadRequest);
        String uploadId = response.uploadId();

        try (FileChannel fc = FileChannel.open(sourceFile)) {
            String multipartChunksize = status.getMultipartChunksize();
            ByteBuffer buffer = ByteBuffer.allocate(ChunksizeComputation.getChunksizeBytes(multipartChunksize));

            ExecutorService es = Executors.newFixedThreadPool(status.getMaxConcurrentRequests());
            List<CompletableFuture<String>> futureList = new ArrayList<>();
            int i = 1;
            while (true) {
                buffer.clear();
                if (fc.read(buffer) == -1) {
                    break;
                }
                buffer.flip();

                // Create tmpFile per size of multipart_chunksize
                String tmpFile = sourceFile + "_" + i;
                try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(tmpFile))) {
                    bos.write(buffer.array(), buffer.arrayOffset(), buffer.limit());
                    bos.flush();
                }

                UploadPartRequest uploadPartRequest = UploadPartRequest.builder()
                        .bucket(bucket).key(objectKey).uploadId(uploadId)
                        .partNumber(i)
                        .build();

                // Async upload to S3
                CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                    return s3.uploadPart(uploadPartRequest, RequestBody.fromFile(Paths.get(tmpFile))).eTag();
                }, es);
                futureList.add(future);

                i++;
            }

            CompletableFuture.allOf(futureList.toArray(new CompletableFuture[futureList.size()])).join();
            List<CompletedPart> partList = new ArrayList<>();
            for (int j = 1; j <= futureList.size(); j++) {
                CompletedPart part = CompletedPart.builder().partNumber(j).eTag(futureList.get(j - 1).get()).build();
                partList.add(part);
                // Remove tmpFile
                Files.delete(Paths.get(sourceFile + "_" + j));
            }

            CompletedMultipartUpload completedMultipartUpload = CompletedMultipartUpload.builder()
                    .parts(partList).build();
            CompleteMultipartUploadRequest completeMultipartUploadRequest = CompleteMultipartUploadRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .uploadId(uploadId)
                    .multipartUpload(completedMultipartUpload)
                    .build();

            s3.completeMultipartUpload(completeMultipartUploadRequest);
        } catch (IOException | S3Exception | InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            AbortMultipartUploadRequest abortMultipartUploadRequest = AbortMultipartUploadRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .uploadId(uploadId)
                    .build();
            s3.abortMultipartUpload(abortMultipartUploadRequest);
        }
    }

    public void putObject(String bucket, String objectKey, Path sourceFile)
    {
        s3.putObject(PutObjectRequest.builder().bucket(bucket).key(objectKey).build(),
                RequestBody.fromFile(sourceFile));
    }

    public void putObject(String bucket, String objectKey, ByteBuffer byteBuffer)
    {
        s3.putObject(PutObjectRequest.builder().bucket(bucket).key(objectKey).build(),
                RequestBody.fromRemainingByteBuffer(byteBuffer));
    }

    public void deleteObject(String bucket, String objectKey)
    {
        s3.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(objectKey).build());
    }
}
