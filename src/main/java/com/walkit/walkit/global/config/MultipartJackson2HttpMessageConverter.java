package com.walkit.walkit.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class MultipartJackson2HttpMessageConverter extends AbstractHttpMessageConverter<Object> {

    private final ObjectMapper objectMapper;

    public MultipartJackson2HttpMessageConverter(ObjectMapper objectMapper) {
        super(MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON);
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean canRead(Class<?> clazz, MediaType mediaType) {
        // APPLICATION_OCTET_STREAM 또는 APPLICATION_JSON만 처리
        if (mediaType == null) {
            return false;
        }
        return MediaType.APPLICATION_OCTET_STREAM.includes(mediaType)
                || MediaType.APPLICATION_JSON.includes(mediaType);
    }

    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        // 쓰기는 지원하지 않음
        return false;
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return true;
    }

    @Override
    protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {
        try (InputStream inputStream = inputMessage.getBody()) {
            return objectMapper.readValue(inputStream, clazz);
        } catch (IOException e) {
            log.error("Failed to read JSON from multipart request", e);
            throw new HttpMessageNotReadableException("Failed to read JSON: " + e.getMessage(), e, inputMessage);
        }
    }

    @Override
    protected void writeInternal(Object object, HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {
        throw new HttpMessageNotWritableException("This converter does not support writing");
    }
}