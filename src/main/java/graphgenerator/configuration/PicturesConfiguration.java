package graphgenerator.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "")
public class PicturesConfiguration {


    public static Map<String,String> pictures;

    public Map<String,String> getPictures() {
        return pictures;
    }

    public void setPictures(Map<String,String> pictures) {
        this.pictures = pictures;
    }
}
