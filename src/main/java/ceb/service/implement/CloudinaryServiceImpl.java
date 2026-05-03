package ceb.service.implement;

import ceb.service.service.CloudinaryService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import ceb.domain.res.CloudinaryResponse;
import ceb.exception.FileEmptyException;
import ceb.exception.FileUploadFailedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryServiceImpl(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Override
    public CloudinaryResponse uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileEmptyException();
        }

        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            String publicId = uploadResult.get("public_id").toString();
            String url = uploadResult.get("secure_url").toString();
            
            return new CloudinaryResponse(publicId, url);
        } catch (Exception e) {
            throw new FileUploadFailedException(e);
        }
    }
}
