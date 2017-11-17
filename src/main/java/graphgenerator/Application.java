package graphgenerator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages={"graphgenerator.*"})
public class Application {

    public static void main(String[] args)
    {
        SpringApplication.run(Application.class, args);


//        GraphBuilder graphBuilder = (GraphBuilder) ApplicationContextProvider.getContext().getBean("graphBuilder");
        int x = 0;
    }

}
