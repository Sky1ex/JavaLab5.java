package DefaultClasses;

import org.jsoup.nodes.Document;

import java.io.IOException;

public interface Parser<T>
{
    T Parse(Document document) throws IOException;
}
