package ClassesToXml;

import SubClasses.MiniArticleArray;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import model.Article;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Authors")
public class MapArray
{
    @Getter
    @XmlElement(name = "author")
    private String Author;
    @Getter
    @XmlElement(name = "articles")
    private MiniArticleArray articles;
}
