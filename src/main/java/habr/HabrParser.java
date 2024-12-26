package habr;

import DefaultClasses.Parser;
import model.Article;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;

public class HabrParser implements Parser<ArrayList<Article>>
{

    public ArrayList<Article> Parse(Document document)
    {
        Elements article = document.select("article[class^=tm-articles-list__item]");
        ArrayList<Article> list = new ArrayList<Article>();
        for(org.jsoup.nodes.Element elem : article)
        {
            String temp = elem.select("div.tm-article-snippet.tm-article-snippet > div.tm-article-snippet__meta-container > div > span > span > a.tm-user-info__username").text();
            if(temp=="")
                continue
                        ;
            list.add(new Article(elem));
        }
        return list;
    }
}