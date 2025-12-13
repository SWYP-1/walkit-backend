package com.walkit.walkit.common.image.service;

import com.walkit.walkit.common.image.enums.ImageType;
import com.walkit.walkit.common.image.repository.ImageRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;

@Transactional
@Service
@RequiredArgsConstructor
public class ImageService {

    private final S3Client s3Client;
    private final UserImageService userImageService;
    private final ImageRepository imageRepository;

    @Value("${ncp.bucket-name}")
    private String bucketName;


    public String uploadFile(ImageType imageType, MultipartFile image, Long entityId) {
        try {
            String imageName = image.getOriginalFilename();
            String timestamp = String.valueOf(System.currentTimeMillis());

            imageName = imageType.toString() + "_" + timestamp + "_" + imageName ;

            // S3 업로드 요청 생성
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(imageName)
                    .contentType(image.getContentType())
                    .build();

            // 파일 업로드 실행
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(image.getBytes()));

            saveImageToDB(imageType, imageName, entityId);

            // 업로드된 파일의 URL 반환
            return "https://kr.object.ncloudstorage.com/" + bucketName + "/" + imageName;

        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 실패", e);
        }
    }

    private void saveImageToDB(ImageType imageType, String imageName, Long entityId) {
        switch(imageType) {
            case USER:
                userImageService.saveUserImage(imageName, entityId);
        }
    }

    public byte[] downloadFile(String imageName) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(imageName)
                .build();

        return s3Client.getObject(getObjectRequest, ResponseTransformer.toBytes()).asByteArray();
    }

    public void deleteFile(String imageName) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(imageName)
                .build();

        imageRepository.deleteByImageName(imageName);
        
        s3Client.deleteObject(deleteObjectRequest);
    }
}