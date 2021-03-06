package com.youlai.service.oss.service;

import cn.hutool.core.lang.Assert;
import com.youlai.service.oss.config.MinIOProperties;
import io.minio.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Slf4j
@Component
@EnableConfigurationProperties({MinIOProperties.class})
public class MinIOService implements InitializingBean {

    private MinIOProperties minIOProperties;

    public MinIOService(MinIOProperties minIOProperties){
        this.minIOProperties=minIOProperties;
    }

    private MinioClient client;

    @Override
    public void afterPropertiesSet() {
        Assert.notBlank(minIOProperties.getEndpoint(), "Minio URL 为空");
        Assert.notBlank(minIOProperties.getAccessKey(), "Minio accessKey为空");
        Assert.notBlank(minIOProperties.getSecretKey(), "Minio secretKey为空");
        this.client = new MinioClient.Builder()
                .endpoint(minIOProperties.getEndpoint())
                .credentials(minIOProperties.getAccessKey(), minIOProperties.getSecretKey())
                .build();
    }

    @SneakyThrows
    public void createBucketIfAbsent(String bucketName) {
        BucketExistsArgs bucketExistsArgs = BucketExistsArgs.builder()
                .bucket(bucketName)
                .build();
        if (!client.bucketExists(bucketExistsArgs)) {
            MakeBucketArgs makeBucketArgs = MakeBucketArgs.builder()
                    .bucket(bucketName).build();
            client.makeBucket(makeBucketArgs);
        }
    }

    public String putObject(String bucketName, String objectName, InputStream inputStream) throws Exception {
        createBucketIfAbsent(bucketName);
        PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .stream(inputStream, inputStream.available(), -1)
                .build();
        client.putObject(putObjectArgs);
        String path = client.getObjectUrl(bucketName, objectName);
        return path;
    }

    public void removeObject(String bucketName, String objectName) throws Exception {
        RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build();
        client.removeObject(removeObjectArgs);
    }
}
