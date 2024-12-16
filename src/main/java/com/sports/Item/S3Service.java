package com.sports.Item;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class S3Service {

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;
    private final S3Presigner s3Presigner;
    private final S3Client s3Client;

    String createPreSignedURL(String filename) {
        // 파일 이름이 null이 아닌지 확인
        if (filename == null || filename.isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be null or empty");
        }

        // 경로를 적절하게 구성
        String path = "img/" + filename; // "img/"는 원하는 폴더를 나타냄

        var putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(path)
                .build();
        var preSignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(3))
                .putObjectRequest(putObjectRequest)
                .build();
        return s3Presigner.presignPutObject(preSignRequest).url().toString();
    }

    // 단일 파일 저장
    public String saveFile(String fileName, InputStream inputStream) throws IOException {

        String basePath = "test/";
        String filePath;

        // fileName이 null일 경우 기본 파일 경로 설정
        if (fileName == null || fileName.isEmpty()) {
            filePath = basePath + "logoblackbg.png"; // 기본으로 띄울 이미지 (나중에 기본이미지 정해서 변경)
        } else {
            filePath = basePath + fileName;

            PutObjectResponse response = s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(filePath)
                            .build(),
                    RequestBody.fromInputStream(inputStream, inputStream.available())
            );
        }
        return "https://" + bucket + ".s3.amazonaws.com/" + filePath;
    }

    // 여러 파일 저장
    public String saveFiles(List<MultipartFile> files) throws IOException {
        List<String> fileUrls = new ArrayList<>();
        String basePath = "test/";
        String defaultImagePath = basePath + "logoblackbg.png"; // 기본 이미지 경로

        // 파일 리스트가 비어 있거나 null인 경우 기본 이미지 추가
        if (files == null || files.isEmpty()) {
            return "https://" + bucket + ".s3.amazonaws.com/" + defaultImagePath;
        }

        // 파일 리스트가 있는 경우 각 파일을 처리
        for (MultipartFile file : files) {
            String fileName = file.getOriginalFilename();
            String filePath = basePath + fileName;

            try (InputStream inputStream = file.getInputStream()) {
                s3Client.putObject(
                        PutObjectRequest.builder()
                                .bucket(bucket)
                                .key(filePath)
                                .build(),
                        RequestBody.fromInputStream(inputStream, inputStream.available())
                );

                String fileUrl = "https://" + bucket + ".s3.amazonaws.com/" + filePath;
                fileUrls.add(fileUrl);

            }
        }

        // URL 리스트를 콤마로 연결하여 반환
        return String.join(",", fileUrls);
    }

    // 기본 이미지 경로 뽑기 -> 사진 바꾸고싶으면 defaultImagePath 파일이름 변경하기
    public String defaultPath () {
        String basePath = "test/";
        // logowhitebg, MovoLogo, MovoLogo시안 (.png)
        String defaultImagePath = "logoblackbg.png";
        return "https://" + bucket + ".s3.amazonaws.com/" + basePath + defaultImagePath;
    }

}
