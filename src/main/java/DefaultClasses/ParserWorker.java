package DefaultClasses;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.jsoup.nodes.Document;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;

public class ParserWorker<T>
{
    private Parser<T> parser;
    private ParserSettings parserSettings;
    private HtmlLoader loader;
    boolean isActive;
    public ArrayList<OnNewDataHandler> onNewDataList = new ArrayList<>();
    public ArrayList<OnCompleted> onCompletedList = new ArrayList<>();
    public ArrayList<Print> onPrintList = new ArrayList<>();

    public ParserWorker (Parser habrParser)
    {
        parser = (Parser<T>) habrParser;
    }

    public interface OnNewDataHandler<T> {
        void OnNewData(Object sender, T e);
        T GetData();
    }

    public interface OnCompleted
    {
        void OnCompleted(Object sender);
    }

    public interface Print<T>
    {
        void Print(T e) throws JsonProcessingException, JAXBException, ParserConfigurationException;
    }

    public Parser<T> getParser() {
        return parser;
    }

    public void setParser(Parser<T> parser) {
        this.parser = parser;
    }

    public ParserSettings getParserSettings() {
        return parserSettings;
    }

    public void setParserSettings(ParserSettings parserSettings)
    {
        loader = new HtmlLoader(parserSettings);
        this.parserSettings = parserSettings;
    }
    public void Abort() {
        isActive = false;
    }

    public void Start() throws IOException, JAXBException, ParserConfigurationException {
        isActive = true;
        Worker();
    }

    public void Worker() throws IOException, JAXBException, ParserConfigurationException {
        for (int i = parserSettings.getStartPoint(); i <= parserSettings.getEndPoint(); i++) {
            if (!isActive) {
                onCompletedList.get(0).OnCompleted(this);
                return;
            }
            Document document = loader.GetSourceByPageId(i);
            T result = parser.Parse(document);
            onNewDataList.get(0).OnNewData(this,result);
        }
        onCompletedList.get(0).OnCompleted(this);
        onPrintList.get(0).Print(onNewDataList.get(0).GetData());
        isActive = false;
    }

}