package DramTeatr;

import DefaultClasses.Parser;
import model.Performance;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class DramTeatrParser implements Parser<ArrayList<Performance>>
{
    public ArrayList<Performance> Parse(Document document) throws IOException
    {
        Elements article = document.select("#list_afisha").get(0).getElementsByClass("t_afisha");
        ArrayList<Performance> list = new ArrayList<>();
        for(Element elem : article)
        {
            String name = elem.select("div.td3 > div > div.td1 > h3 > a").text();
            String AgeLimit = elem.select("div.td3 > div > div.td2 > div.t_u3 > div.td1 > div.value_limit").text();
            String Date = elem.select("div.td1 > div.num").text();
            Date += " " + elem.select("div.td1 > div.day").text();
            Date += " " + elem.select("div.td1 > div.time").text();
            String Size = elem.select("div.td3 > div > div.td2 > div.t_u3 > div.td1 > div:nth-child(1)").text();

            Size = Size.toLowerCase();

            int hours = 0, minutes = 0;
            if(Size.contains("час")) hours = Integer.parseInt(Size.substring(Size.indexOf("час")-2,Size.indexOf("час")-1));
            if(Size.contains("минут")) minutes = Integer.parseInt(Size.substring(Size.indexOf("минут")-3,Size.indexOf("минут")-1));

            int time = hours * 60 + minutes;

            String ImgAddress = elem.select("div.td2 > div > a > img").attr("src");
            list.add(new Performance(name, AgeLimit, time, Date, ImgAddress));
        }
        return list;
    }
}