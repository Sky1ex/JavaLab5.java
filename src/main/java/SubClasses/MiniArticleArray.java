package SubClasses;

import lombok.Getter;
import lombok.NoArgsConstructor;
import model.Article;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Objects;

@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Articles")
public class MiniArticleArray
{
    @Getter
    @XmlElement(name = "article")
    private ArrayList<MiniArticle> articles;

    public MiniArticleArray(ArrayList<Article> _articles)
    {
        articles = new ArrayList<>();
        for(int i = 0; i < _articles.size(); i++)
        {
            articles.add(new MiniArticle(_articles.get(i)));
        }
    }

}
