package ClassesToXml;

import SubClasses.MiniArticle;
import SubClasses.MiniArticleArray;
import lombok.NoArgsConstructor;
import model.Article;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Authors")
public class AuthorArray
{
    @XmlElement(name = "author")
    private ArrayList<MapArray> mapArrays;

    public AuthorArray(Map<String, ArrayList<Article>> _articlesByAuthor)
    {
        mapArrays = new ArrayList<>();
        String Author;
        MiniArticleArray articles;
        Set<String> set = _articlesByAuthor.keySet();
        for(String temp : set)
        {
            Author = temp;
            MiniArticleArray tempArray = new MiniArticleArray(_articlesByAuthor.get(temp));
            articles = tempArray;
            mapArrays.add(new MapArray(Author, articles));
        }
    }
}
