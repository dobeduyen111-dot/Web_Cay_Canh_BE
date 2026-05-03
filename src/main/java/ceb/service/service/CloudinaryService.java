package ceb.service.service;

import org.springframework.web.multipart.MultipartFile;
import ceb.domain.res.CloudinaryResponse;
import java.io.IOException;

public interface CloudinaryService {
    CloudinaryResponse uploadFile(MultipartFile file);
}
