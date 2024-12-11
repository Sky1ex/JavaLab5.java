package Ekvus;

import DefaultClasses.Parser;
import model.Performance;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class EkvusParser implements Parser<ArrayList<Performance>>
{

    public ArrayList<Performance> Parse(Document document) throws IOException {
        Elements article = document.select("body > table.t_base > tbody > tr:nth-child(1) > td.td2 > div > table > tbody").get(0).getElementsByTag("tr");
        ArrayList<Performance> list = new ArrayList<>();
        for(Element elem : article)
        {
            if(elem.childNodeSize() <= 1) continue;
            String part1 = elem.select("tr > td:nth-child(2) > a").attr("href");
            String part2 = document.location().substring(0, document.location().lastIndexOf("/")-1);
            String AgeLimit = elem.select("span[class^=al]").text();
            part2 = part2.substring(0, part2.lastIndexOf("/"));
            Document temp = Jsoup.connect(part2+part1).get();
            String name = temp.select("body > table.t_base > tbody > tr:nth-child(1) > td.td2 > div > table:nth-child(5) > tbody > tr:nth-child(2) > td").tagName("h1").first().text();
            String SizeTemp = temp.select("body > table.t_base > tbody > tr:nth-child(1) > td.td2 > div > div:nth-child(8)").text();
            String Size = "";
            String Date = "";
            if(SizeTemp.startsWith("Продолжительность")) Size = SizeTemp;
            else if(SizeTemp.startsWith("Премьера")) Date = SizeTemp;
            String DateTemp = temp.select("body > table.t_base > tbody > tr:nth-child(1) > td.td2 > div > div:nth-child(9)").text();

            if(DateTemp.startsWith("Премьера")) Date = DateTemp;
            else if(DateTemp.startsWith("Продолжительность")) Size = DateTemp;
            String ImgAddress = temp.select("a[rel^=lightbox[afisha]]").attr("href");

            Size = Size.toLowerCase();

            int hours = 0, minutes = 0;
            if(Size.contains("час")) hours = Integer.parseInt(Size.substring(Size.indexOf("час")-2,Size.indexOf("час")-1));
            if(Size.contains("минут")) hours = Integer.parseInt(Size.substring(Size.indexOf("минут")-2,Size.indexOf("минут")-1));

            int time = hours * 60 + minutes;

            list.add(new Performance(name, AgeLimit, time, Date, ImgAddress));
        }
        return list;
    }
}