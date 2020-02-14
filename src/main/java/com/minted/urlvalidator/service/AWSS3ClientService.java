package com.minted.urlvalidator.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.minted.urlvalidator.model.ProductFXG;
import com.minted.urlvalidator.model.ProductTemplates;
import com.minted.urlvalidator.repository.ProductRepositoryImplementation;

@Component
public class AWSS3ClientService {
	
	private AmazonS3 awsS3;
	private String awsMintedBucket;
	
	@Autowired
	ProductRepositoryImplementation productRepositoryImplementation;
	
	@Autowired
    public AWSS3ClientService(Region awsRegion, AWSCredentialsProvider awsCredentialsProvider, String awsMintedBucket) 
    {
        this.awsS3 = AmazonS3ClientBuilder.standard()
                .withCredentials(awsCredentialsProvider)
                .withRegion(awsRegion.getName()).build();
        this.awsMintedBucket = awsMintedBucket;
    }
	
	public Map<String, String> getAllFilesFromS3Bucket(String awsMintedBucket, String filePath) throws IOException {
		
		Map<String, String> s3ResultValidation = new HashMap<String, String>();
		List<String> productSKU = Files.readAllLines(Paths.get(filePath));
		List<ProductFXG> fxgList =  productRepositoryImplementation.getTemplates(productSKU);
		List<ProductTemplates> templatesList = productRepositoryImplementation.getAllTemplates(productSKU);
		List<String> allTemplates = new ArrayList<String>();
		
		for (ProductFXG fxg : fxgList) {
			if(!fxg.getFxg_filename().isEmpty()) {
				allTemplates.add(fxg.getFxg_filename());
			}
		}
		
		for (ProductTemplates templates : templatesList) {
			allTemplates.add(templates.getLinerTemplate());
			allTemplates.add(templates.getRapTemplate());
			allTemplates.add(templates.getRcpTemplate());
			allTemplates.add(templates.getSkinnywrapTemplate());
			
		}
		for (String templates: allTemplates) {
			  if(!this.awsS3.doesObjectExist(awsMintedBucket,templates)) {
				  s3ResultValidation.put(templates,String.valueOf(this.awsS3.doesObjectExist(awsMintedBucket,templates)));
			  } 
		}
		   
        return s3ResultValidation;
	}

	
}
