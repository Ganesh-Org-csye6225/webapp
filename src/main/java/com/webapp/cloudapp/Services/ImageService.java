package com.webapp.cloudapp.Services;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.webapp.cloudapp.Util.BasicAccessAuthenticationHandler;
import com.webapp.cloudapp.Util.Util;
import com.webapp.cloudapp.Entity.Image;
import com.webapp.cloudapp.Entity.Product;
import com.webapp.cloudapp.Entity.User;
import com.webapp.cloudapp.Repository.ImageRepository;
import com.webapp.cloudapp.Repository.ProductRepository;

@Service
public class ImageService {

	@Autowired
	ImageRepository imageRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private BasicAccessAuthenticationHandler authHandler;

	Logger logger = LoggerFactory.getLogger(ImageService.class);


	@Autowired
	private AmazonS3 s3;

	@Value("${aws.s3.bucket}")
	private String s3Bucket;

	public ResponseEntity<?> createImage(MultipartFile multipartFile, String productId,
    NativeWebRequest nativeWebRequest) {
		try {

			User authUser = authHandler.getUser(nativeWebRequest);
			if (authUser == null) {
				logger.error("ImageService: User Authentication failed");
				return new ResponseEntity<>(null, HttpStatusCode.valueOf(401));
			}

			if (Util.isValidNumber(productId) == false) {
				logger.error("ImageService: Invalid productId format");
				return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
			}

			Optional<Product> produOptional = productRepository.findById(Integer.parseInt(productId));

			if (!produOptional.isPresent()) {
				logger.error("ImageService: Product not found for the given productID");
				return new ResponseEntity<>(null, HttpStatusCode.valueOf(404));
			}

			if (produOptional.get().getUser().getId() != authUser.getId()) {
				logger.error("ImageService: User does not have access for the given productID");
				return new ResponseEntity<>(null, HttpStatusCode.valueOf(403));
			}

			Product product = produOptional.get();
			String extension = FilenameUtils.getExtension(multipartFile.getOriginalFilename());
			if (!isSupportedExtension(extension)) {
				logger.error("ImageService: File of type " + extension+ " is not supported");
				return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
			}

			InputStream fis = multipartFile.getInputStream();

			String filename = multipartFile.getOriginalFilename();
			String filepath = UUID.randomUUID() + "/" + filename;
			String bucket = s3Bucket;

			ObjectMetadata objectMetadata = new ObjectMetadata();
			objectMetadata.setContentLength(multipartFile.getSize());
			objectMetadata.setContentType(multipartFile.getContentType());
			objectMetadata.setCacheControl("public, max-age=31536000");

			try {
				s3.putObject(bucket, filepath, fis, objectMetadata);
				logger.info("Successfully saved the image in s3 bucket");
			} catch (AmazonServiceException e) {
				System.out.println("ase " + e.getLocalizedMessage());
				logger.error("ImageService: Error saving the image in s3 bukcet", e.getLocalizedMessage());
				return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
			}
			
			String imageUrl = String.valueOf(s3.getUrl(bucket, filepath));
			Image image = new Image(product.getId(), filename, LocalDateTime.now().toString(), imageUrl);
			imageRepository.save(image);
			logger.info("Successfully saved the image details in DB");
			return new ResponseEntity<>(convertToImageDto(new ArrayList<>(Arrays.asList(image))),
					HttpStatusCode.valueOf(201));
		} catch (Exception e) {
			logger.error("ImageService: Error saving the image", e.getLocalizedMessage());
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
		}
	}

	private boolean isSupportedExtension(String extension) {
		return extension != null && (extension.equalsIgnoreCase("png") || extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("jpeg"));
	}

	public ResponseEntity<?> getAllImage(String productId,  NativeWebRequest nativeWebRequest) {
		User authUser = authHandler.getUser(nativeWebRequest);
		if (authUser == null) {
			logger.error("ImageService: User Authentication failed");
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(401));
		}

		if (Util.isValidNumber(productId) == false) {
			logger.error("ImageService: Invalid productId format");
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
		}

		Optional<Product> produOptional = productRepository.findById(Integer.parseInt(productId));

		if (!produOptional.isPresent()) {
			logger.error("ImageService: Product not found for the given productID");
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(404));
		}

		if (produOptional.get().getUser().getId() != authUser.getId()) {
			logger.error("ImageService: User does not have access for the given productID");
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(403));
		}

		List<Image> images = imageRepository.findAllByProductId(Integer.parseInt(productId));

		if (images != null && !images.isEmpty()) {
			logger.info("Successfully fetched all the images");
			return new ResponseEntity<>(convertToImageDto(images), HttpStatusCode.valueOf(200));
		}
		return new ResponseEntity<>(null, HttpStatusCode.valueOf(200));

	}

	private List<Object> convertToImageDto(List<Image> images) {
		List<Object> objsList = new ArrayList<>();
		for (Image img : images) {
			Map<String, Object> map = new HashMap<>();
			map.put("image_id", img.getId());
			map.put("product_id", img.getProductId());
			map.put("file_name", img.getFileName());
			map.put("date_created", img.getDateCreated());
			map.put("s3_bucket_path", img.getS3BucketPath());
			objsList.add(map);
		}
		return objsList;
	}

	public ResponseEntity<?> getImage(String imageId, String productId,  NativeWebRequest nativeWebRequest) {
		User authUser = authHandler.getUser(nativeWebRequest);
		if (authUser == null) {
			logger.error("ImageService: User Authentication failed");
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(401));
		}

		if (Util.isValidNumber(productId) == false) {
			logger.error("ImageService: Invalid productId format");
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
		}

		Optional<Product> produOptional = productRepository.findById(Integer.parseInt(productId));

		if (!produOptional.isPresent()) {
			logger.error("ImageService: Product not found for the given productID");
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(404));
		}

		if (produOptional.get().getUser().getId() != authUser.getId()) {
			logger.error("ImageService: User does not have access for the given productID");
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(403));
		}
		if (Util.isValidNumber(imageId) == false) {
			logger.error("ImageService: Invalid imageId format");
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
		}

		Optional<Image> imageOptional = imageRepository.findById(Integer.parseInt(imageId));

		if (!imageOptional.isPresent()) {
			logger.error("ImageService: Image not found for the given imageID");
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(404));
		}

		if (imageOptional.get().getProductId() != produOptional.get().getId()) {
			logger.error("ImageService: image does not belong to the given product");
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(403));
		}
		logger.info("Successfully fetched the image");
		return new ResponseEntity<>(new ArrayList<>(Arrays.asList(imageOptional.get())), HttpStatusCode.valueOf(200));
	}

	public ResponseEntity<?> deleteImage(String imageId, String productId,  NativeWebRequest nativeWebRequest) {
		User authUser = authHandler.getUser(nativeWebRequest);
		if (authUser == null) {
			logger.error("ImageService: User Authentication failed");
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(401));
		}

		if (Util.isValidNumber(productId) == false) {
			logger.error("ImageService: Invalid productId format");
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
		}

		Optional<Product> produOptional = productRepository.findById(Integer.parseInt(productId));

		if (!produOptional.isPresent()) {
			logger.error("ImageService: Product not found for the given productID");
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(404));
		}

		if (produOptional.get().getUser().getId() != authUser.getId()) {
			logger.error("ImageService: User does not have access for the given productID");
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(403));
		}
		if (Util.isValidNumber(imageId) == false) {
			logger.error("ImageService: Invalid imageId format");
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
		}

		Optional<Image> imageOptional = imageRepository.findById(Integer.parseInt(imageId));

		if (!imageOptional.isPresent()) {
			logger.error("ImageService: Image not found for the given imageID");
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(404));
		}

		if (imageOptional.get().getProductId() != produOptional.get().getId()) {
			logger.error("ImageService: image does not belong to the given product");
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(403));
		}
		Image image = imageOptional.get();
		String url = image.getS3BucketPath();
		System.out.println(url);
		String[] spl = url.split("/");
		String bucket = spl[2].split("\\.")[0];
		System.out.println(bucket);
		String filepath = spl[3] + "/" + spl[4];
		System.out.println(filepath);
		s3.deleteObject(bucket, filepath);
		imageRepository.delete(image);
		logger.info("Successfully deleted the image");
		return new ResponseEntity<>(null, HttpStatusCode.valueOf(204));
	}
}

