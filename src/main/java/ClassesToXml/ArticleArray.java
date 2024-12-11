package ClassesToXml;

import lombok.Getter;
import lombok.NoArgsConstructor;
import model.Article;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;

@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Articles")
public class ArticleArray
{
    @Getter
    @XmlElement(name = "article")
    private ArrayList<Article> articles;

    public ArticleArray(ArrayList<Article> _articles)
    {
        articles = new ArrayList<>();
        this.articles = _articles;
    }
}
