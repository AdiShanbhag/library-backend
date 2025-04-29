package com.libraryhub.book;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class CloudinaryTest {

    public static void main(String[] args) throws IOException {
        // Initialize Cloudinary with your credentials directly
        Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dpddciabn", // replace with your actual cloud name
                "api_key", "958158161521144",       // replace with your actual API key
                "api_secret", "RTrqQ5zsSxNGHN695TIC8vbQ3q0"  // replace with your actual API secret
        ));

        // Example file to upload (replace with an actual file path on your system)
        File file = new File("~/Downloads/Offer_AdityaDevidas_Shanbhag.pdf");

        // Upload the file to Cloudinary
        Map uploadResult = cloudinary.uploader().upload(file, ObjectUtils.asMap("resource_type", "auto"));

        // Print out the upload result to verify it worked
        System.out.println(uploadResult);
    }
}
