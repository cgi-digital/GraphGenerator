package graphgenerator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.web.MultipartAutoConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@EnableAutoConfiguration(exclude = { JacksonAutoConfiguration.class })
@SpringBootApplication(scanBasePackages={"graphgenerator.*"})
public class Application {

    public static void main(String[] args)
    {
        final Properties appProperties = new Properties();
        try(InputStream stream =
            Application.class.getClassLoader().getResourceAsStream("application.properties")) {
                appProperties.load(stream);
            } catch(IOException e) {
                System.out.println("Unable to load application properies file");
            }
        
        // Set up graphgenerator directories
        File neo4jDbDir = new File(appProperties.getProperty("dbPath"));
        boolean neo4jDbDirExists = false;
        if(!neo4jDbDir.exists()) {
            neo4jDbDirExists = neo4jDbDir.mkdirs();
        } else {
            neo4jDbDirExists = true;
        }
        File picturesDir = new File(appProperties.getProperty("picturesDirectory"));
        boolean picturesDirExists = false;
        if(!picturesDir.exists()) {
            picturesDirExists = picturesDir.mkdirs();
        } else {
            picturesDirExists = true;
        }

        // If the directories are there start the spring application
        if(picturesDirExists && neo4jDbDirExists)
                SpringApplication.run(Application.class, args);
        else {
            System.out.println("Unable to start spring application due to failure to create directories");
        }
    }

}
