package org.example;

import SubClasses.Miniperformance;
import model.Performance;
import model.PerformanceComparatorSize;
import ClassesToXml.AuthorArray;
import ClassesToXml.Rubricks;
import DramTeatr.DramTeatrParser;
import DramTeatr.DramTeatrSettings;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import DefaultClasses.ParserWorker;
import model.Article;
import ClassesToXml.ArticleArray;
import model.ArticleComparator;
import ClassesToXml.MapArray;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


public class App
{
    static class NewData<T> implements ParserWorker.OnNewDataHandler<ArrayList<T>> {

        public ArrayList<T> Data;

        public NewData() {
            Data = new ArrayList<>();
        }

        public ArrayList<T> GetData() {
            return Data;
        }

        @Override
        public void OnNewData(Object sender, ArrayList<T> args) {
            Data.addAll(args);
        }

    }

    static class Completed implements ParserWorker.OnCompleted {

        @Override
        public void OnCompleted(Object sender) {
            System.out.println("Загрузка закончена");
        }
    }

    static class Print<T> implements ParserWorker.Print<ArrayList<T>>
    {
        @Override
        public void Print(ArrayList<T> args) throws JsonProcessingException {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(args);
            try (FileWriter writer = new FileWriter("src/main/java/Data/DatasetDramTeatr.json", false)) {
                writer.write(json);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Множество авторов статей со списком их публикаций в формате json
     */
    static class Print1_1Json<T> implements ParserWorker.Print<ArrayList<Article>>
    {
        @Override
        public void Print(ArrayList<Article> args) throws JsonProcessingException {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
            args = (ArrayList<Article>) args.stream().distinct().collect(Collectors.toList());
            Map<String, ArrayList<Article>> articlesByAuthor = args.stream()
                    .collect(Collectors.groupingBy(
                            Article::getAuthor,
                            Collectors.collectingAndThen(
                                    Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(Article::hashCode))),
                                    ArrayList::new
                            )
                    ));
            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(articlesByAuthor);
            try (FileWriter writer = new FileWriter("src/main/java/Data/1_1Data.json", false)) {
                writer.write(json);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Множество авторов статей со списком их публикаций в формате XML
     */
    static class Print1_1XML<T> implements ParserWorker.Print<ArrayList<Article>>
    {
        @Override
        public void Print(ArrayList<Article> args) throws JsonProcessingException, JAXBException
        {

            JAXBContext context = JAXBContext.newInstance(MapArray.class, Article.class, ArrayList.class, ArticleArray.class, AuthorArray.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            args = (ArrayList<Article>) args.stream().distinct().collect(Collectors.toList());
            Map<String, ArrayList<Article>> articlesByAuthor = args.stream()
                    .collect(Collectors.groupingBy(
                            Article::getAuthor,
                            Collectors.collectingAndThen(
                                    Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(Article::hashCode))),
                                    ArrayList::new
                            )
                    ));

            AuthorArray mapArray = new AuthorArray(articlesByAuthor);
            marshaller.marshal(mapArray, new File("src/main/java/Data/1_1Data.xml"));
        }
    }

    /**
     * Множество публикаций с количеством просмотров, больших 100 в формате json
     */
    static class Print1_2Json<T> implements ParserWorker.Print<ArrayList<Article>>
    {
        @Override
        public void Print(ArrayList<Article> args) throws JsonProcessingException {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
            args = (ArrayList<Article>) args.stream().distinct().collect(Collectors.toList());
            args = (ArrayList<Article>) args.stream()
                    .filter(Article -> Article.getViews() >= 100)
                    .distinct()
                    .sorted(new ArticleComparator())
                    .collect(Collectors.toList());
            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(args);
            try (FileWriter writer = new FileWriter("src/main/java/Data/1_2Data.json", false)) {
                writer.write(json);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Множество публикаций с количеством просмотров, больших 100 в формате XML
     */
    static class Print1_2XML<T> implements ParserWorker.Print<ArrayList<Article>>
    {
        @Override
        public void Print(ArrayList<Article> args) throws JsonProcessingException, JAXBException
        {
            JAXBContext context = JAXBContext.newInstance(ArticleArray.class, Article.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            args = (ArrayList<Article>) args.stream().distinct().collect(Collectors.toList());
            args = (ArrayList<Article>) args.stream()
                    .filter(Article -> Article.getViews() >= 100)
                    .distinct()
                    .sorted(new ArticleComparator())
                    .collect(Collectors.toList());

            ArticleArray articles = new ArticleArray(args);
            marshaller.marshal(articles, new File("src/main/java/Data/1_2Data.xml"));
        }
    }

    /**
     * Множество уникальных рубрик в формате json
     */
    static class Print1_3Json<T> implements ParserWorker.Print<ArrayList<Article>>
    {
        @Override
        public void Print(ArrayList<Article> args) throws JsonProcessingException {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
            args = (ArrayList<Article>) args.stream().distinct().collect(Collectors.toList());

            ArrayList<String> Rubricks = (ArrayList<String>) args.stream()
                    .flatMap(article -> article.getRubriks().stream())
                    .distinct()
                    .collect(Collectors.toList());

            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(Rubricks);
            try (FileWriter writer = new FileWriter("src/main/java/Data/1_3Data.json", false)) {
                writer.write(json);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Множество уникальных рубрик в формате XML
     */
    static class Print1_3XML<T> implements ParserWorker.Print<ArrayList<Article>>
    {
        @Override
        public void Print(ArrayList<Article> args) throws JsonProcessingException, JAXBException {

            JAXBContext context = JAXBContext.newInstance(ArrayList.class, Rubricks.class, String.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            args = (ArrayList<Article>) args.stream().distinct().collect(Collectors.toList());

            ArrayList<String> Rubricks = (ArrayList<String>) args.stream()
                    .flatMap(article -> article.getRubriks().stream())
                    .distinct()
                    .collect(Collectors.toList());


            Rubricks rubricks = new Rubricks(Rubricks);
            marshaller.marshal(rubricks, new File("src/main/java/Data/1_3Data.xml"));
        }
    }

    /**
     * Множество отсортированных по возрастанию спектаклей с ограничением 6+ в формате json
     */
    static class Print2_1Json<T> implements ParserWorker.Print<ArrayList<Performance>>
    {
        @Override
        public void Print(ArrayList<Performance> args) throws JsonProcessingException {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
            args = (ArrayList<Performance>) args.stream().distinct().collect(Collectors.toList());

            args = (ArrayList<Performance>) args.stream()
                    .filter(Performance -> Objects.equals(Performance.getAgeLimit(), "6+"))
                    .sorted(new PerformanceComparatorSize())
                    .collect(Collectors.toList());

            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(args);
            try (FileWriter writer = new FileWriter("src/main/java/Data/2_1Data.json", false)) {
                writer.write(json);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Множество различных спектаклей без поля ageLimit в формате json
     */
    static class Print2_2Json<T> implements ParserWorker.Print<ArrayList<Performance>>
    {
        @Override
        public void Print(ArrayList<Performance> args) throws JsonProcessingException {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
            args = (ArrayList<Performance>) args.stream().distinct().collect(Collectors.toList());

            ArrayList<Miniperformance> argsNew = (ArrayList<Miniperformance>) args.stream()
                    .map(Miniperformance::new)
                    .collect(Collectors.toList());

            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(argsNew);
            try (FileWriter writer = new FileWriter("src/main/java/Data/2_2Data.json", false)) {
                writer.write(json);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException, JAXBException {
        /*ParserWorker<Article> parser = new ParserWorker<>(new HabrParser());
        int start = 1;
        int end = 5;
        parser.setParserSettings(new HabrSettings(1, 5));
        parser.onCompletedList.add(new Completed());
        parser.onNewDataList.add(new NewData());
        parser.onPrintList.add(new Print1_3XML());
        parser.Start();
        Thread.sleep(10000);
        parser.Abort();*/

        ParserWorker<Article> parser = new ParserWorker<>(new DramTeatrParser());
        parser.setParserSettings(new DramTeatrSettings());
        parser.onCompletedList.add(new Completed());
        parser.onNewDataList.add(new NewData());
        parser.onPrintList.add(new Print2_2Json());
        parser.Start();
        Thread.sleep(10000);
        parser.Abort();
    }
}

