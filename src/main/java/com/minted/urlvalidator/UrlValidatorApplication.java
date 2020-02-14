package com.minted.urlvalidator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.minted.urlvalidator.service.AWSS3ClientService;
import com.minted.urlvalidator.service.FxgJSONURLGeneratorService;
import com.minted.urlvalidator.service.URLValidatorService;

@SpringBootApplication
public class UrlValidatorApplication implements CommandLineRunner{
	
	
	private static final String SKU_LIST_PATH = "src/main/resources/skulist_master.txt";
	
	@Autowired
	private FxgJSONURLGeneratorService fxgjsonURLGeneratorService;
	
	@Autowired
	private URLValidatorService urlValidatorService;
	
	@Autowired
	private AWSS3ClientService s3ClientService;

	public static void main(String[] args) {
		SpringApplication.run(UrlValidatorApplication.class, args);
	}
	
	@Override
	public void run(String args[]) throws IOException, InterruptedException, ExecutionException {
		
	   System.out.println("--------------------------------------------------------------------------------------------------------------------------------");
	   System.out.println("Validating whether templates exists in S3 Bucket");
	   
	   Map<String, String> s3ValidationResult = s3ClientService.getAllFilesFromS3Bucket("minted.render.prod", SKU_LIST_PATH);
	   
	   System.out.println("--------------------------------------------------------------------------------------------------------------------------------");
	   System.out.println("Completed validation writing to file");
	   writeResultsToCSV(s3ValidationResult, "s3Validation.csv");		
		
		/*
		 * System.out.println(
		 * "--------------------------------------------------------------------------------------------------------------------------------"
		 * ); System.out.println("Starting the validation of fxg json url's");
		 * 
		 * List<String> fxgJsonURLList =
		 * fxgjsonURLGeneratorService.generateFXGJsonURLFromFile(SKU_LIST_PATH);
		 * 
		 * System.out.println(
		 * "--------------------------------------------------------------------------------------------------------------------------------"
		 * ); System.out.println("Total count of the fxg json url's fetched from db : "
		 * + fxgJsonURLList.size());
		 * 
		 * System.out.println(
		 * "--------------------------------------------------------------------------------------------------------------------------------"
		 * ); System.out.println("Starting to get the status of the fxg json urls at: "
		 * + LocalDateTime.now());
		 * 
		 * Map<String, String> urlResult =
		 * urlValidatorService.fxgJsonUrlValidator(fxgJsonURLList);
		 * 
		 * writeResultsToCSV(urlResult, "output.csv"); System.out.println(
		 * "--------------------------------------------------------------------------------------------------------------------------------"
		 * );
		 * System.out.println("Finished retrieving the status of the fxg json urls at: "
		 * + LocalDateTime.now());
		 * 
		 * System.out.println(
		 * "--------------------------------------------------------------------------------------------------------------------------------"
		 * ); System.out.
		 * println("The resulting output.csv file will emailed to the recipients in the jenkins job. if the attachment is not received then please check the folder"
		 * +
		 * "src/main/resources under the workspace in the slave for the output csv file"
		 * ); System.out.println(
		 * "--------------------------------------------------------------------------------------------------------------------------------"
		 * );
		 */
	}

	private void writeResultsToCSV(Map<String, String> urlResult, String fileName) throws IOException {
		Path out = Paths.get("src/main/resources/report/" + fileName);
		Files.write(out, () -> urlResult.entrySet().stream()
			    .<CharSequence>map(e -> e.getKey() + "," + e.getValue())
			    .iterator());
	}
}
