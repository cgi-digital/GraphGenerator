package graphgenerator.utilities;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Component
public class FileService
{
    private final Path rootLocation;


    public FileService(@Value("${picturesDirectory}") String picturesDirectory)
    {
        rootLocation = Paths.get(picturesDirectory);
    }

//    public String storeFile(MultipartFile file, Long id)
//    {
//        String filename = StringUtils.cleanPath(file.getOriginalFilename());
//        try
//        {
//            if(file.isEmpty())
//            {
//                return null;
//            }
//
//            Files.copy(file.getInputStream(), this.rootLocation.resolve(filename),StandardCopyOption.REPLACE_EXISTING);
//            return this.rootLocation.resolve(filename).toString();
//        }
//        catch (IOException e)
//        {
//            return null;
//        }
//    }

    public Path load(String filename) {
        return rootLocation.resolve(filename);
    }

    public Resource loadAsResource(String filename, String type) throws IOException {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable())
            {
                return resource;
            }
            else if(type.equals("person"))
            {
                return new UrlResource("http://localhost:8888/images/person.png");
            }
            else if(type.equals("crime"))
            {
                return new UrlResource("http://localhost:8888/images/crime.png");
            }
            else
            {
                throw new IOException("Could not read file: " + filename);
            }
        }
        catch (MalformedURLException e)
        {
            throw new IOException("Could not read file: " + filename, e);
        }
    }
}
