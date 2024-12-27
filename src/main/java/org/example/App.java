package org.example;

import SubClasses.*;
import habr.HabrParser;
import habr.HabrSettings;
import model.*;
import ClassesToXml.*;
import DramTeatr.*;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import DefaultClasses.ParserWorker;
import model.Article;
import ClassesToXml.ArticleArray;
import model.ArticleComparator;
import ClassesToXml.MapArray;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
    static class Print1_1XmlJaxb<T> implements ParserWorker.Print<ArrayList<Article>>
    {
        @Override
        public void Print(ArrayList<Article> args) throws JsonProcessingException, JAXBException
        {
            JAXBContext context = JAXBContext.newInstance(MapArray.class, Article.class, ArrayList.class, ArticleArray.class, AuthorArray.class, MiniArticle.class, MiniArticleArray.class);
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
            marshaller.marshal(mapArray, new File("src/main/java/Data/1_1DataJaxb.xml"));
        }
    }

    /**
     * Множество авторов статей со списком их публикаций в формате XML
     */
    static class Print1_1XmlDom<T> implements ParserWorker.Print<ArrayList<Article>>
    {
        @Override
        public void Print(ArrayList<Article> args) throws JsonProcessingException
        {
            Document document = DocumentHelper.createDocument();
            Element root = document.addElement("Authors");

            args = (ArrayList<Article>) args.stream().distinct().collect(Collectors.toList());
            Map<String, ArrayList<Article>> articlesByAuthor = args.stream()
                    .collect(Collectors.groupingBy(
                            Article::getAuthor,
                            Collectors.collectingAndThen(
                                    Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(Article::hashCode))),
                                    ArrayList::new
                            )
                    ));

            for (Map.Entry<String, ArrayList<Article>> entry : articlesByAuthor.entrySet()) {
                String authorName = entry.getKey();
                List<Article> articles = entry.getValue();

                // Создание элемента <author>
                Element authorElement = root.addElement("author");

                // Добавление имени автора
                authorElement.addElement("author").addText(authorName);

                // Создание элемента <articles>
                Element articlesElement = authorElement.addElement("articles");

                // Проход по каждой статье автора
                for (Article article : articles) {
                    // Создание элемента <article>
                    Element articleElement = articlesElement.addElement("article");

                    // Добавление полей статьи в элемент <article>
                    articleElement.addElement("name").addText(article.getName());
                    articleElement.addElement("prehedder").addText(article.getPrehedder());
                }
            }

            // Запись документа в файл
            try (FileWriter fileWriter = new FileWriter(new File("src/main/java/Data/1_1DataDom.xml"))) {
                OutputFormat format = OutputFormat.createPrettyPrint();
                XMLWriter writer = new XMLWriter(fileWriter, format);
                writer.write(document);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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
    static class Print1_2XmlJaxb<T> implements ParserWorker.Print<ArrayList<Article>>
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
            marshaller.marshal(articles, new File("src/main/java/Data/1_2DataJaxb.xml"));
        }
    }

    /**
     * Множество публикаций с количеством просмотров, больших 100 в формате XML
     */
    static class Print1_2XmlDom<T> implements ParserWorker.Print<ArrayList<Article>>
    {
        @Override
        public void Print(ArrayList<Article> args) throws JsonProcessingException, JAXBException
        {
            Document document = DocumentHelper.createDocument();
            Element root = document.addElement("Articles");

            args = (ArrayList<Article>) args.stream().distinct().collect(Collectors.toList());
            args = (ArrayList<Article>) args.stream()
                    .filter(Article -> Article.getViews() >= 100)
                    .distinct()
                    .sorted(new ArticleComparator())
                    .collect(Collectors.toList());

            for (Article article : args) {
                // Создание элемента <article>
                Element articleElement = root.addElement("article");

                // Добавление полей статьи в элемент <article>
                articleElement.addElement("author").addText(article.getAuthor());
                articleElement.addElement("name").addText(article.getName());
                articleElement.addElement("realise").addText(article.getRealise());
                articleElement.addElement("timeToRead").addText(String.valueOf(article.getTimeToRead()));
                articleElement.addElement("views").addText(String.valueOf(article.getViews()));
                articleElement.addElement("imgAddress").addText(article.getImgAddress());
                articleElement.addElement("prehedder").addText(article.getPrehedder());

                // Добавление элемента <Rubriks>
                Element rubriksElement = articleElement.addElement("Rubriks");
                for (String rubrik : article.getRubriks()) {
                    rubriksElement.addElement("rubriks").addText(rubrik);
                }
            }

            // Запись документа в файл
            try (FileWriter fileWriter = new FileWriter(new File("src/main/java/Data/1_2DataDom.xml"))) {
                OutputFormat format = OutputFormat.createPrettyPrint();
                XMLWriter writer = new XMLWriter(fileWriter, format);
                writer.write(document);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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
    static class Print1_3XmlJaxb<T> implements ParserWorker.Print<ArrayList<Article>>
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
            marshaller.marshal(rubricks, new File("src/main/java/Data/1_3DataJaxb.xml"));
        }
    }

    /**
     * Множество уникальных рубрик в формате XML
     */
    static class Print1_3XmlDom<T> implements ParserWorker.Print<ArrayList<Article>>
    {
        @Override
        public void Print(ArrayList<Article> args) throws JsonProcessingException, JAXBException
        {
            Document document = DocumentHelper.createDocument();
            Element root = document.addElement("Rubriks");

            args = (ArrayList<Article>) args.stream().distinct().collect(Collectors.toList());

            ArrayList<String> Rubricks = (ArrayList<String>) args.stream()
                    .flatMap(article -> article.getRubriks().stream())
                    .distinct()
                    .collect(Collectors.toList());


            for (String rubrik : Rubricks) {
                // Создание элемента <rubrika>
                root.addElement("rubrika").addText(rubrik);
            }

            // Запись документа в файл
            try (FileWriter fileWriter = new FileWriter(new File("src/main/java/Data/1_3DataDom.xml"))) {
                OutputFormat format = OutputFormat.createPrettyPrint();
                XMLWriter writer = new XMLWriter(fileWriter, format);
                writer.write(document);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Множество статей, время прочтения которых меньше среднего в формате XML
     */
    static class Print1_4XmlJaxb<T> implements ParserWorker.Print<ArrayList<Article>> {
        @Override
        public void Print(ArrayList<Article> args) throws JsonProcessingException, JAXBException {

            JAXBContext context = JAXBContext.newInstance(ArticleArray.class, Article.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            args = (ArrayList<Article>) args.stream().distinct().collect(Collectors.toList());
            ArrayList<Article> finalArgs = args;
            args = (ArrayList<Article>) args.stream()
                    .filter(Article -> Article.getTimeToRead() < finalArgs.stream().mapToInt(num->num.getTimeToRead()).average().getAsDouble())
                    .distinct()
                    .sorted(new ArticleComparator())
                    .collect(Collectors.toList());

            ArticleArray articles = new ArticleArray(args);
            marshaller.marshal(articles, new File("src/main/java/Data/1_4DataJaxb.xml"));
        }
    }

    /**
     * Множество статей, время прочтения которых меньше среднего в формате XML
     */
    static class Print1_4XmlDom<T> implements ParserWorker.Print<ArrayList<Article>> {
        @Override
        public void Print(ArrayList<Article> args) throws JsonProcessingException, JAXBException
        {
            Document document = DocumentHelper.createDocument();
            Element root = document.addElement("Articles");

            args = (ArrayList<Article>) args.stream().distinct().collect(Collectors.toList());
            ArrayList<Article> finalArgs = args;
            args = (ArrayList<Article>) args.stream()
                    .filter(Article -> Article.getTimeToRead() < finalArgs.stream().mapToInt(num->num.getTimeToRead()).average().getAsDouble())
                    .distinct()
                    .sorted(new ArticleComparator())
                    .collect(Collectors.toList());

            for (Article article : args) {
                // Создание элемента <article>
                Element articleElement = root.addElement("article");

                // Добавление полей статьи в элемент <article>
                articleElement.addElement("author").addText(article.getAuthor());
                articleElement.addElement("name").addText(article.getName());
                articleElement.addElement("realise").addText(article.getRealise());
                articleElement.addElement("timeToRead").addText(String.valueOf(article.getTimeToRead()));
                articleElement.addElement("views").addText(String.valueOf(article.getViews()));
                articleElement.addElement("imgAddress").addText(article.getImgAddress());
                articleElement.addElement("prehedder").addText(article.getPrehedder());

                // Добавление элемента <Rubriks>
                Element rubriksElement = articleElement.addElement("Rubriks");
                for (String rubrik : article.getRubriks()) {
                    rubriksElement.addElement("rubriks").addText(rubrik);
                }
            }

            // Запись документа в файл
            try (FileWriter fileWriter = new FileWriter(new File("src/main/java/Data/1_4DataDom.xml"))) {
                OutputFormat format = OutputFormat.createPrettyPrint();
                XMLWriter writer = new XMLWriter(fileWriter, format);
                writer.write(document);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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

    /**
     * Множество различных спектаклей без поля ageLimit в формате json
     */
    static class Print2_3Json<T> implements ParserWorker.Print<ArrayList<Performance>>
    {
        @Override
        public void Print(ArrayList<Performance> args) throws JsonProcessingException {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

            List<PerformanceWithDates> performanceWithDatesList = args.stream()
                    .collect(Collectors.groupingBy(Performance::getName))
                    .entrySet().stream()
                    .map(entry -> {
                        Performance firstPerformance = entry.getValue().get(0);
                        List<String> dates = entry.getValue().stream()
                                .map(Performance::getDate)
                                .collect(Collectors.toCollection(ArrayList::new));
                        return new PerformanceWithDates(
                                firstPerformance.getName(),
                                firstPerformance.getAgeLimit(),
                                firstPerformance.getSize(),
                                firstPerformance.getImgAddress(),
                                new ArrayList<>(dates)
                        );
                    })
                    .collect(Collectors.toList());

            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(performanceWithDatesList);
            try (FileWriter writer = new FileWriter("src/main/java/Data/2_3Data.json", false)) {
                writer.write(json);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Множество различных спектаклей без поля ageLimit в формате json
     */
    static class Print2_4Json<T> implements ParserWorker.Print<ArrayList<Performance>>
    {
        @Override
        public void Print(ArrayList<Performance> args) throws JsonProcessingException {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

            List<PerformanceWithDates> performanceWithDatesList = args.stream()
                    .filter(performance -> {
                        DayOfWeek dayOfWeek = getDayOfWeek(performance.getDate());
                        LocalTime time = getTime(performance.getDate());
                        return (dayOfWeek == DayOfWeek.TUESDAY || dayOfWeek == DayOfWeek.THURSDAY || dayOfWeek == DayOfWeek.SATURDAY)
                                && time.isAfter(LocalTime.of(14, 59)); // После 15:00 включительно
                    })
                    .filter(performance -> performance.getSize() < 90) // 90 минут = 1,5 часа
                    .collect(Collectors.groupingBy(Performance::getName))
                    .entrySet().stream()
                    .map(entry -> {
                        Performance firstPerformance = entry.getValue().get(0);
                        List<String> dates = entry.getValue().stream()
                                .map(Performance::getDate)
                                .collect(Collectors.toCollection(ArrayList::new));
                        return new PerformanceWithDates(
                                firstPerformance.getName(),
                                firstPerformance.getAgeLimit(),
                                firstPerformance.getSize(),
                                firstPerformance.getImgAddress(),
                                new ArrayList<>(dates)
                        );
                    })
                    .collect(Collectors.toList());
            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(performanceWithDatesList);
            try (FileWriter writer = new FileWriter("src/main/java/Data/2_4Data.json", false)) {
                writer.write(json);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private DayOfWeek getDayOfWeek(String date) {
            // Создаем маппинг сокращений дней недели на русском языке
            Map<String, DayOfWeek> dayOfWeekMap = new HashMap<>();
            dayOfWeekMap.put("Пн", DayOfWeek.MONDAY);
            dayOfWeekMap.put("Вт", DayOfWeek.TUESDAY);
            dayOfWeekMap.put("Ср", DayOfWeek.WEDNESDAY);
            dayOfWeekMap.put("Чт", DayOfWeek.THURSDAY);
            dayOfWeekMap.put("Пт", DayOfWeek.FRIDAY);
            dayOfWeekMap.put("Сб", DayOfWeek.SATURDAY);
            dayOfWeekMap.put("Вс", DayOfWeek.SUNDAY);

            // Извлекаем сокращение дня недели из строки даты
            String dayOfWeekStr = date.substring(3, 5).trim();

            // Получаем соответствующий день недели
            return dayOfWeekMap.get(dayOfWeekStr);
        }

        private LocalTime getTime(String date) {
            // Извлекаем время из строки даты
            String timeStr = date.substring(6).trim();
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            return LocalTime.parse(timeStr, timeFormatter);
        }
    }

    /**
     * Множество различных спектаклей без поля ageLimit в формате json
     */
    static class Print2_4XmlDom<T> implements ParserWorker.Print<ArrayList<Performance>>
    {
        @Override
        public void Print(ArrayList<Performance> args) throws JsonProcessingException, ParserConfigurationException
        {
            List<PerformanceWithDates> performanceWithDatesList = args.stream()
                    .filter(performance -> {
                        DayOfWeek dayOfWeek = getDayOfWeek(performance.getDate());
                        LocalTime time = getTime(performance.getDate());
                        return (dayOfWeek == DayOfWeek.TUESDAY || dayOfWeek == DayOfWeek.THURSDAY || dayOfWeek == DayOfWeek.SATURDAY)
                                && time.isAfter(LocalTime.of(14, 59)); // После 15:00 включительно
                    })
                    .filter(performance -> performance.getSize() < 90) // 90 минут = 1,5 часа
                    .collect(Collectors.groupingBy(Performance::getName))
                    .entrySet().stream()
                    .map(entry -> {
                        Performance firstPerformance = entry.getValue().get(0);
                        List<String> dates = entry.getValue().stream()
                                .map(Performance::getDate)
                                .collect(Collectors.toCollection(ArrayList::new));
                        return new PerformanceWithDates(
                                firstPerformance.getName(),
                                firstPerformance.getAgeLimit(),
                                firstPerformance.getSize(),
                                firstPerformance.getImgAddress(),
                                new ArrayList<>(dates)
                        );
                    })
                    .collect(Collectors.toList());

            Document document = DocumentHelper.createDocument();
            Element root = document.addElement("Performances");

            for (PerformanceWithDates performanceWithDates : performanceWithDatesList) {
                Element performanceElement = root.addElement("Performance");
                performanceElement.addElement("Name").setText(performanceWithDates.getName());
                performanceElement.addElement("AgeLimit").setText(performanceWithDates.getAgeLimit());
                performanceElement.addElement("Size").setText(String.valueOf(performanceWithDates.getSize()));
                performanceElement.addElement("ImgAddress").setText(performanceWithDates.getImgAddress());

                Element datesElement = performanceElement.addElement("Dates");
                for (String date : performanceWithDates.getDates()) {
                    datesElement.addElement("Date").setText(date);
                }
            }

            try (FileWriter writer = new FileWriter("src/main/java/Data/2_4DataDom.xml", false)) {
                OutputFormat format = OutputFormat.createPrettyPrint();
                XMLWriter xmlWriter = new XMLWriter(writer, format);
                xmlWriter.write(document);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private DayOfWeek getDayOfWeek(String date) {
            // Создаем маппинг сокращений дней недели на русском языке
            Map<String, DayOfWeek> dayOfWeekMap = new HashMap<>();
            dayOfWeekMap.put("Пн", DayOfWeek.MONDAY);
            dayOfWeekMap.put("Вт", DayOfWeek.TUESDAY);
            dayOfWeekMap.put("Ср", DayOfWeek.WEDNESDAY);
            dayOfWeekMap.put("Чт", DayOfWeek.THURSDAY);
            dayOfWeekMap.put("Пт", DayOfWeek.FRIDAY);
            dayOfWeekMap.put("Сб", DayOfWeek.SATURDAY);
            dayOfWeekMap.put("Вс", DayOfWeek.SUNDAY);

            // Извлекаем сокращение дня недели из строки даты
            String dayOfWeekStr = date.substring(3, 5).trim();

            // Получаем соответствующий день недели
            return dayOfWeekMap.get(dayOfWeekStr);
        }

        private LocalTime getTime(String date) {
            // Извлекаем время из строки даты
            String timeStr = date.substring(6).trim();
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            return LocalTime.parse(timeStr, timeFormatter);
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException, JAXBException, ParserConfigurationException
    {
        /*ParserWorker<Article> parser = new ParserWorker<>(new HabrParser());
        int start = 1;
        int end = 5;
        parser.setParserSettings(new HabrSettings(1, 5));
        parser.onCompletedList.add(new Completed());
        parser.onNewDataList.add(new NewData());
        parser.onPrintList.add(new Print1_4XmlDom());
        parser.Start();
        Thread.sleep(10000);
        parser.Abort();*/

        /*ParserWorker<Article> parser = new ParserWorker<>(new HabrParser());
        parser.setParserSettings(new HabrSettings(1, 5));
        parser.onCompletedList.add(new Completed());
        parser.onNewDataList.add(new NewData());
        parser.onPrintList.add(new Print2_2Json());
        parser.Start();
        Thread.sleep(10000);
        parser.Abort();*/

        ParserWorker<Performance> parser = new ParserWorker<>(new DramTeatrParser());
        parser.setParserSettings(new DramTeatrSettings());
        parser.onCompletedList.add(new Completed());
        parser.onNewDataList.add(new NewData());
        parser.onPrintList.add(new Print2_4XmlDom());
        parser.Start();
        Thread.sleep(5000);
        parser.Abort();
    }
}

