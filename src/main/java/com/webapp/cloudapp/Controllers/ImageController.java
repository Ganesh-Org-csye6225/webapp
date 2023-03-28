package com.webapp.cloudapp.Controllers;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.multipart.MultipartFile;
import com.timgroup.statsd.StatsDClient;
import com.webapp.cloudapp.Services.ImageService;


@RestController
public class ImageController {

	@Autowired
	ImageService imageService;

	@Autowired
    private StatsDClient statsDClient;

	Logger logger = LoggerFactory.getLogger(ImageController.class);

	@PostMapping(value = "/v1/product/{productId}/image", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE,
			MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<?> addImage(@RequestParam("file") MultipartFile multipartFile,
			@PathVariable String productId, NativeWebRequest nativeWebRequest) {
		statsDClient.incrementCounter("post.imageRequest.count");
		logger.info("ImageController: Adding an image for a product");
		try {
			return imageService.createImage(multipartFile, productId, nativeWebRequest);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			Map<String, Object> resMap = new HashMap<>();
			resMap.put("msg", "Please enter valid data");
			logger.error("ImageController: Error adding an image for a product", e.getLocalizedMessage());
			return new ResponseEntity<>(resMap, HttpStatusCode.valueOf(400));
		}
	}

	@GetMapping("/v1/product/{productId}/image/{imageId}")
	public ResponseEntity<?> getImage(@PathVariable String productId, @PathVariable String imageId,
    NativeWebRequest nativeWebRequest) {
		statsDClient.incrementCounter("get.imageRequest.count");
		logger.info("ImageController: Fetching the image of a product");
		try {
			return imageService.getImage(imageId, productId, nativeWebRequest);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			Map<String, Object> resMap = new HashMap<>();
			resMap.put("msg", "Please enter valid data");
			logger.error("ImageController: Error fetching the image of a product",e.getLocalizedMessage());
			return new ResponseEntity<>(resMap, HttpStatusCode.valueOf(400));
		}
	}

	@GetMapping("/v1/product/{productId}/image")
	public ResponseEntity<?> getAllImages(@PathVariable String productId,NativeWebRequest nativeWebRequest) {
		statsDClient.incrementCounter("getAll.imageRequest.count");
		logger.info("ImageController: Fetching all the images of a product");
		try {
			return imageService.getAllImage(productId, nativeWebRequest);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			Map<String, Object> resMap = new HashMap<>();
			resMap.put("msg", "Please enter valid data");
			logger.error("ImageController: Error fetching the images of a product",e.getLocalizedMessage());
			return new ResponseEntity<>(resMap, HttpStatusCode.valueOf(400));
		}
	}

	@DeleteMapping("/v1/product/{productId}/image/{imageId}")
	public ResponseEntity<?> deleteImages(@PathVariable String productId, @PathVariable String imageId,
    NativeWebRequest nativeWebRequest) {
		statsDClient.incrementCounter("delete.imageRequest.count");
		logger.info("ImageController: Deleting the image of a product");
		try {
			return imageService.deleteImage(imageId, productId, nativeWebRequest);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			Map<String, Object> resMap = new HashMap<>();
			resMap.put("msg", "Please enter valid data");
			logger.error("ImageController: Error deleting the image of a product");
			return new ResponseEntity<>(resMap, HttpStatusCode.valueOf(400));
		}
	}

}
