package model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Article")
public class Article
{
    @Getter
    @XmlElement(name = "author")
    private String author;

    @Getter
    @XmlElement(name = "name")
    private String name;

    @Getter
    @XmlElement(name = "realise")
    private String realise;

    @Getter
    @XmlElement(name = "timeToRead")
    private int timeToRead;

    @Getter
    @XmlElement(name = "views")
    private int views;

    @Getter
    @XmlElement(name = "imgAddress")
    private String imgAddress;

    @Getter
    @XmlElementWrapper(name="Rubriks")
    @XmlElement(name = "rubriks")
    private List<String> rubriks;

    @Getter
    @XmlElement(name = "prehedder")
    private String prehedder;

    @Override
    public boolean equals(Object o) {
        Article article = (Article) o;
        return Objects.equals(author, article.author) &&
                Objects.equals(name, article.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(author, name);
    }

    public Article(Element art) {
        this.prehedder = "";
        this.rubriks = new ArrayList<>();
        this.author = art.select("div.tm-article-snippet.tm-article-snippet > div.tm-article-snippet__meta-container > div > span > span > a.tm-user-info__username").text();
        this.name = art.select("div.tm-article-snippet.tm-article-snippet > h2 > a > span").text();
        String date = art.select("div.tm-article-snippet.tm-article-snippet > div.tm-article-snippet__meta-container > div > span > span > a.tm-article-datetime-published.tm-article-datetime-published_link > time").attr("datetime");
        date = date.replaceAll(":", "-").replace("T", "-");
        date = date.substring(0, date.length() - 5);
        String[] date1 = date.split("-");
        this.realise = new Date(Integer.parseInt(date1[0]) - 1900, Integer.parseInt(date1[1]), Integer.parseInt(date1[2]) + 17, Integer.parseInt(date1[3]), Integer.parseInt(date1[4]), Integer.parseInt(date1[5])).toString();
        String timeToRead = art.select("div.tm-article-snippet.tm-article-snippet > div.tm-article-snippet__stats > div.tm-article-reading-time > span.tm-article-reading-time__label").text();
        this.timeToRead = Integer.parseInt(timeToRead.substring(0, timeToRead.indexOf(" мин")));
        String views = art.select("div.tm-article-snippet.tm-article-snippet > div.tm-article-snippet__stats > span > span").text();
        if (views.endsWith("K")) {
            views = views.substring(0, views.length() - 1);
            if (views.contains(".")) views = views.substring(0, views.indexOf(".")) + views.substring(views.indexOf(".") + 1, views.length());
            this.views = Integer.parseInt(views) * 1000;
        } else {
            this.views = Integer.parseInt(views);
        }
        this.imgAddress = art.select("div.tm-article-snippet.tm-article-snippet > div.tm-article-body.tm-article-snippet__lead > div.tm-article-snippet__cover_cover.tm-article-snippet__cover > img").attr("src");
        Elements rub = art.select("div[class^=tm-publication-hubs] > span");
        for (int i = 0; i < rub.size(); i++) {
            this.rubriks.add(rub.get(i).text());
        }
        Elements hed = art.select("div.tm-article-snippet.tm-article-snippet > div.tm-article-body.tm-article-snippet__lead > div:nth-child(2) > div > div");
        for (Element elem : hed) {
            this.prehedder += elem.text();
        }
    }
}
