package SubClasses;

import lombok.Getter;
import lombok.NoArgsConstructor;
import model.Article;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Objects;

@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "MiniArticle")
@Getter
public class MiniArticle
{
    @XmlElement(name = "name")
    private String name;
    @XmlElement(name = "prehedder")
    private String prehedder;

    public MiniArticle(Article article)
    {
        this.name = article.getName();
        this.prehedder = article.getPrehedder();
    }

    @Override
    public boolean equals(Object o) {
        MiniArticle article = (MiniArticle) o;
        return Objects.equals(name, article.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

}
